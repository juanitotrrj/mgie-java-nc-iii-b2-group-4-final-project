package com.group4.inventoryserver.service;

import com.group4.inventoryserver.config.EnvConfig;
import com.group4.inventoryserver.dto.auth.LoginData;
import com.group4.inventoryserver.dto.auth.LoginRequest;
import com.group4.inventoryserver.dto.auth.UserProfile;
import com.group4.inventoryserver.exception.ForbiddenException;
import com.group4.inventoryserver.exception.UnauthorizedException;
import com.group4.inventoryserver.exception.ValidationException;
import com.group4.inventoryserver.repository.SessionRepository;
import com.group4.inventoryserver.repository.UserRepository;
import com.group4.inventoryserver.security.JwtUtil;
import com.group4.inventoryserver.security.PasswordUtil;
import com.group4.inventoryserver.util.HashUtil;
import com.group4.inventoryserver.util.ValidationUtil;
import com.group4.inventoryserver.util.ValidationUtil.FieldError;
import io.jsonwebtoken.Claims;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthService {

  private static final Logger log = LoggerFactory.getLogger(AuthService.class);

  private static final Set<String> VALID_ROLES =
      new HashSet<>(Arrays.asList("Administrator", "Manager", "Inventory Clerk", "Cashier"));

  private final UserRepository userRepository = new UserRepository();
  private final SessionRepository sessionRepository = new SessionRepository();

  public LoginData login(LoginRequest request, String ip, String userAgent) {
    validateLoginRequest(request);

    Map<String, Object> user = userRepository.findByUsername(request.getUsername());
    if (user == null) {
      throw new UnauthorizedException("Invalid username or password.");
    }

    long userId = (Long) user.get("userId");
    String status = (String) user.get("status");

    if ("Inactive".equals(status)) {
      throw new ForbiddenException("Account is inactive. Contact an administrator.");
    }
    if ("Locked".equals(status)) {
      throw new ForbiddenException("Account is locked due to too many failed login attempts.");
    }

    String passwordHash = (String) user.get("passwordHash");
    if (!PasswordUtil.verify(request.getPassword(), passwordHash)) {
      int attempts = (Integer) user.get("failedLoginAttempts") + 1;
      userRepository.incrementFailedAttempts(userId);
      if (attempts >= EnvConfig.securityLockAfterFailedAttempts()) {
        userRepository.lockUser(userId);
        log.warn("User {} locked after {} failed attempts", request.getUsername(), attempts);
        throw new ForbiddenException("Account locked due to too many failed login attempts.");
      }
      throw new UnauthorizedException("Invalid username or password.");
    }

    String actualRole = (String) user.get("roleName");
    if (!actualRole.equals(request.getRole())) {
      throw new UnauthorizedException("Selected role does not match your assigned role.");
    }

    userRepository.resetFailedAttempts(userId);
    userRepository.updateLastLogin(userId);

    long roleId = (Long) user.get("roleId");
    List<String> permissions = userRepository.findPermissionsByRoleId(roleId);

    int expirySeconds = EnvConfig.authTokenExpirySeconds();
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", userId);
    claims.put("role", actualRole);
    claims.put("username", request.getUsername());

    String token = JwtUtil.generateToken(String.valueOf(userId), claims);

    String tokenHash = HashUtil.sha256(token);
    Timestamp expiresAt = new Timestamp(System.currentTimeMillis() + (expirySeconds * 1000L));
    if (!EnvConfig.securityAllowMultipleSessions()) {
      sessionRepository.invalidateAllForUser(userId);
    }
    sessionRepository.create(userId, tokenHash, ip, userAgent, expiresAt);

    UserProfile profile =
        new UserProfile(
            userId,
            (String) user.get("fullName"),
            (String) user.get("username"),
            (String) user.get("email"),
            actualRole,
            status,
            (String) user.get("lastLoginAt"),
            permissions);

    log.info("User {} logged in successfully [role={}]", request.getUsername(), actualRole);
    return new LoginData(token, EnvConfig.authTokenType(), expirySeconds, profile);
  }

  public void logout(String token) {
    Claims claims = JwtUtil.validateToken(token);
    if (claims == null) {
      throw new UnauthorizedException("Invalid or expired token.");
    }

    String tokenHash = HashUtil.sha256(token);
    Map<String, Object> session = sessionRepository.findActiveByTokenHash(tokenHash);
    if (session != null) {
      sessionRepository.invalidate((Long) session.get("sessionId"), "Logged Out");
    }
    log.info("User {} logged out", claims.getSubject());
  }

  public UserProfile getCurrentUser(String token) {
    Claims claims = JwtUtil.validateToken(token);
    if (claims == null) {
      throw new UnauthorizedException("Invalid or expired token.");
    }

    long userId = ((Number) claims.get("userId")).longValue();
    Map<String, Object> user = userRepository.findById(userId);
    if (user == null) {
      throw new UnauthorizedException("User not found.");
    }

    long roleId = (Long) user.get("roleId");
    List<String> permissions = userRepository.findPermissionsByRoleId(roleId);
    return userRepository.buildProfile(user, permissions);
  }

  public Map<String, Object> validateSession(String token) {
    Claims claims = JwtUtil.validateToken(token);
    if (claims == null) {
      return null;
    }

    String tokenHash = HashUtil.sha256(token);
    Map<String, Object> session = sessionRepository.findActiveByTokenHash(tokenHash);
    if (session == null) {
      return null;
    }

    sessionRepository.updateLastActivity((Long) session.get("sessionId"));

    Map<String, Object> context = new HashMap<>();
    context.put("userId", ((Number) claims.get("userId")).longValue());
    context.put("role", claims.get("role"));
    context.put("username", claims.get("username"));
    context.put("sessionId", session.get("sessionId"));
    return context;
  }

  private void validateLoginRequest(LoginRequest request) {
    List<FieldError> errors = ValidationUtil.newErrorList();
    ValidationUtil.requireNonBlank(request.getUsername(), "username", errors);
    ValidationUtil.requireNonBlank(request.getPassword(), "password", errors);
    ValidationUtil.requireNonBlank(request.getRole(), "role", errors);
    ValidationUtil.throwIfErrors(errors);

    if (!VALID_ROLES.contains(request.getRole())) {
      throw new ValidationException("Invalid role. Must be one of: " + VALID_ROLES);
    }
  }
}
