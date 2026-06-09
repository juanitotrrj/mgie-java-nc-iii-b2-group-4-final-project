package com.group4.inventoryserver.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.group4.inventoryserver.dto.PaginatedResponse;
import com.group4.inventoryserver.dto.PaginationParams;
import com.group4.inventoryserver.dto.icr.IcrApproveRequest;
import com.group4.inventoryserver.dto.icr.IcrCreateFields;
import com.group4.inventoryserver.dto.icr.IcrData;
import com.group4.inventoryserver.dto.icr.IcrDetailData;
import com.group4.inventoryserver.dto.icr.IcrRejectRequest;
import com.group4.inventoryserver.dto.icr.IcrSummaryData;
import com.group4.inventoryserver.exception.ConflictException;
import com.group4.inventoryserver.exception.NotFoundException;
import com.group4.inventoryserver.exception.ValidationException;
import com.group4.inventoryserver.repository.IcrRepository;
import com.group4.inventoryserver.server.RequestContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class IcrServiceTest {

  private IcrRepository icrRepository;
  private IcrService icrService;

  @Before
  public void setUp() {
    icrRepository = mock(IcrRepository.class);
    icrService = new IcrService(icrRepository);
  }

  private IcrData sampleIcr(String status, long requestedById) {
    return new IcrData(
        1L,
        "ICR-2026-000001",
        1L,
        "P001",
        "Test Product",
        "Adjustment",
        100,
        105,
        5,
        "Physical count discrepancy",
        status,
        requestedById,
        "Clerk User",
        "2026-06-01T10:00:00Z",
        null,
        null,
        null);
  }

  // ─── List Tests ─────────────────────────────────────────────────────────────

  @Test
  public void list_asReviewer_returnsAll() {
    when(icrRepository.findAll(0, 10, "requestedAt", "desc", null, null, null, null, null, null))
        .thenReturn(Collections.singletonList(sampleIcr("Pending", 2L)));
    when(icrRepository.count(null, null, null, null, null, null)).thenReturn(1L);

    RequestContext ctx = mock(RequestContext.class);
    when(ctx.getQueryParam("page")).thenReturn(null);
    when(ctx.getQueryParam("size")).thenReturn(null);
    when(ctx.getQueryParam("sortBy", "requestedAt")).thenReturn("requestedAt");
    when(ctx.getQueryParam("sortDir", "asc")).thenReturn("desc");
    when(ctx.getQueryParam("search")).thenReturn(null);
    PaginationParams params =
        PaginationParams.from(ctx, "requestedAt", new HashSet<>(Arrays.asList("requestedAt")));

    PaginatedResponse<IcrData> response =
        icrService.list(params, null, null, null, null, null, 1L, true);
    assertEquals(1, response.getData().size());
  }

  @Test
  public void list_asClerk_filtersToOwnRequests() {
    when(icrRepository.findAll(0, 10, "requestedAt", "desc", null, null, null, 2L, null, null))
        .thenReturn(Collections.singletonList(sampleIcr("Pending", 2L)));
    when(icrRepository.count(null, null, null, 2L, null, null)).thenReturn(1L);

    RequestContext ctx = mock(RequestContext.class);
    when(ctx.getQueryParam("page")).thenReturn(null);
    when(ctx.getQueryParam("size")).thenReturn(null);
    when(ctx.getQueryParam("sortBy", "requestedAt")).thenReturn("requestedAt");
    when(ctx.getQueryParam("sortDir", "asc")).thenReturn("desc");
    when(ctx.getQueryParam("search")).thenReturn(null);
    PaginationParams params =
        PaginationParams.from(ctx, "requestedAt", new HashSet<>(Arrays.asList("requestedAt")));

    PaginatedResponse<IcrData> response =
        icrService.list(params, null, null, null, null, null, 2L, false);
    assertEquals(1, response.getData().size());
  }

  // ─── GetById Tests ──────────────────────────────────────────────────────────

  @Test
  public void getById_asReviewer_returnsAny() {
    when(icrRepository.findById(1L)).thenReturn(sampleIcr("Pending", 2L));
    when(icrRepository.findFileByIcrId(1L)).thenReturn(null);
    IcrDetailData result = icrService.getById(1L, 1L, true);
    assertEquals("ICR-2026-000001", result.getRequestNo());
  }

  @Test(expected = NotFoundException.class)
  public void getById_asClerk_throwsNotFoundForOthersRequest() {
    when(icrRepository.findById(1L)).thenReturn(sampleIcr("Pending", 2L));
    icrService.getById(1L, 3L, false);
  }

  @Test
  public void getById_asClerk_returnsOwnRequest() {
    when(icrRepository.findById(1L)).thenReturn(sampleIcr("Pending", 2L));
    when(icrRepository.findFileByIcrId(1L)).thenReturn(null);
    IcrDetailData result = icrService.getById(1L, 2L, false);
    assertEquals("ICR-2026-000001", result.getRequestNo());
  }

  @Test(expected = NotFoundException.class)
  public void getById_throwsNotFound_whenMissing() {
    when(icrRepository.findById(99L)).thenReturn(null);
    icrService.getById(99L, 1L, true);
  }

  // ─── Create Tests ───────────────────────────────────────────────────────────

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenMissingFields() {
    IcrCreateFields fields = new IcrCreateFields(null, null, null, null, null);
    icrService.create(fields, null, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenInvalidRequestType() {
    IcrCreateFields fields = new IcrCreateFields(1L, "Invalid", null, 5, "reason");
    icrService.create(fields, null, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenNoQuantityProvided() {
    IcrCreateFields fields = new IcrCreateFields(1L, "Adjustment", null, null, "reason");
    icrService.create(fields, null, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenInvalidMimeType() {
    IcrCreateFields fields = new IcrCreateFields(1L, "Adjustment", null, 5, "reason");
    com.group4.inventoryserver.util.FileUpload badFile =
        new com.group4.inventoryserver.util.FileUpload("doc.pdf", "application/pdf", new byte[10]);
    icrService.create(fields, badFile, 1L);
  }

  // ─── Approve Tests ──────────────────────────────────────────────────────────

  @Test(expected = NotFoundException.class)
  public void approve_throwsNotFound_whenMissing() {
    when(icrRepository.findById(99L)).thenReturn(null);
    icrService.approve(99L, new IcrApproveRequest(), 1L);
  }

  @Test(expected = ConflictException.class)
  public void approve_throwsConflict_whenAlreadyApproved() {
    when(icrRepository.findById(1L)).thenReturn(sampleIcr("Approved", 2L));
    icrService.approve(1L, new IcrApproveRequest(), 1L);
  }

  @Test(expected = ConflictException.class)
  public void approve_throwsConflict_whenAlreadyRejected() {
    when(icrRepository.findById(1L)).thenReturn(sampleIcr("Rejected", 2L));
    icrService.approve(1L, new IcrApproveRequest(), 1L);
  }

  // ─── Reject Tests ───────────────────────────────────────────────────────────

  @Test(expected = ValidationException.class)
  public void reject_throwsValidation_whenNoReason() {
    IcrRejectRequest request = new IcrRejectRequest();
    icrService.reject(1L, request, 1L);
  }

  @Test(expected = ValidationException.class)
  public void reject_throwsValidation_whenBlankReason() {
    IcrRejectRequest request = new IcrRejectRequest();
    request.setReason("   ");
    icrService.reject(1L, request, 1L);
  }

  @Test(expected = NotFoundException.class)
  public void reject_throwsNotFound_whenMissing() {
    IcrRejectRequest request = new IcrRejectRequest();
    request.setReason("Not justified");
    when(icrRepository.findById(99L)).thenReturn(null);
    icrService.reject(99L, request, 1L);
  }

  @Test(expected = ConflictException.class)
  public void reject_throwsConflict_whenNotPending() {
    IcrRejectRequest request = new IcrRejectRequest();
    request.setReason("Not justified");
    when(icrRepository.findById(1L)).thenReturn(sampleIcr("Approved", 2L));
    icrService.reject(1L, request, 1L);
  }

  @Test
  public void reject_success() {
    IcrRejectRequest request = new IcrRejectRequest();
    request.setReason("Insufficient evidence");
    when(icrRepository.findById(1L)).thenReturn(sampleIcr("Pending", 2L));
    icrService.reject(1L, request, 1L);
    verify(icrRepository).reject(1L, 1L, "Insufficient evidence");
  }

  // ─── Summary Tests ──────────────────────────────────────────────────────────

  @Test
  public void getMySummary_returnsCounts() {
    IcrSummaryData expected = new IcrSummaryData(3, 5, 2);
    when(icrRepository.countByStatus(2L)).thenReturn(expected);
    IcrSummaryData result = icrService.getMySummary(2L);
    assertEquals(3, result.getPending());
    assertEquals(5, result.getApproved());
    assertEquals(2, result.getRejected());
  }

  // ─── GetProofFile Tests ─────────────────────────────────────────────────────

  @Test(expected = NotFoundException.class)
  public void getProofFile_throwsNotFound_whenNoIcr() {
    when(icrRepository.findById(99L)).thenReturn(null);
    icrService.getProofFile(99L, 1L, true);
  }

  @Test(expected = NotFoundException.class)
  public void getProofFile_throwsNotFound_whenClerkNotOwner() {
    when(icrRepository.findById(1L)).thenReturn(sampleIcr("Pending", 2L));
    icrService.getProofFile(1L, 3L, false);
  }

  @Test(expected = NotFoundException.class)
  public void getProofFile_throwsNotFound_whenNoFile() {
    when(icrRepository.findById(1L)).thenReturn(sampleIcr("Pending", 2L));
    when(icrRepository.findFileByIcrId(1L)).thenReturn(null);
    icrService.getProofFile(1L, 1L, true);
  }

  @Test
  public void getProofFile_returnsFileInfo() {
    when(icrRepository.findById(1L)).thenReturn(sampleIcr("Pending", 2L));
    Map<String, Object> fileInfo = new HashMap<>();
    fileInfo.put("fileId", 1L);
    fileInfo.put("originalFilename", "photo.jpg");
    fileInfo.put("storagePath", "/tmp/photo.jpg");
    fileInfo.put("mimeType", "image/jpeg");
    fileInfo.put("fileSizeBytes", 12345L);
    when(icrRepository.findFileByIcrId(1L)).thenReturn(fileInfo);

    Map<String, Object> result = icrService.getProofFile(1L, 1L, true);
    assertEquals("photo.jpg", result.get("originalFilename"));
    assertEquals("image/jpeg", result.get("mimeType"));
  }
}
