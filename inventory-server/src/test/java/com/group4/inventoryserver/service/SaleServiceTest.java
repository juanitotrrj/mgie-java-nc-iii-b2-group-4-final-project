package com.group4.inventoryserver.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.group4.inventoryserver.dto.PaginatedResponse;
import com.group4.inventoryserver.dto.PaginationParams;
import com.group4.inventoryserver.dto.sale.CancelSaleRequest;
import com.group4.inventoryserver.dto.sale.ReceiptData;
import com.group4.inventoryserver.dto.sale.SaleCreateRequest;
import com.group4.inventoryserver.dto.sale.SaleData;
import com.group4.inventoryserver.dto.sale.SaleDetailData;
import com.group4.inventoryserver.dto.sale.SaleItemData;
import com.group4.inventoryserver.dto.sale.SaleItemRequest;
import com.group4.inventoryserver.dto.sale.SaleUpdateRequest;
import com.group4.inventoryserver.exception.ConflictException;
import com.group4.inventoryserver.exception.ForbiddenException;
import com.group4.inventoryserver.exception.NotFoundException;
import com.group4.inventoryserver.exception.ValidationException;
import com.group4.inventoryserver.repository.SaleRepository;
import com.group4.inventoryserver.server.RequestContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class SaleServiceTest {

  private SaleRepository saleRepository;
  private SaleService saleService;

  @Before
  public void setUp() {
    saleRepository = mock(SaleRepository.class);
    saleService = new SaleService(saleRepository);
  }

  private SaleData sampleSale(String status) {
    return new SaleData(
        1L,
        "INV-2026-00001",
        "Walk-in Customer",
        1L,
        "Admin User",
        "2026-05-20T02:30:00Z",
        "Cash",
        status,
        1,
        1700.0,
        0.0,
        1700.0,
        2000.0,
        300.0,
        null,
        "2026-05-20T02:30:00Z",
        "2026-05-20T02:30:00Z");
  }

  private SaleItemData sampleItem() {
    return new SaleItemData(1L, 1L, "P001", "Keyboard", 2, 850.0, 1700.0);
  }

  private SaleCreateRequest validCreateRequest() {
    SaleCreateRequest req = new SaleCreateRequest();
    SaleItemRequest item = new SaleItemRequest();
    item.setProductId(1L);
    item.setQuantity(2);
    req.setItems(Collections.singletonList(item));
    req.setPaymentMethod("Cash");
    req.setAmountReceived(2000.0);
    return req;
  }

  private SaleUpdateRequest validUpdateRequest() {
    SaleUpdateRequest req = new SaleUpdateRequest();
    SaleItemRequest item = new SaleItemRequest();
    item.setProductId(1L);
    item.setQuantity(3);
    req.setItems(Collections.singletonList(item));
    req.setPaymentMethod("GCash");
    return req;
  }

  // ─── create() validation ──────────────────────────────────────────────────────

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenPaymentMethodNull() {
    SaleCreateRequest req = validCreateRequest();
    req.setPaymentMethod(null);
    saleService.create(req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenPaymentMethodInvalid() {
    SaleCreateRequest req = validCreateRequest();
    req.setPaymentMethod("Bitcoin");
    saleService.create(req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenItemsEmpty() {
    SaleCreateRequest req = validCreateRequest();
    req.setItems(Collections.<SaleItemRequest>emptyList());
    saleService.create(req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenItemQuantityZero() {
    SaleCreateRequest req = validCreateRequest();
    req.getItems().get(0).setQuantity(0);
    saleService.create(req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenItemProductIdNull() {
    SaleCreateRequest req = validCreateRequest();
    req.getItems().get(0).setProductId(null);
    saleService.create(req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenProductInactive() {
    SaleCreateRequest req = validCreateRequest();
    when(saleRepository.productExistsAndActive(1L)).thenReturn(false);
    saleService.create(req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenCashAmountReceivedMissing() {
    SaleCreateRequest req = validCreateRequest();
    req.setAmountReceived(null);
    saleService.create(req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenPendingNotAllowed() {
    SaleCreateRequest req = validCreateRequest();
    req.setStatus("Pending");
    saleService.create(req, 1L);
  }

  // ─── getById() ────────────────────────────────────────────────────────────────

  @Test(expected = NotFoundException.class)
  public void getById_throwsNotFound() {
    when(saleRepository.findById(99L)).thenReturn(null);
    saleService.getById(99L, 1L, true);
  }

  @Test
  public void getById_returnsDetail_whenPrivileged() {
    when(saleRepository.findById(1L)).thenReturn(sampleSale("Paid"));
    when(saleRepository.findItemsBySaleId(1L)).thenReturn(Collections.singletonList(sampleItem()));
    SaleDetailData result = saleService.getById(1L, 2L, true);
    assertNotNull(result);
    assertEquals("INV-2026-00001", result.getInvoiceNo());
  }

  @Test(expected = ForbiddenException.class)
  public void getById_throwsForbidden_whenCashierViewsOtherSale() {
    when(saleRepository.findById(1L)).thenReturn(sampleSale("Paid"));
    saleService.getById(1L, 2L, false);
  }

  @Test
  public void getById_allowsCashierOwnSale() {
    when(saleRepository.findById(1L)).thenReturn(sampleSale("Paid"));
    when(saleRepository.findItemsBySaleId(1L)).thenReturn(Collections.singletonList(sampleItem()));
    SaleDetailData result = saleService.getById(1L, 1L, false);
    assertNotNull(result);
  }

  // ─── update() ─────────────────────────────────────────────────────────────────

  @Test(expected = NotFoundException.class)
  public void update_throwsNotFound() {
    when(saleRepository.findById(99L)).thenReturn(null);
    saleService.update(99L, validUpdateRequest(), 1L, true);
  }

  @Test(expected = ConflictException.class)
  public void update_throwsConflict_whenPaid() {
    when(saleRepository.findById(1L)).thenReturn(sampleSale("Paid"));
    saleService.update(1L, validUpdateRequest(), 1L, true);
  }

  @Test(expected = ConflictException.class)
  public void update_throwsConflict_whenCancelled() {
    when(saleRepository.findById(1L)).thenReturn(sampleSale("Cancelled"));
    saleService.update(1L, validUpdateRequest(), 1L, true);
  }

  @Test(expected = ForbiddenException.class)
  public void update_throwsForbidden_whenCashierUpdatesOtherSale() {
    when(saleRepository.findById(1L))
        .thenReturn(
            new SaleData(
                1L,
                "INV-2026-00001",
                "Walk-in Customer",
                2L,
                "Other Cashier",
                "2026-05-20T02:30:00Z",
                "Cash",
                "Pending",
                1,
                1700.0,
                0.0,
                1700.0,
                2000.0,
                300.0,
                null,
                "2026-05-20T02:30:00Z",
                "2026-05-20T02:30:00Z"));
    saleService.update(1L, validUpdateRequest(), 1L, false);
  }

  @Test
  public void update_success_whenPending() {
    SaleData pendingSale =
        new SaleData(
            1L,
            "INV-2026-00001",
            "Walk-in Customer",
            1L,
            "Admin User",
            "2026-05-20T02:30:00Z",
            "Cash",
            "Pending",
            1,
            1700.0,
            0.0,
            1700.0,
            2000.0,
            300.0,
            null,
            "2026-05-20T02:30:00Z",
            "2026-05-20T02:30:00Z");
    when(saleRepository.findById(1L)).thenReturn(pendingSale);
    when(saleRepository.productExistsAndActive(1L)).thenReturn(true);
    when(saleRepository.getProductUnitPrice(1L)).thenReturn(850.0);
    when(saleRepository.findItemsBySaleId(1L)).thenReturn(Collections.singletonList(sampleItem()));

    SaleDetailData result = saleService.update(1L, validUpdateRequest(), 1L, true);
    assertNotNull(result);
    verify(saleRepository).deleteItemsBySaleId(1L);
    verify(saleRepository)
        .updateHeader(
            eq(1L),
            eq("Walk-in Customer"),
            eq("GCash"),
            eq(2550.0),
            eq(0.0),
            eq(2550.0),
            eq(2550.0),
            eq(0.0),
            eq(1L));
    verify(saleRepository).insertItem(eq(1L), eq(1L), eq(3), eq(850.0), eq(2550.0));
  }

  // ─── cancel() ─────────────────────────────────────────────────────────────────

  @Test(expected = NotFoundException.class)
  public void cancel_throwsNotFound() {
    when(saleRepository.findById(99L)).thenReturn(null);
    CancelSaleRequest req = new CancelSaleRequest();
    req.setReason("Changed mind");
    saleService.cancel(99L, req, 1L, true);
  }

  @Test(expected = ConflictException.class)
  public void cancel_throwsConflict_whenAlreadyCancelled() {
    when(saleRepository.findById(1L)).thenReturn(sampleSale("Cancelled"));
    CancelSaleRequest req = new CancelSaleRequest();
    req.setReason("Duplicate");
    saleService.cancel(1L, req, 1L, true);
  }

  @Test(expected = ValidationException.class)
  public void cancel_throwsValidation_whenReasonBlank() {
    when(saleRepository.findById(1L)).thenReturn(sampleSale("Paid"));
    CancelSaleRequest req = new CancelSaleRequest();
    req.setReason("");
    saleService.cancel(1L, req, 1L, true);
  }

  // ─── list() ───────────────────────────────────────────────────────────────────

  @Test
  public void list_returnsPaginatedResponse() {
    List<SaleData> sales = Arrays.asList(sampleSale("Paid"));
    when(saleRepository.findAll(0, 10, "createdAt", "desc", null, null, null, null, null, null))
        .thenReturn(sales);
    when(saleRepository.count(null, null, null, null, null, null)).thenReturn(1L);

    RequestContext ctx = mock(RequestContext.class);
    when(ctx.getQueryParam("page")).thenReturn(null);
    when(ctx.getQueryParam("size")).thenReturn(null);
    when(ctx.getQueryParam("sortBy", "createdAt")).thenReturn("createdAt");
    when(ctx.getQueryParam("sortDir", "asc")).thenReturn("desc");
    when(ctx.getQueryParam("search")).thenReturn(null);
    PaginationParams params =
        PaginationParams.from(ctx, "createdAt", new HashSet<>(Arrays.asList("createdAt")));

    PaginatedResponse<SaleData> result = saleService.list(params, null, null, null, null, null);
    assertNotNull(result);
    assertTrue(result.isSuccess());
    assertEquals(1, result.getData().size());
  }

  // ─── getReceipt() ─────────────────────────────────────────────────────────────

  @Test
  public void getReceipt_returnsReceiptData() {
    when(saleRepository.findById(1L)).thenReturn(sampleSale("Paid"));
    when(saleRepository.findItemsBySaleId(1L)).thenReturn(Collections.singletonList(sampleItem()));
    ReceiptData receipt = saleService.getReceipt(1L, 1L, true);
    assertNotNull(receipt);
    assertEquals("INV-2026-00001", receipt.getInvoiceNo());
    assertEquals(1, receipt.getItems().size());
    assertEquals(2000.0, receipt.getAmountReceived(), 0.01);
    assertEquals(300.0, receipt.getChangeAmount(), 0.01);
  }
}
