package com.group4.inventoryserver.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.group4.inventoryserver.dto.PaginatedResponse;
import com.group4.inventoryserver.dto.PaginationParams;
import com.group4.inventoryserver.dto.user.ResetPasswordRequest;
import com.group4.inventoryserver.dto.user.UserCreateRequest;
import com.group4.inventoryserver.dto.user.UserData;
import com.group4.inventoryserver.dto.user.UserUpdateRequest;
import com.group4.inventoryserver.exception.ConflictException;
import com.group4.inventoryserver.exception.NotFoundException;
import com.group4.inventoryserver.exception.ValidationException;
import com.group4.inventoryserver.repository.UserRepository;
import com.group4.inventoryserver.server.RequestContext;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class UserServiceTest {

  private UserRepository userRepository;
  private UserService userService;

  @Before
  public void setUp() {
    userRepository = mock(UserRepository.class);
    userService = new UserService(userRepository);
  }

  private UserData sampleUser() {
    return new UserData(
        1L,
        "U001",
        "John Doe",
        "johndoe",
        "john@example.com",
        "Administrator",
        "Active",
        null,
        "2026-05-20T02:30:00Z",
        "2026-05-20T02:30:00Z");
  }

  private UserData sampleUser(long id, String code) {
    return new UserData(
        id,
        code,
        "John Doe",
        "johndoe",
        "john@example.com",
        "Administrator",
        "Active",
        null,
        "2026-05-20T02:30:00Z",
        "2026-05-20T02:30:00Z");
  }

  // ─── List Tests ─────────────────────────────────────────────────────────────

  @Test
  public void list_returnsPaginatedUsers() {
    List<UserData> users = Arrays.asList(sampleUser(), sampleUser(2L, "U002"));
    when(userRepository.findAll(0, 10, "createdAt", "desc", null, null, null)).thenReturn(users);
    when(userRepository.count(null, null, null)).thenReturn(2L);

    RequestContext ctx = mock(RequestContext.class);
    when(ctx.getQueryParam("page")).thenReturn(null);
    when(ctx.getQueryParam("size")).thenReturn(null);
    when(ctx.getQueryParam("sortBy", "createdAt")).thenReturn("createdAt");
    when(ctx.getQueryParam("sortDir", "asc")).thenReturn("desc");
    when(ctx.getQueryParam("search")).thenReturn(null);
    PaginationParams params =
        PaginationParams.from(ctx, "createdAt", new HashSet<>(Arrays.asList("createdAt")));

    PaginatedResponse<UserData> response = userService.list(params, null, null);

    assertEquals(2, response.getData().size());
    assertEquals(2L, response.getMeta().getTotalRecords());
  }

  // ─── GetById Tests ──────────────────────────────────────────────────────────

  @Test
  public void getById_returnsUser_whenExists() {
    when(userRepository.findDetailById(1L)).thenReturn(sampleUser());
    UserData result = userService.getById(1L);
    assertEquals("U001", result.getUserCode());
  }

  @Test(expected = NotFoundException.class)
  public void getById_throwsNotFound_whenMissing() {
    when(userRepository.findDetailById(99L)).thenReturn(null);
    userService.getById(99L);
  }

  // ─── Create Tests ───────────────────────────────────────────────────────────

  @Test
  public void create_success() {
    UserCreateRequest req = new UserCreateRequest();
    req.setFullName("Jane Smith");
    req.setUsername("janesmith");
    req.setEmail("jane@example.com");
    req.setRole("Manager");
    req.setPassword("Str0ng@Pass!");

    when(userRepository.existsByUsername("janesmith")).thenReturn(false);
    when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
    when(userRepository.getRoleIdByName("Manager")).thenReturn(2L);
    when(userRepository.generateNextUserCode()).thenReturn("U005");
    when(userRepository.insert(
            eq("U005"),
            eq("Jane Smith"),
            eq("janesmith"),
            eq("jane@example.com"),
            anyString(),
            eq(2L),
            eq("Active"),
            eq(10L)))
        .thenReturn(5L);
    when(userRepository.findDetailById(5L))
        .thenReturn(
            new UserData(
                5L,
                "U005",
                "Jane Smith",
                "janesmith",
                "jane@example.com",
                "Manager",
                "Active",
                null,
                "2026-05-20T02:30:00Z",
                "2026-05-20T02:30:00Z"));

    UserData result = userService.create(req, 10L);
    assertEquals("U005", result.getUserCode());
    assertEquals("Jane Smith", result.getFullName());
    verify(userRepository)
        .insert(
            eq("U005"),
            eq("Jane Smith"),
            eq("janesmith"),
            eq("jane@example.com"),
            anyString(),
            eq(2L),
            eq("Active"),
            eq(10L));
  }

  @Test(expected = ConflictException.class)
  public void create_throwsConflict_whenUsernameDuplicate() {
    UserCreateRequest req = new UserCreateRequest();
    req.setFullName("Jane Smith");
    req.setUsername("existing");
    req.setEmail("jane@example.com");
    req.setRole("Manager");
    req.setPassword("Str0ng@Pass!");

    when(userRepository.existsByUsername("existing")).thenReturn(true);
    userService.create(req, 10L);
  }

  @Test(expected = ConflictException.class)
  public void create_throwsConflict_whenEmailDuplicate() {
    UserCreateRequest req = new UserCreateRequest();
    req.setFullName("Jane Smith");
    req.setUsername("janesmith");
    req.setEmail("taken@example.com");
    req.setRole("Manager");
    req.setPassword("Str0ng@Pass!");

    when(userRepository.existsByUsername("janesmith")).thenReturn(false);
    when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);
    userService.create(req, 10L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenMissingRequiredFields() {
    UserCreateRequest req = new UserCreateRequest();
    userService.create(req, 10L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenInvalidRole() {
    UserCreateRequest req = new UserCreateRequest();
    req.setFullName("Jane Smith");
    req.setUsername("janesmith");
    req.setEmail("jane@example.com");
    req.setRole("Nonexistent Role");
    req.setPassword("Str0ng@Pass!");

    userService.create(req, 10L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenWeakPassword() {
    UserCreateRequest req = new UserCreateRequest();
    req.setFullName("Jane Smith");
    req.setUsername("janesmith");
    req.setEmail("jane@example.com");
    req.setRole("Manager");
    req.setPassword("weak");

    userService.create(req, 10L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenPasswordMissingSpecialChar() {
    UserCreateRequest req = new UserCreateRequest();
    req.setFullName("Jane Smith");
    req.setUsername("janesmith");
    req.setEmail("jane@example.com");
    req.setRole("Manager");
    req.setPassword("NoSpecial1");

    userService.create(req, 10L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenInvalidEmail() {
    UserCreateRequest req = new UserCreateRequest();
    req.setFullName("Jane Smith");
    req.setUsername("janesmith");
    req.setEmail("not-an-email");
    req.setRole("Manager");
    req.setPassword("Str0ng@Pass!");

    userService.create(req, 10L);
  }

  // ─── Update Tests ───────────────────────────────────────────────────────────

  @Test
  public void update_success() {
    UserData existing = sampleUser();
    when(userRepository.findDetailById(1L)).thenReturn(existing);
    when(userRepository.existsByEmailExcluding("new@example.com", 1L)).thenReturn(false);
    when(userRepository.getRoleIdByName("Manager")).thenReturn(2L);

    UserUpdateRequest req = new UserUpdateRequest();
    req.setFullName("Updated Name");
    req.setEmail("new@example.com");
    req.setRole("Manager");

    UserData updated =
        new UserData(
            1L,
            "U001",
            "Updated Name",
            "johndoe",
            "new@example.com",
            "Manager",
            "Active",
            null,
            "2026-05-20T02:30:00Z",
            "2026-05-21T02:30:00Z");
    when(userRepository.findDetailById(1L)).thenReturn(existing).thenReturn(updated);

    UserData result = userService.update(1L, req, 10L);
    assertEquals("Updated Name", result.getFullName());
    verify(userRepository)
        .update(eq(1L), eq("Updated Name"), eq("new@example.com"), eq(2L), eq("Active"), eq(10L));
  }

  @Test(expected = NotFoundException.class)
  public void update_throwsNotFound_whenUserMissing() {
    when(userRepository.findDetailById(99L)).thenReturn(null);
    UserUpdateRequest req = new UserUpdateRequest();
    req.setFullName("Nope");
    userService.update(99L, req, 10L);
  }

  @Test(expected = ConflictException.class)
  public void update_throwsConflict_whenEmailTaken() {
    UserData existing = sampleUser();
    when(userRepository.findDetailById(1L)).thenReturn(existing);
    when(userRepository.existsByEmailExcluding("taken@example.com", 1L)).thenReturn(true);
    when(userRepository.getRoleIdByName("Administrator")).thenReturn(1L);

    UserUpdateRequest req = new UserUpdateRequest();
    req.setEmail("taken@example.com");

    userService.update(1L, req, 10L);
  }

  // ─── Deactivate Tests ───────────────────────────────────────────────────────

  @Test
  public void deactivate_success() {
    when(userRepository.findDetailById(2L)).thenReturn(sampleUser(2L, "U002"));
    userService.deactivate(2L, 1L);
    verify(userRepository).deactivate(2L, 1L);
    verify(userRepository).invalidateSessions(2L);
  }

  @Test(expected = NotFoundException.class)
  public void deactivate_throwsNotFound_whenUserMissing() {
    when(userRepository.findDetailById(99L)).thenReturn(null);
    userService.deactivate(99L, 1L);
  }

  @Test(expected = ConflictException.class)
  public void deactivate_throwsConflict_whenSelfDeactivation() {
    when(userRepository.findDetailById(1L)).thenReturn(sampleUser());
    userService.deactivate(1L, 1L);
  }

  // ─── ResetPassword Tests ────────────────────────────────────────────────────

  @Test
  public void resetPassword_success() {
    when(userRepository.findDetailById(1L)).thenReturn(sampleUser());

    ResetPasswordRequest req = new ResetPasswordRequest();
    req.setNewPassword("N3w@Secure!");
    req.setForceChangeOnNextLogin(true);

    userService.resetPassword(1L, req, 10L);
    verify(userRepository).resetPassword(eq(1L), anyString(), eq(true));
  }

  @Test(expected = NotFoundException.class)
  public void resetPassword_throwsNotFound_whenUserMissing() {
    when(userRepository.findDetailById(99L)).thenReturn(null);
    ResetPasswordRequest req = new ResetPasswordRequest();
    req.setNewPassword("N3w@Secure!");
    userService.resetPassword(99L, req, 10L);
  }

  @Test(expected = ValidationException.class)
  public void resetPassword_throwsValidation_whenWeakPassword() {
    when(userRepository.findDetailById(1L)).thenReturn(sampleUser());
    ResetPasswordRequest req = new ResetPasswordRequest();
    req.setNewPassword("short");
    userService.resetPassword(1L, req, 10L);
  }

  @Test(expected = ValidationException.class)
  public void resetPassword_throwsValidation_whenBlankPassword() {
    when(userRepository.findDetailById(1L)).thenReturn(sampleUser());
    ResetPasswordRequest req = new ResetPasswordRequest();
    req.setNewPassword("");
    userService.resetPassword(1L, req, 10L);
  }

  @Test
  public void resetPassword_defaultsForceChangeToFalse() {
    when(userRepository.findDetailById(1L)).thenReturn(sampleUser());
    ResetPasswordRequest req = new ResetPasswordRequest();
    req.setNewPassword("N3w@Secure!");

    userService.resetPassword(1L, req, 10L);
    verify(userRepository).resetPassword(eq(1L), anyString(), eq(false));
  }
}
