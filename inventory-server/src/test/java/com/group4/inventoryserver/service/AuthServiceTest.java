package com.group4.inventoryserver.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.group4.inventoryserver.dto.auth.LoginData;
import com.group4.inventoryserver.dto.auth.LoginRequest;
import com.group4.inventoryserver.dto.auth.UserProfile;
import com.group4.inventoryserver.exception.ForbiddenException;
import com.group4.inventoryserver.exception.UnauthorizedException;
import com.group4.inventoryserver.exception.ValidationException;
import com.group4.inventoryserver.repository.SessionRepository;
import com.group4.inventoryserver.repository.UserRepository;
import com.group4.inventoryserver.security.PasswordUtil;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class AuthServiceTest {

  private UserRepository userRepository;
  private SessionRepository sessionRepository;
  private AuthService authService;

  @Before
  public void setUp() {
    userRepository = mock(UserRepository.class);
    sessionRepository = mock(SessionRepository.class);
    authService = new AuthService(userRepository, sessionRepository);
  }

  private LoginRequest makeLoginRequest(String username, String password, String role) {
    LoginRequest req = new LoginRequest();
    req.setUsername(username);
    req.setPassword(password);
    req.setRole(role);
    return req;
  }

  private Map<String, Object> makeUserMap(
      long userId,
      String username,
      String passwordHash,
      String roleName,
      String status,
      long roleId,
      int failedAttempts) {
    Map<String, Object> user = new HashMap<>();
    user.put("userId", userId);
    user.put("username", username);
    user.put("passwordHash", passwordHash);
    user.put("roleName", roleName);
    user.put("status", status);
    user.put("roleId", roleId);
    user.put("failedLoginAttempts", failedAttempts);
    user.put("fullName", "Test User");
    user.put("email", "test@test.com");
    user.put("lastLoginAt", null);
    return user;
  }

  // ─── login() ──────────────────────────────────────────────────────────────────

  @Test(expected = ValidationException.class)
  public void login_throwsValidationException_whenUsernameBlank() {
    authService.login(makeLoginRequest("", "pass", "Administrator"), "127.0.0.1", "agent");
  }

  @Test(expected = ValidationException.class)
  public void login_throwsValidationException_whenPasswordBlank() {
    authService.login(makeLoginRequest("admin", "", "Administrator"), "127.0.0.1", "agent");
  }

  @Test(expected = ValidationException.class)
  public void login_throwsValidationException_whenRoleBlank() {
    authService.login(makeLoginRequest("admin", "pass", ""), "127.0.0.1", "agent");
  }

  @Test(expected = ValidationException.class)
  public void login_throwsValidationException_whenRoleInvalid() {
    authService.login(makeLoginRequest("admin", "pass", "SuperUser"), "127.0.0.1", "agent");
  }

  @Test(expected = UnauthorizedException.class)
  public void login_throwsUnauthorized_whenUserNotFound() {
    when(userRepository.findByUsername("ghost")).thenReturn(null);
    authService.login(makeLoginRequest("ghost", "pass", "Administrator"), "127.0.0.1", "agent");
  }

  @Test(expected = ForbiddenException.class)
  public void login_throwsForbidden_whenAccountInactive() {
    Map<String, Object> user = makeUserMap(1L, "admin", "hash", "Administrator", "Inactive", 1L, 0);
    when(userRepository.findByUsername("admin")).thenReturn(user);
    authService.login(makeLoginRequest("admin", "pass", "Administrator"), "127.0.0.1", "agent");
  }

  @Test(expected = ForbiddenException.class)
  public void login_throwsForbidden_whenAccountLocked() {
    Map<String, Object> user = makeUserMap(1L, "admin", "hash", "Administrator", "Locked", 1L, 5);
    when(userRepository.findByUsername("admin")).thenReturn(user);
    authService.login(makeLoginRequest("admin", "pass", "Administrator"), "127.0.0.1", "agent");
  }

  @Test(expected = UnauthorizedException.class)
  public void login_throwsUnauthorized_whenPasswordWrong() {
    String hash = PasswordUtil.hash("correct-password");
    Map<String, Object> user = makeUserMap(1L, "admin", hash, "Administrator", "Active", 1L, 0);
    when(userRepository.findByUsername("admin")).thenReturn(user);

    authService.login(
        makeLoginRequest("admin", "wrong-password", "Administrator"), "127.0.0.1", "agent");
  }

  @Test
  public void login_incrementsFailedAttempts_whenPasswordWrong() {
    String hash = PasswordUtil.hash("correct-password");
    Map<String, Object> user = makeUserMap(1L, "admin", hash, "Administrator", "Active", 1L, 0);
    when(userRepository.findByUsername("admin")).thenReturn(user);

    try {
      authService.login(makeLoginRequest("admin", "wrong", "Administrator"), "127.0.0.1", "agent");
    } catch (UnauthorizedException ignored) {
    }

    verify(userRepository).incrementFailedAttempts(1L);
  }

  @Test(expected = ForbiddenException.class)
  public void login_locksAccount_afterMaxFailedAttempts() {
    String hash = PasswordUtil.hash("correct-password");
    Map<String, Object> user = makeUserMap(1L, "admin", hash, "Administrator", "Active", 1L, 4);
    when(userRepository.findByUsername("admin")).thenReturn(user);

    authService.login(makeLoginRequest("admin", "wrong", "Administrator"), "127.0.0.1", "agent");
  }

  @Test
  public void login_locksAccount_callsLockUser() {
    String hash = PasswordUtil.hash("correct-password");
    Map<String, Object> user = makeUserMap(1L, "admin", hash, "Administrator", "Active", 1L, 4);
    when(userRepository.findByUsername("admin")).thenReturn(user);

    try {
      authService.login(makeLoginRequest("admin", "wrong", "Administrator"), "127.0.0.1", "agent");
    } catch (ForbiddenException ignored) {
    }

    verify(userRepository).lockUser(1L);
  }

  @Test(expected = UnauthorizedException.class)
  public void login_throwsUnauthorized_whenRoleMismatch() {
    String hash = PasswordUtil.hash("Admin@123");
    Map<String, Object> user = makeUserMap(1L, "admin", hash, "Administrator", "Active", 1L, 0);
    when(userRepository.findByUsername("admin")).thenReturn(user);

    authService.login(makeLoginRequest("admin", "Admin@123", "Cashier"), "127.0.0.1", "agent");
  }

  @Test
  public void login_success_returnsLoginData() {
    String hash = PasswordUtil.hash("Admin@123");
    Map<String, Object> user = makeUserMap(1L, "admin", hash, "Administrator", "Active", 1L, 0);
    when(userRepository.findByUsername("admin")).thenReturn(user);
    List<String> perms = Arrays.asList("PRODUCT_READ", "PRODUCT_WRITE");
    when(userRepository.findPermissionsByRoleId(1L)).thenReturn(perms);

    LoginData result =
        authService.login(
            makeLoginRequest("admin", "Admin@123", "Administrator"), "127.0.0.1", "curl/7.0");

    assertNotNull(result);
    assertNotNull(result.getToken());
    assertEquals("Bearer", result.getTokenType());
    assertTrue(result.getExpiresInSeconds() > 0);
    assertNotNull(result.getUser());
    assertEquals("admin", result.getUser().getUsername());
    assertEquals("Administrator", result.getUser().getRole());
  }

  @Test
  public void login_success_resetsFailedAttempts() {
    String hash = PasswordUtil.hash("Admin@123");
    Map<String, Object> user = makeUserMap(1L, "admin", hash, "Administrator", "Active", 1L, 2);
    when(userRepository.findByUsername("admin")).thenReturn(user);
    when(userRepository.findPermissionsByRoleId(1L)).thenReturn(Arrays.asList("AUTH_LOGIN"));

    authService.login(
        makeLoginRequest("admin", "Admin@123", "Administrator"), "127.0.0.1", "agent");

    verify(userRepository).resetFailedAttempts(1L);
  }

  @Test
  public void login_success_updatesLastLogin() {
    String hash = PasswordUtil.hash("Admin@123");
    Map<String, Object> user = makeUserMap(1L, "admin", hash, "Administrator", "Active", 1L, 0);
    when(userRepository.findByUsername("admin")).thenReturn(user);
    when(userRepository.findPermissionsByRoleId(1L)).thenReturn(Arrays.asList("AUTH_LOGIN"));

    authService.login(
        makeLoginRequest("admin", "Admin@123", "Administrator"), "127.0.0.1", "agent");

    verify(userRepository).updateLastLogin(1L);
  }

  @Test
  public void login_success_createsSession() {
    String hash = PasswordUtil.hash("Admin@123");
    Map<String, Object> user = makeUserMap(1L, "admin", hash, "Administrator", "Active", 1L, 0);
    when(userRepository.findByUsername("admin")).thenReturn(user);
    when(userRepository.findPermissionsByRoleId(1L)).thenReturn(Arrays.asList("AUTH_LOGIN"));

    authService.login(
        makeLoginRequest("admin", "Admin@123", "Administrator"), "10.0.0.1", "TestAgent");

    ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> ipCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> uaCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Timestamp> expiryCaptor = ArgumentCaptor.forClass(Timestamp.class);

    verify(sessionRepository)
        .create(
            userIdCaptor.capture(),
            hashCaptor.capture(),
            ipCaptor.capture(),
            uaCaptor.capture(),
            expiryCaptor.capture());

    assertEquals(Long.valueOf(1L), userIdCaptor.getValue());
    assertEquals("10.0.0.1", ipCaptor.getValue());
    assertEquals("TestAgent", uaCaptor.getValue());
    assertNotNull(hashCaptor.getValue());
    assertEquals(64, hashCaptor.getValue().length());
    assertTrue(expiryCaptor.getValue().getTime() > System.currentTimeMillis());
  }

  // ─── logout() ─────────────────────────────────────────────────────────────────

  @Test(expected = UnauthorizedException.class)
  public void logout_throwsUnauthorized_forInvalidToken() {
    authService.logout("garbage-token");
  }

  @Test
  public void logout_invalidatesSession_whenFound() {
    String hash = PasswordUtil.hash("Admin@123");
    Map<String, Object> user = makeUserMap(1L, "admin", hash, "Administrator", "Active", 1L, 0);
    when(userRepository.findByUsername("admin")).thenReturn(user);
    when(userRepository.findPermissionsByRoleId(1L)).thenReturn(Arrays.asList("AUTH_LOGIN"));
    LoginData loginData =
        authService.login(
            makeLoginRequest("admin", "Admin@123", "Administrator"), "127.0.0.1", "agent");

    Map<String, Object> session = new HashMap<>();
    session.put("sessionId", 99L);
    when(sessionRepository.findActiveByTokenHash(anyString())).thenReturn(session);

    authService.logout(loginData.getToken());

    verify(sessionRepository).invalidate(99L, "Logged Out");
  }

  @Test
  public void logout_doesNotThrow_whenSessionNotFound() {
    String hash = PasswordUtil.hash("Admin@123");
    Map<String, Object> user = makeUserMap(1L, "admin", hash, "Administrator", "Active", 1L, 0);
    when(userRepository.findByUsername("admin")).thenReturn(user);
    when(userRepository.findPermissionsByRoleId(1L)).thenReturn(Arrays.asList("AUTH_LOGIN"));
    LoginData loginData =
        authService.login(
            makeLoginRequest("admin", "Admin@123", "Administrator"), "127.0.0.1", "agent");

    when(sessionRepository.findActiveByTokenHash(anyString())).thenReturn(null);

    authService.logout(loginData.getToken());

    verify(sessionRepository, never()).invalidate(anyLong(), anyString());
  }

  // ─── getCurrentUser() ─────────────────────────────────────────────────────────

  @Test(expected = UnauthorizedException.class)
  public void getCurrentUser_throwsUnauthorized_forInvalidToken() {
    authService.getCurrentUser("not-a-real-token");
  }

  @Test(expected = UnauthorizedException.class)
  public void getCurrentUser_throwsUnauthorized_whenUserNotFound() {
    String hash = PasswordUtil.hash("Admin@123");
    Map<String, Object> user = makeUserMap(1L, "admin", hash, "Administrator", "Active", 1L, 0);
    when(userRepository.findByUsername("admin")).thenReturn(user);
    when(userRepository.findPermissionsByRoleId(1L)).thenReturn(Arrays.asList("AUTH_LOGIN"));
    LoginData loginData =
        authService.login(
            makeLoginRequest("admin", "Admin@123", "Administrator"), "127.0.0.1", "agent");

    when(userRepository.findById(1L)).thenReturn(null);

    authService.getCurrentUser(loginData.getToken());
  }

  @Test
  public void getCurrentUser_returnsProfile_whenValid() {
    String hash = PasswordUtil.hash("Admin@123");
    Map<String, Object> user = makeUserMap(1L, "admin", hash, "Administrator", "Active", 1L, 0);
    when(userRepository.findByUsername("admin")).thenReturn(user);
    when(userRepository.findPermissionsByRoleId(1L)).thenReturn(Arrays.asList("AUTH_LOGIN"));
    LoginData loginData =
        authService.login(
            makeLoginRequest("admin", "Admin@123", "Administrator"), "127.0.0.1", "agent");

    Map<String, Object> foundUser = new HashMap<>(user);
    when(userRepository.findById(1L)).thenReturn(foundUser);
    UserProfile expectedProfile =
        new UserProfile(
            1L,
            "Test User",
            "admin",
            "test@test.com",
            "Administrator",
            "Active",
            null,
            Arrays.asList("AUTH_LOGIN"));
    when(userRepository.buildProfile(eq(foundUser), anyList())).thenReturn(expectedProfile);

    UserProfile result = authService.getCurrentUser(loginData.getToken());

    assertNotNull(result);
    assertEquals("admin", result.getUsername());
    assertEquals("Administrator", result.getRole());
  }

  // ─── validateSession() ────────────────────────────────────────────────────────

  @Test
  public void validateSession_returnsNull_forInvalidToken() {
    assertNull(authService.validateSession("garbage"));
  }

  @Test
  public void validateSession_returnsNull_whenNoActiveSession() {
    String hash = PasswordUtil.hash("Admin@123");
    Map<String, Object> user = makeUserMap(1L, "admin", hash, "Administrator", "Active", 1L, 0);
    when(userRepository.findByUsername("admin")).thenReturn(user);
    when(userRepository.findPermissionsByRoleId(1L)).thenReturn(Arrays.asList("AUTH_LOGIN"));
    LoginData loginData =
        authService.login(
            makeLoginRequest("admin", "Admin@123", "Administrator"), "127.0.0.1", "agent");

    when(sessionRepository.findActiveByTokenHash(anyString())).thenReturn(null);

    assertNull(authService.validateSession(loginData.getToken()));
  }

  @Test
  public void validateSession_returnsContext_whenSessionActive() {
    String hash = PasswordUtil.hash("Admin@123");
    Map<String, Object> user = makeUserMap(1L, "admin", hash, "Administrator", "Active", 1L, 0);
    when(userRepository.findByUsername("admin")).thenReturn(user);
    when(userRepository.findPermissionsByRoleId(1L)).thenReturn(Arrays.asList("AUTH_LOGIN"));
    LoginData loginData =
        authService.login(
            makeLoginRequest("admin", "Admin@123", "Administrator"), "127.0.0.1", "agent");

    Map<String, Object> session = new HashMap<>();
    session.put("sessionId", 42L);
    when(sessionRepository.findActiveByTokenHash(anyString())).thenReturn(session);

    Map<String, Object> ctx = authService.validateSession(loginData.getToken());

    assertNotNull(ctx);
    assertEquals(1L, ctx.get("userId"));
    assertEquals("Administrator", ctx.get("role"));
    assertEquals("admin", ctx.get("username"));
    assertEquals(42L, ctx.get("sessionId"));
  }

  @Test
  public void validateSession_updatesLastActivity() {
    String hash = PasswordUtil.hash("Admin@123");
    Map<String, Object> user = makeUserMap(1L, "admin", hash, "Administrator", "Active", 1L, 0);
    when(userRepository.findByUsername("admin")).thenReturn(user);
    when(userRepository.findPermissionsByRoleId(1L)).thenReturn(Arrays.asList("AUTH_LOGIN"));
    LoginData loginData =
        authService.login(
            makeLoginRequest("admin", "Admin@123", "Administrator"), "127.0.0.1", "agent");

    Map<String, Object> session = new HashMap<>();
    session.put("sessionId", 42L);
    when(sessionRepository.findActiveByTokenHash(anyString())).thenReturn(session);

    authService.validateSession(loginData.getToken());

    verify(sessionRepository).updateLastActivity(42L);
  }

  // ─── Role validation ──────────────────────────────────────────────────────────

  @Test
  public void login_acceptsAllValidRoles() {
    String[] roles = {"Administrator", "Manager", "Inventory Clerk", "Cashier"};
    for (String role : roles) {
      String hash = PasswordUtil.hash("Pass@123");
      Map<String, Object> user = makeUserMap(1L, "user1", hash, role, "Active", 1L, 0);
      when(userRepository.findByUsername("user1")).thenReturn(user);
      when(userRepository.findPermissionsByRoleId(1L)).thenReturn(Arrays.asList("AUTH_LOGIN"));

      LoginData result =
          authService.login(makeLoginRequest("user1", "Pass@123", role), "127.0.0.1", "agent");
      assertNotNull("Login should succeed for role: " + role, result);
    }
  }
}
