package com.group4.inventoryserver.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.group4.inventoryserver.dto.PaginatedResponse;
import com.group4.inventoryserver.dto.audit.AuditLogData;
import com.group4.inventoryserver.repository.AuditLogRepository;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class AuditServiceTest {

  private AuditLogRepository auditLogRepository;
  private AuditService auditService;

  @Before
  public void setUp() {
    auditLogRepository = mock(AuditLogRepository.class);
    auditService = new AuditService(auditLogRepository);
  }

  @Test
  public void logAction_insertsAuditRecord() {
    auditService.logAction(
        1L, "admin", "CREATE", "products", "P001", "Created product", "127.0.0.1");

    verify(auditLogRepository)
        .insert(1L, "admin", "CREATE", "products", "P001", "Created product", "127.0.0.1");
  }

  @Test
  public void logAction_withoutIp_passesNullIp() {
    auditService.logAction(2L, "clerk", "UPDATE", "sales", "S001", "Updated sale");

    verify(auditLogRepository).insert(2L, "clerk", "UPDATE", "sales", "S001", "Updated sale", null);
  }

  @Test
  public void logAction_swallowsRepositoryException() {
    doThrow(new RuntimeException("DB down"))
        .when(auditLogRepository)
        .insert(any(), any(), any(), any(), any(), any(), any());

    auditService.logAction(1L, "admin", "DELETE", "users", "U1", "Deleted user", "10.0.0.1");
  }

  @Test
  public void list_returnsPaginatedResponse() {
    AuditLogData entry =
        new AuditLogData(1L, "2026-01-01T00:00:00Z", 1L, "admin", "LOGIN", "auth", null, null);
    when(auditLogRepository.findAll(0, 10, "createdAt", "desc", null, null, null, null, null, null))
        .thenReturn(Collections.singletonList(entry));
    when(auditLogRepository.count(null, null, null, null, null, null)).thenReturn(1L);

    PaginatedResponse<AuditLogData> response =
        auditService.list(1, 10, "createdAt", "desc", null, null, null, null, null, null);

    assertTrue(response.isSuccess());
    assertEquals(1, response.getData().size());
    assertEquals(1L, response.getMeta().getTotalRecords());
    assertEquals(1, response.getMeta().getTotalPages());
    assertEquals("admin", response.getData().get(0).getUsername());
  }

  @Test
  public void list_passesFiltersToRepository() {
    when(auditLogRepository.findAll(
            10, 5, "username", "asc", "login", 3L, "LOGIN", "auth", "2026-01-01", "2026-01-31"))
        .thenReturn(Collections.emptyList());
    when(auditLogRepository.count("login", 3L, "LOGIN", "auth", "2026-01-01", "2026-01-31"))
        .thenReturn(0L);

    auditService.list(
        3, 5, "username", "asc", "login", 3L, "LOGIN", "auth", "2026-01-01", "2026-01-31");

    verify(auditLogRepository)
        .findAll(
            10, 5, "username", "asc", "login", 3L, "LOGIN", "auth", "2026-01-01", "2026-01-31");
  }

  @Test
  public void logAction_capturesAllFields() {
    auditService.logAction(
        99L, "manager", "EXPORT", "reports", "RPT-1", "Exported CSV", "192.168.1.1");

    ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<String> actionCaptor = ArgumentCaptor.forClass(String.class);
    verify(auditLogRepository)
        .insert(
            userIdCaptor.capture(),
            eq("manager"),
            actionCaptor.capture(),
            eq("reports"),
            eq("RPT-1"),
            eq("Exported CSV"),
            eq("192.168.1.1"));

    assertEquals(Long.valueOf(99L), userIdCaptor.getValue());
    assertEquals("EXPORT", actionCaptor.getValue());
  }
}
