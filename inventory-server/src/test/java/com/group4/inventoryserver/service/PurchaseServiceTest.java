package com.group4.inventoryserver.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.group4.inventoryserver.dto.PaginatedResponse;
import com.group4.inventoryserver.dto.PaginationParams;
import com.group4.inventoryserver.dto.purchase.CancelPurchaseRequest;
import com.group4.inventoryserver.dto.purchase.PurchaseCreateRequest;
import com.group4.inventoryserver.dto.purchase.PurchaseData;
import com.group4.inventoryserver.dto.purchase.PurchaseDetailData;
import com.group4.inventoryserver.dto.purchase.PurchaseItemData;
import com.group4.inventoryserver.dto.purchase.PurchaseItemRequest;
import com.group4.inventoryserver.dto.purchase.PurchaseUpdateRequest;
import com.group4.inventoryserver.exception.ConflictException;
import com.group4.inventoryserver.exception.NotFoundException;
import com.group4.inventoryserver.exception.ValidationException;
import com.group4.inventoryserver.repository.PurchaseRepository;
import com.group4.inventoryserver.repository.StockMovementRepository;
import com.group4.inventoryserver.server.RequestContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class PurchaseServiceTest {

  private PurchaseRepository purchaseRepository;
  private StockMovementRepository stockMovementRepository;
  private PurchaseService purchaseService;

  @Before
  public void setUp() {
    purchaseRepository = mock(PurchaseRepository.class);
    stockMovementRepository = mock(StockMovementRepository.class);
    purchaseService = new PurchaseService(purchaseRepository, stockMovementRepository);
  }

  private PurchaseData samplePurchase(String status) {
    return new PurchaseData(
        1L,
        "PO-2026-00001",
        1L,
        "Tech Solutions Inc.",
        "2026-05-15",
        "2026-05-22",
        null,
        2,
        17000.0,
        status,
        "Test order",
        null,
        "2026-05-15T00:00:00Z",
        "2026-05-15T00:00:00Z");
  }

  private PurchaseItemData sampleItem() {
    return new PurchaseItemData(1L, 1L, "P001", "Keyboard", 20, 850.0, 17000.0);
  }

  private PurchaseCreateRequest validCreateRequest() {
    PurchaseCreateRequest req = new PurchaseCreateRequest();
    req.setSupplierId(1L);
    req.setOrderDate("2026-06-01");
    PurchaseItemRequest item = new PurchaseItemRequest();
    item.setProductId(1L);
    item.setQuantity(10);
    item.setUnitCost(100.0);
    req.setItems(Collections.singletonList(item));
    return req;
  }

  private PurchaseUpdateRequest validUpdateRequest() {
    PurchaseUpdateRequest req = new PurchaseUpdateRequest();
    req.setSupplierId(1L);
    req.setOrderDate("2026-06-01");
    PurchaseItemRequest item = new PurchaseItemRequest();
    item.setProductId(1L);
    item.setQuantity(5);
    item.setUnitCost(200.0);
    req.setItems(Collections.singletonList(item));
    return req;
  }

  // ─── create() ─────────────────────────────────────────────────────────────────

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenSupplierIdNull() {
    PurchaseCreateRequest req = validCreateRequest();
    req.setSupplierId(null);
    purchaseService.create(req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenOrderDateBlank() {
    PurchaseCreateRequest req = validCreateRequest();
    req.setOrderDate("");
    purchaseService.create(req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenItemsEmpty() {
    PurchaseCreateRequest req = validCreateRequest();
    req.setItems(Collections.<PurchaseItemRequest>emptyList());
    purchaseService.create(req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenItemQuantityZero() {
    PurchaseCreateRequest req = validCreateRequest();
    req.getItems().get(0).setQuantity(0);
    purchaseService.create(req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenItemUnitCostNegative() {
    PurchaseCreateRequest req = validCreateRequest();
    req.getItems().get(0).setUnitCost(-1.0);
    purchaseService.create(req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenDuplicateProductInItems() {
    PurchaseCreateRequest req = validCreateRequest();
    PurchaseItemRequest item2 = new PurchaseItemRequest();
    item2.setProductId(1L);
    item2.setQuantity(5);
    item2.setUnitCost(50.0);
    req.setItems(Arrays.asList(req.getItems().get(0), item2));
    purchaseService.create(req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenSupplierInactive() {
    PurchaseCreateRequest req = validCreateRequest();
    when(purchaseRepository.supplierExistsAndActive(1L)).thenReturn(false);
    purchaseService.create(req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenProductNotFound() {
    PurchaseCreateRequest req = validCreateRequest();
    when(purchaseRepository.supplierExistsAndActive(1L)).thenReturn(true);
    when(purchaseRepository.productExists(1L)).thenReturn(false);
    purchaseService.create(req, 1L);
  }

  @Test
  public void create_success() {
    PurchaseCreateRequest req = validCreateRequest();
    when(purchaseRepository.supplierExistsAndActive(1L)).thenReturn(true);
    when(purchaseRepository.productExists(1L)).thenReturn(true);
    when(purchaseRepository.generateNextPoNumber()).thenReturn("PO-2026-00002");
    when(purchaseRepository.insert(
            eq("PO-2026-00002"), eq(1L), eq("2026-06-01"), isNull(), isNull(), eq(1000.0), eq(1L)))
        .thenReturn(2L);
    when(purchaseRepository.findById(2L))
        .thenReturn(
            new PurchaseData(
                2L,
                "PO-2026-00002",
                1L,
                "Tech Solutions Inc.",
                "2026-06-01",
                null,
                null,
                1,
                1000.0,
                "Pending",
                null,
                null,
                "2026-06-01T00:00:00Z",
                "2026-06-01T00:00:00Z"));
    when(purchaseRepository.findItemsByPurchaseId(2L))
        .thenReturn(
            Collections.singletonList(
                new PurchaseItemData(1L, 1L, "P001", "Keyboard", 10, 100.0, 1000.0)));

    PurchaseDetailData result = purchaseService.create(req, 1L);
    assertNotNull(result);
    assertEquals("PO-2026-00002", result.getPoNumber());
    assertEquals(1, result.getItems().size());
    verify(purchaseRepository).insertItem(eq(2L), eq(1L), eq(10), eq(100.0), eq(1000.0));
  }

  // ─── getById() ────────────────────────────────────────────────────────────────

  @Test(expected = NotFoundException.class)
  public void getById_throwsNotFound() {
    when(purchaseRepository.findById(99L)).thenReturn(null);
    purchaseService.getById(99L);
  }

  @Test
  public void getById_returnsDetail() {
    when(purchaseRepository.findById(1L)).thenReturn(samplePurchase("Pending"));
    when(purchaseRepository.findItemsByPurchaseId(1L))
        .thenReturn(Collections.singletonList(sampleItem()));
    PurchaseDetailData result = purchaseService.getById(1L);
    assertNotNull(result);
    assertEquals(1, result.getItems().size());
    assertEquals("PO-2026-00001", result.getPoNumber());
  }

  // ─── update() ─────────────────────────────────────────────────────────────────

  @Test(expected = NotFoundException.class)
  public void update_throwsNotFound() {
    when(purchaseRepository.findById(99L)).thenReturn(null);
    purchaseService.update(99L, validUpdateRequest(), 1L);
  }

  @Test(expected = ConflictException.class)
  public void update_throwsConflict_whenNotPending() {
    when(purchaseRepository.findById(1L)).thenReturn(samplePurchase("Received"));
    purchaseService.update(1L, validUpdateRequest(), 1L);
  }

  @Test
  public void update_success_whenPending() {
    when(purchaseRepository.findById(1L)).thenReturn(samplePurchase("Pending"));
    when(purchaseRepository.supplierExistsAndActive(1L)).thenReturn(true);
    when(purchaseRepository.productExists(1L)).thenReturn(true);
    when(purchaseRepository.findById(1L)).thenReturn(samplePurchase("Pending"));
    when(purchaseRepository.findItemsByPurchaseId(1L))
        .thenReturn(Collections.singletonList(sampleItem()));

    PurchaseDetailData result = purchaseService.update(1L, validUpdateRequest(), 1L);
    assertNotNull(result);
    verify(purchaseRepository).deleteItemsByPurchaseId(1L);
    verify(purchaseRepository)
        .updateHeader(eq(1L), eq(1L), eq("2026-06-01"), isNull(), isNull(), eq(1000.0), eq(1L));
  }

  // ─── cancel() ─────────────────────────────────────────────────────────────────

  @Test(expected = NotFoundException.class)
  public void cancel_throwsNotFound() {
    when(purchaseRepository.findById(99L)).thenReturn(null);
    CancelPurchaseRequest req = new CancelPurchaseRequest();
    req.setReason("No longer needed");
    purchaseService.cancel(99L, req, 1L);
  }

  @Test(expected = ConflictException.class)
  public void cancel_throwsConflict_whenNotPending() {
    when(purchaseRepository.findById(1L)).thenReturn(samplePurchase("Received"));
    CancelPurchaseRequest req = new CancelPurchaseRequest();
    req.setReason("Changed plans");
    purchaseService.cancel(1L, req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void cancel_throwsValidation_whenReasonBlank() {
    when(purchaseRepository.findById(1L)).thenReturn(samplePurchase("Pending"));
    CancelPurchaseRequest req = new CancelPurchaseRequest();
    req.setReason("");
    purchaseService.cancel(1L, req, 1L);
  }

  @Test
  public void cancel_success() {
    when(purchaseRepository.findById(1L)).thenReturn(samplePurchase("Pending"));
    CancelPurchaseRequest req = new CancelPurchaseRequest();
    req.setReason("Supplier issue");
    purchaseService.cancel(1L, req, 1L);
    verify(purchaseRepository).cancel(1L, "Supplier issue", 1L);
  }

  // ─── list() ───────────────────────────────────────────────────────────────────

  @Test
  public void list_returnsPaginatedResponse() {
    List<PurchaseData> purchases = Arrays.asList(samplePurchase("Pending"));
    when(purchaseRepository.findAll(0, 10, "createdAt", "desc", null, null, null, null, null))
        .thenReturn(purchases);
    when(purchaseRepository.count(null, null, null, null, null)).thenReturn(1L);

    RequestContext ctx = mock(RequestContext.class);
    when(ctx.getQueryParam("page")).thenReturn(null);
    when(ctx.getQueryParam("size")).thenReturn(null);
    when(ctx.getQueryParam("sortBy", "createdAt")).thenReturn("createdAt");
    when(ctx.getQueryParam("sortDir", "asc")).thenReturn("desc");
    when(ctx.getQueryParam("search")).thenReturn(null);
    PaginationParams params =
        PaginationParams.from(ctx, "createdAt", new HashSet<>(Arrays.asList("createdAt")));

    PaginatedResponse<PurchaseData> result = purchaseService.list(params, null, null, null, null);
    assertNotNull(result);
    assertTrue(result.isSuccess());
    assertEquals(1, result.getData().size());
  }
}
