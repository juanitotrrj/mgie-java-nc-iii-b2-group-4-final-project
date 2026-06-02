package com.group4.inventoryserver.service;

import com.group4.inventoryserver.dto.PaginatedResponse;
import com.group4.inventoryserver.dto.PaginationMeta;
import com.group4.inventoryserver.dto.PaginationParams;
import com.group4.inventoryserver.dto.user.ResetPasswordRequest;
import com.group4.inventoryserver.dto.user.UserCreateRequest;
import com.group4.inventoryserver.dto.user.UserData;
import com.group4.inventoryserver.dto.user.UserUpdateRequest;
import com.group4.inventoryserver.exception.ConflictException;
import com.group4.inventoryserver.exception.NotFoundException;
import com.group4.inventoryserver.exception.ValidationException;
import com.group4.inventoryserver.repository.UserRepository;
import com.group4.inventoryserver.security.PasswordUtil;
import com.group4.inventoryserver.util.ValidationUtil;
import com.group4.inventoryserver.util.ValidationUtil.FieldError;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {

  private static final Logger log = LoggerFactory.getLogger(UserService.class);

  private static final Set<String> VALID_ROLES =
      new HashSet<>(Arrays.asList("Administrator", "Manager", "Inventory Clerk", "Cashier"));

  private static final Set<String> VALID_STATUSES =
      new HashSet<>(Arrays.asList("Active", "Inactive", "Locked"));

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

  private static final int MIN_PASSWORD_LENGTH = 8;

  private final UserRepository userRepository;

  public UserService() {
    this(new UserRepository());
  }

  UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public PaginatedResponse<UserData> list(PaginationParams params, String role, String status) {
    List<UserData> data =
        userRepository.findAll(
            params.getOffset(),
            params.getSize(),
            params.getSortBy(),
            params.getSortDir(),
            params.getSearch(),
            role,
            status);
    long total = userRepository.count(params.getSearch(), role, status);
    PaginationMeta meta =
        new PaginationMeta(
            params.getPage(), params.getSize(), total, params.getSortBy(), params.getSortDir());
    return new PaginatedResponse<>(data, meta);
  }

  public UserData getById(long userId) {
    UserData user = userRepository.findDetailById(userId);
    if (user == null) {
      throw new NotFoundException("User not found.");
    }
    return user;
  }

  public UserData create(UserCreateRequest request, long authUserId) {
    validateCreateRequest(request);

    if (userRepository.existsByUsername(request.getUsername().trim())) {
      throw new ConflictException("Username already exists.");
    }
    if (userRepository.existsByEmail(request.getEmail().trim())) {
      throw new ConflictException("Email already exists.");
    }

    Long roleId = userRepository.getRoleIdByName(request.getRole());
    if (roleId == null) {
      throw new ValidationException("Invalid role: " + request.getRole());
    }

    String passwordHash = PasswordUtil.hash(request.getPassword());
    String userCode = userRepository.generateNextUserCode();
    String status =
        request.getStatus() != null && !request.getStatus().trim().isEmpty()
            ? request.getStatus().trim()
            : "Active";

    long userId =
        userRepository.insert(
            userCode,
            request.getFullName().trim(),
            request.getUsername().trim(),
            request.getEmail().trim(),
            passwordHash,
            roleId,
            status,
            authUserId);

    log.info(
        "User created [id={}, code={}, username={}] by user {}",
        userId,
        userCode,
        request.getUsername().trim(),
        authUserId);
    return getById(userId);
  }

  public UserData update(long userId, UserUpdateRequest request, long authUserId) {
    UserData existing = userRepository.findDetailById(userId);
    if (existing == null) {
      throw new NotFoundException("User not found.");
    }

    String fullName =
        request.getFullName() != null && !request.getFullName().trim().isEmpty()
            ? request.getFullName().trim()
            : existing.getFullName();
    String email =
        request.getEmail() != null && !request.getEmail().trim().isEmpty()
            ? request.getEmail().trim()
            : existing.getEmail();
    String role =
        request.getRole() != null && !request.getRole().trim().isEmpty()
            ? request.getRole().trim()
            : existing.getRole();
    String status =
        request.getStatus() != null && !request.getStatus().trim().isEmpty()
            ? request.getStatus().trim()
            : existing.getStatus();

    List<FieldError> errors = ValidationUtil.newErrorList();
    ValidationUtil.requireMaxLength(fullName, "fullName", 150, errors);
    if (!EMAIL_PATTERN.matcher(email).matches()) {
      errors.add(new FieldError("email", "Invalid email format."));
    }
    ValidationUtil.requireMaxLength(email, "email", 150, errors);
    if (!VALID_ROLES.contains(role)) {
      errors.add(new FieldError("role", "Invalid role."));
    }
    if (!VALID_STATUSES.contains(status)) {
      errors.add(new FieldError("status", "Invalid status."));
    }
    ValidationUtil.throwIfErrors(errors);

    if (!email.equals(existing.getEmail())
        && userRepository.existsByEmailExcluding(email, userId)) {
      throw new ConflictException("Email already exists.");
    }

    Long roleId = userRepository.getRoleIdByName(role);
    if (roleId == null) {
      throw new ValidationException("Invalid role: " + role);
    }

    userRepository.update(userId, fullName, email, roleId, status, authUserId);
    log.info("User updated [id={}] by user {}", userId, authUserId);
    return getById(userId);
  }

  public void deactivate(long userId, long authUserId) {
    UserData existing = userRepository.findDetailById(userId);
    if (existing == null) {
      throw new NotFoundException("User not found.");
    }
    if (userId == authUserId) {
      throw new ConflictException("Cannot deactivate your own account.");
    }

    userRepository.deactivate(userId, authUserId);
    userRepository.invalidateSessions(userId);
    log.info("User deactivated [id={}] by user {}", userId, authUserId);
  }

  public void resetPassword(long userId, ResetPasswordRequest request, long authUserId) {
    UserData existing = userRepository.findDetailById(userId);
    if (existing == null) {
      throw new NotFoundException("User not found.");
    }

    List<FieldError> errors = ValidationUtil.newErrorList();
    ValidationUtil.requireNonBlank(request.getNewPassword(), "newPassword", errors);
    ValidationUtil.throwIfErrors(errors);

    validatePasswordPolicy(request.getNewPassword(), errors);
    ValidationUtil.throwIfErrors(errors);

    String passwordHash = PasswordUtil.hash(request.getNewPassword());
    boolean mustChange =
        request.getForceChangeOnNextLogin() != null && request.getForceChangeOnNextLogin();

    userRepository.resetPassword(userId, passwordHash, mustChange);
    log.info("Password reset for user [id={}] by user {}", userId, authUserId);
  }

  private void validateCreateRequest(UserCreateRequest request) {
    List<FieldError> errors = ValidationUtil.newErrorList();
    ValidationUtil.requireNonBlank(request.getFullName(), "fullName", errors);
    ValidationUtil.requireMaxLength(request.getFullName(), "fullName", 150, errors);
    ValidationUtil.requireNonBlank(request.getUsername(), "username", errors);
    ValidationUtil.requireMaxLength(request.getUsername(), "username", 60, errors);
    ValidationUtil.requireNonBlank(request.getEmail(), "email", errors);
    ValidationUtil.requireMaxLength(request.getEmail(), "email", 150, errors);
    if (request.getEmail() != null
        && !request.getEmail().trim().isEmpty()
        && !EMAIL_PATTERN.matcher(request.getEmail().trim()).matches()) {
      errors.add(new FieldError("email", "Invalid email format."));
    }
    ValidationUtil.requireNonBlank(request.getRole(), "role", errors);
    if (request.getRole() != null
        && !request.getRole().trim().isEmpty()
        && !VALID_ROLES.contains(request.getRole().trim())) {
      errors.add(new FieldError("role", "Invalid role."));
    }
    if (request.getStatus() != null
        && !request.getStatus().trim().isEmpty()
        && !VALID_STATUSES.contains(request.getStatus().trim())) {
      errors.add(new FieldError("status", "Invalid status."));
    }
    ValidationUtil.requireNonBlank(request.getPassword(), "password", errors);
    ValidationUtil.throwIfErrors(errors);

    validatePasswordPolicy(request.getPassword(), errors);
    ValidationUtil.throwIfErrors(errors);
  }

  private void validatePasswordPolicy(String password, List<FieldError> errors) {
    if (password.length() < MIN_PASSWORD_LENGTH) {
      errors.add(
          new FieldError(
              "password", "Password must be at least " + MIN_PASSWORD_LENGTH + " characters."));
      return;
    }
    boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
    for (char c : password.toCharArray()) {
      if (Character.isUpperCase(c)) hasUpper = true;
      else if (Character.isLowerCase(c)) hasLower = true;
      else if (Character.isDigit(c)) hasDigit = true;
      else hasSpecial = true;
    }
    if (!hasUpper || !hasLower || !hasDigit || !hasSpecial) {
      errors.add(
          new FieldError(
              "password",
              "Password must contain uppercase, lowercase, digit, and special character."));
    }
  }
}
