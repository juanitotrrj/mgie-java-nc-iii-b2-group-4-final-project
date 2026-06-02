package com.group4.inventoryserver.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.group4.inventoryserver.dto.PaginatedResponse;
import com.group4.inventoryserver.dto.PaginationParams;
import com.group4.inventoryserver.dto.supplier.SupplierCreateRequest;
import com.group4.inventoryserver.dto.supplier.SupplierData;
import com.group4.inventoryserver.dto.supplier.SupplierUpdateRequest;
import com.group4.inventoryserver.exception.ConflictException;
import com.group4.inventoryserver.exception.NotFoundException;
import com.group4.inventoryserver.exception.ValidationException;
import com.group4.inventoryserver.repository.SupplierRepository;
import com.group4.inventoryserver.server.RequestContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class SupplierServiceTest {

  private SupplierRepository supplierRepository;
  private SupplierService supplierService;

  @Before
  public void setUp() {
    supplierRepository = mock(SupplierRepository.class);
    supplierService = new SupplierService(supplierRepository);
  }

  private SupplierData sampleSupplier() {
    return new SupplierData(
        1L,
        "SUP001",
        "Tech Solutions Inc.",
        "Robert Garcia",
        "(02) 8123-1111",
        "robert.garcia@techsolutions.com",
        "Makati City, Metro Manila",
        "Local",
        true,
        3,
        "Active",
        "2026-01-01T00:00:00Z",
        "2026-01-01T00:00:00Z");
  }

  private SupplierCreateRequest validCreateRequest() {
    SupplierCreateRequest req = new SupplierCreateRequest();
    req.setSupplierName("New Supplier");
    req.setContactPerson("John Doe");
    req.setEmail("john@example.com");
    return req;
  }

  // ─── create() ─────────────────────────────────────────────────────────────────

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenNameBlank() {
    SupplierCreateRequest req = new SupplierCreateRequest();
    req.setSupplierName("");
    supplierService.create(req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenNameNull() {
    SupplierCreateRequest req = new SupplierCreateRequest();
    supplierService.create(req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenNameTooLong() {
    SupplierCreateRequest req = new SupplierCreateRequest();
    StringBuilder longName = new StringBuilder();
    for (int i = 0; i < 160; i++) longName.append('a');
    req.setSupplierName(longName.toString());
    supplierService.create(req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenEmailInvalid() {
    SupplierCreateRequest req = validCreateRequest();
    req.setEmail("not-an-email");
    supplierService.create(req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenTypeInvalid() {
    SupplierCreateRequest req = validCreateRequest();
    req.setType("Unknown");
    supplierService.create(req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenStatusInvalid() {
    SupplierCreateRequest req = validCreateRequest();
    req.setStatus("Suspended");
    supplierService.create(req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenAddressTooLong() {
    SupplierCreateRequest req = validCreateRequest();
    StringBuilder longAddr = new StringBuilder();
    for (int i = 0; i < 510; i++) longAddr.append('a');
    req.setAddress(longAddr.toString());
    supplierService.create(req, 1L);
  }

  @Test(expected = ConflictException.class)
  public void create_throwsConflict_whenNameExists() {
    SupplierCreateRequest req = validCreateRequest();
    when(supplierRepository.existsByName("New Supplier")).thenReturn(true);
    supplierService.create(req, 1L);
  }

  @Test
  public void create_success_insertsAndReturnsData() {
    SupplierCreateRequest req = validCreateRequest();
    req.setType("International");
    req.setPreferred(true);
    when(supplierRepository.existsByName("New Supplier")).thenReturn(false);
    when(supplierRepository.generateNextCode()).thenReturn("SUP003");
    when(supplierRepository.insert(
            eq("SUP003"),
            eq("New Supplier"),
            eq("John Doe"),
            isNull(),
            eq("john@example.com"),
            isNull(),
            eq("International"),
            eq(true),
            eq("Active"),
            eq(1L)))
        .thenReturn(3L);
    when(supplierRepository.findById(3L))
        .thenReturn(
            new SupplierData(
                3L,
                "SUP003",
                "New Supplier",
                "John Doe",
                null,
                "john@example.com",
                null,
                "International",
                true,
                0,
                "Active",
                "2026-01-01T00:00:00Z",
                "2026-01-01T00:00:00Z"));

    SupplierData result = supplierService.create(req, 1L);
    assertNotNull(result);
    assertEquals("SUP003", result.getSupplierCode());
    assertEquals("New Supplier", result.getSupplierName());
    assertTrue(result.isPreferred());
  }

  @Test
  public void create_usesDefaultTypeAndStatus_whenNotProvided() {
    SupplierCreateRequest req = new SupplierCreateRequest();
    req.setSupplierName("Minimal Supplier");
    when(supplierRepository.existsByName("Minimal Supplier")).thenReturn(false);
    when(supplierRepository.generateNextCode()).thenReturn("SUP003");
    when(supplierRepository.insert(
            anyString(),
            anyString(),
            any(),
            any(),
            any(),
            any(),
            eq("Local"),
            eq(false),
            eq("Active"),
            anyLong()))
        .thenReturn(3L);
    when(supplierRepository.findById(3L)).thenReturn(sampleSupplier());

    supplierService.create(req, 1L);

    verify(supplierRepository)
        .insert(
            anyString(),
            anyString(),
            any(),
            any(),
            any(),
            any(),
            eq("Local"),
            eq(false),
            eq("Active"),
            anyLong());
  }

  // ─── getById() ────────────────────────────────────────────────────────────────

  @Test
  public void getById_returnsSupplier_whenExists() {
    when(supplierRepository.findById(1L)).thenReturn(sampleSupplier());
    SupplierData result = supplierService.getById(1L);
    assertNotNull(result);
    assertEquals("SUP001", result.getSupplierCode());
  }

  @Test(expected = NotFoundException.class)
  public void getById_throwsNotFound_whenMissing() {
    when(supplierRepository.findById(99L)).thenReturn(null);
    supplierService.getById(99L);
  }

  // ─── update() ─────────────────────────────────────────────────────────────────

  @Test(expected = NotFoundException.class)
  public void update_throwsNotFound_whenSupplierMissing() {
    when(supplierRepository.findById(99L)).thenReturn(null);
    supplierService.update(99L, new SupplierUpdateRequest(), 1L);
  }

  @Test(expected = ConflictException.class)
  public void update_throwsConflict_whenNameAlreadyUsed() {
    when(supplierRepository.findById(1L)).thenReturn(sampleSupplier());
    SupplierUpdateRequest req = new SupplierUpdateRequest();
    req.setSupplierName("Existing Name");
    when(supplierRepository.existsByNameExcluding("Existing Name", 1L)).thenReturn(true);
    supplierService.update(1L, req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void update_throwsValidation_whenEmailInvalid() {
    when(supplierRepository.findById(1L)).thenReturn(sampleSupplier());
    SupplierUpdateRequest req = new SupplierUpdateRequest();
    req.setEmail("bad-email");
    supplierService.update(1L, req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void update_throwsValidation_whenTypeInvalid() {
    when(supplierRepository.findById(1L)).thenReturn(sampleSupplier());
    SupplierUpdateRequest req = new SupplierUpdateRequest();
    req.setType("Unknown");
    supplierService.update(1L, req, 1L);
  }

  @Test
  public void update_success_updatesAndReturnsSupplier() {
    when(supplierRepository.findById(1L)).thenReturn(sampleSupplier());
    SupplierUpdateRequest req = new SupplierUpdateRequest();
    req.setSupplierName("Updated Name");
    req.setPreferred(false);
    when(supplierRepository.existsByNameExcluding("Updated Name", 1L)).thenReturn(false);
    when(supplierRepository.findById(1L))
        .thenReturn(
            new SupplierData(
                1L,
                "SUP001",
                "Updated Name",
                "Robert Garcia",
                "(02) 8123-1111",
                "robert.garcia@techsolutions.com",
                "Makati City, Metro Manila",
                "Local",
                false,
                3,
                "Active",
                "2026-01-01T00:00:00Z",
                "2026-01-02T00:00:00Z"));

    SupplierData result = supplierService.update(1L, req, 1L);
    assertNotNull(result);
    verify(supplierRepository)
        .update(
            eq(1L),
            eq("Updated Name"),
            isNull(),
            isNull(),
            isNull(),
            isNull(),
            isNull(),
            eq(false),
            isNull(),
            eq(1L));
  }

  @Test
  public void update_doesNotCheckNameConflict_whenNameNotProvided() {
    when(supplierRepository.findById(1L)).thenReturn(sampleSupplier());
    SupplierUpdateRequest req = new SupplierUpdateRequest();
    req.setContactPerson("New Contact");
    when(supplierRepository.findById(1L)).thenReturn(sampleSupplier());

    supplierService.update(1L, req, 1L);

    verify(supplierRepository, never()).existsByNameExcluding(anyString(), anyLong());
  }

  // ─── deactivate() ─────────────────────────────────────────────────────────────

  @Test(expected = NotFoundException.class)
  public void deactivate_throwsNotFound_whenMissing() {
    when(supplierRepository.findById(99L)).thenReturn(null);
    supplierService.deactivate(99L, 1L);
  }

  @Test
  public void deactivate_success() {
    when(supplierRepository.findById(1L)).thenReturn(sampleSupplier());
    supplierService.deactivate(1L, 1L);
    verify(supplierRepository).deactivate(1L, 1L);
  }

  // ─── list() ───────────────────────────────────────────────────────────────────

  @Test
  public void list_returnsPaginatedResponse() {
    List<SupplierData> suppliers = Arrays.asList(sampleSupplier());
    when(supplierRepository.findAll(0, 10, "supplierName", "asc", null, null, null))
        .thenReturn(suppliers);
    when(supplierRepository.count(null, null, null)).thenReturn(1L);

    RequestContext ctx = mock(RequestContext.class);
    when(ctx.getQueryParam("page")).thenReturn(null);
    when(ctx.getQueryParam("size")).thenReturn(null);
    when(ctx.getQueryParam("sortBy", "supplierName")).thenReturn("supplierName");
    when(ctx.getQueryParam("sortDir", "asc")).thenReturn("asc");
    when(ctx.getQueryParam("search")).thenReturn(null);
    PaginationParams params =
        PaginationParams.from(ctx, "supplierName", new HashSet<>(Arrays.asList("supplierName")));

    PaginatedResponse<SupplierData> result = supplierService.list(params, null, null);
    assertNotNull(result);
    assertTrue(result.isSuccess());
    assertEquals(1, result.getData().size());
    assertEquals(1L, result.getMeta().getTotalRecords());
  }

  @Test
  public void list_withTypeFilter() {
    when(supplierRepository.findAll(0, 10, "supplierName", "asc", null, null, "Local"))
        .thenReturn(Collections.singletonList(sampleSupplier()));
    when(supplierRepository.count(null, null, "Local")).thenReturn(1L);

    RequestContext ctx = mock(RequestContext.class);
    when(ctx.getQueryParam("page")).thenReturn(null);
    when(ctx.getQueryParam("size")).thenReturn(null);
    when(ctx.getQueryParam("sortBy", "supplierName")).thenReturn("supplierName");
    when(ctx.getQueryParam("sortDir", "asc")).thenReturn("asc");
    when(ctx.getQueryParam("search")).thenReturn(null);
    PaginationParams params =
        PaginationParams.from(ctx, "supplierName", new HashSet<>(Arrays.asList("supplierName")));

    PaginatedResponse<SupplierData> result = supplierService.list(params, null, "Local");
    assertNotNull(result);
    assertEquals(1, result.getData().size());
  }
}
