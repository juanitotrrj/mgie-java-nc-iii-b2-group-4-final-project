package com.group4.inventoryserver.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.group4.inventoryserver.dto.PaginatedResponse;
import com.group4.inventoryserver.dto.PaginationParams;
import com.group4.inventoryserver.dto.product.ProductCreateRequest;
import com.group4.inventoryserver.dto.product.ProductData;
import com.group4.inventoryserver.dto.product.ProductUpdateRequest;
import com.group4.inventoryserver.dto.product.StockMovementData;
import com.group4.inventoryserver.exception.ConflictException;
import com.group4.inventoryserver.exception.NotFoundException;
import com.group4.inventoryserver.exception.ValidationException;
import com.group4.inventoryserver.repository.ProductRepository;
import com.group4.inventoryserver.repository.StockMovementRepository;
import com.group4.inventoryserver.server.RequestContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class ProductServiceTest {

  private ProductRepository productRepository;
  private StockMovementRepository stockMovementRepository;
  private ProductService productService;

  @Before
  public void setUp() {
    productRepository = mock(ProductRepository.class);
    stockMovementRepository = mock(StockMovementRepository.class);
    productService = new ProductService(productRepository, stockMovementRepository);
  }

  private ProductData sampleProduct() {
    return new ProductData(
        1L,
        "P001",
        "Keyboard",
        1L,
        "Accessories",
        1L,
        "Tech Solutions",
        45,
        20,
        850.0,
        "In Stock",
        "2026-01-01T00:00:00Z",
        "2026-01-01T00:00:00Z");
  }

  private ProductCreateRequest validCreateRequest() {
    ProductCreateRequest req = new ProductCreateRequest();
    req.setProductCode("P005");
    req.setProductName("New Product");
    req.setCategoryId(1L);
    req.setQuantity(10);
    req.setUnitPrice(100.0);
    return req;
  }

  // ─── create() ─────────────────────────────────────────────────────────────────

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenCodeBlank() {
    ProductCreateRequest req = validCreateRequest();
    req.setProductCode("");
    productService.create(req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenNameBlank() {
    ProductCreateRequest req = validCreateRequest();
    req.setProductName("");
    productService.create(req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenCategoryNull() {
    ProductCreateRequest req = validCreateRequest();
    req.setCategoryId(null);
    productService.create(req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenQuantityNegative() {
    ProductCreateRequest req = validCreateRequest();
    req.setQuantity(-1);
    productService.create(req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenUnitPriceNull() {
    ProductCreateRequest req = validCreateRequest();
    req.setUnitPrice(null);
    productService.create(req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenUnitPriceNegative() {
    ProductCreateRequest req = validCreateRequest();
    req.setUnitPrice(-5.0);
    productService.create(req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenStatusInvalid() {
    ProductCreateRequest req = validCreateRequest();
    req.setStatus("Discontinued");
    productService.create(req, 1L);
  }

  @Test(expected = ConflictException.class)
  public void create_throwsConflict_whenProductCodeExists() {
    ProductCreateRequest req = validCreateRequest();
    when(productRepository.existsByProductCode("P005")).thenReturn(true);
    productService.create(req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenCategoryInactive() {
    ProductCreateRequest req = validCreateRequest();
    when(productRepository.existsByProductCode("P005")).thenReturn(false);
    when(productRepository.categoryExistsAndActive(1L)).thenReturn(false);
    productService.create(req, 1L);
  }

  @Test(expected = ValidationException.class)
  public void create_throwsValidation_whenSupplierNotFound() {
    ProductCreateRequest req = validCreateRequest();
    req.setSupplierId(99L);
    when(productRepository.existsByProductCode("P005")).thenReturn(false);
    when(productRepository.categoryExistsAndActive(1L)).thenReturn(true);
    when(productRepository.supplierExists(99L)).thenReturn(false);
    productService.create(req, 1L);
  }

  @Test
  public void create_success_insertsProductAndReturnsData() {
    ProductCreateRequest req = validCreateRequest();
    when(productRepository.existsByProductCode("P005")).thenReturn(false);
    when(productRepository.categoryExistsAndActive(1L)).thenReturn(true);
    when(productRepository.insert(
            eq("P005"),
            eq("New Product"),
            eq(1L),
            isNull(),
            eq(10),
            eq(0),
            eq(100.0),
            eq("In Stock"),
            eq(1L)))
        .thenReturn(5L);
    when(productRepository.findById(5L))
        .thenReturn(
            new ProductData(
                5L,
                "P005",
                "New Product",
                1L,
                "Accessories",
                null,
                null,
                10,
                0,
                100.0,
                "In Stock",
                "2026-01-01T00:00:00Z",
                "2026-01-01T00:00:00Z"));

    ProductData result = productService.create(req, 1L);
    assertNotNull(result);
    assertEquals("P005", result.getProductCode());
  }

  @Test
  public void create_createsOpeningStockMovement_whenQuantityPositive() {
    ProductCreateRequest req = validCreateRequest();
    when(productRepository.existsByProductCode("P005")).thenReturn(false);
    when(productRepository.categoryExistsAndActive(1L)).thenReturn(true);
    when(productRepository.insert(
            anyString(),
            anyString(),
            anyLong(),
            any(),
            anyInt(),
            anyInt(),
            anyDouble(),
            anyString(),
            anyLong()))
        .thenReturn(5L);
    when(productRepository.findById(5L)).thenReturn(sampleProduct());

    productService.create(req, 1L);

    verify(stockMovementRepository)
        .create(
            eq(5L),
            eq("OPENING"),
            eq("PRODUCT"),
            eq(5L),
            eq("P005"),
            eq(0),
            eq(10),
            eq(10),
            anyString(),
            eq(1L));
  }

  @Test
  public void create_noStockMovement_whenQuantityZero() {
    ProductCreateRequest req = validCreateRequest();
    req.setQuantity(0);
    when(productRepository.existsByProductCode("P005")).thenReturn(false);
    when(productRepository.categoryExistsAndActive(1L)).thenReturn(true);
    when(productRepository.insert(
            anyString(),
            anyString(),
            anyLong(),
            any(),
            anyInt(),
            anyInt(),
            anyDouble(),
            anyString(),
            anyLong()))
        .thenReturn(5L);
    when(productRepository.findById(5L)).thenReturn(sampleProduct());

    productService.create(req, 1L);

    verify(stockMovementRepository, never())
        .create(
            anyLong(),
            anyString(),
            anyString(),
            any(),
            anyString(),
            anyInt(),
            anyInt(),
            anyInt(),
            anyString(),
            anyLong());
  }

  @Test
  public void create_computesOutOfStock_whenQuantityZero() {
    ProductCreateRequest req = validCreateRequest();
    req.setQuantity(0);
    when(productRepository.existsByProductCode("P005")).thenReturn(false);
    when(productRepository.categoryExistsAndActive(1L)).thenReturn(true);
    when(productRepository.insert(
            anyString(),
            anyString(),
            anyLong(),
            any(),
            eq(0),
            anyInt(),
            anyDouble(),
            eq("Out of Stock"),
            anyLong()))
        .thenReturn(5L);
    when(productRepository.findById(5L)).thenReturn(sampleProduct());

    productService.create(req, 1L);

    verify(productRepository)
        .insert(
            anyString(),
            anyString(),
            anyLong(),
            any(),
            eq(0),
            anyInt(),
            anyDouble(),
            eq("Out of Stock"),
            anyLong());
  }

  @Test
  public void create_computesLowStock_whenQuantityBelowReorder() {
    ProductCreateRequest req = validCreateRequest();
    req.setQuantity(5);
    req.setReorderLevel(10);
    when(productRepository.existsByProductCode("P005")).thenReturn(false);
    when(productRepository.categoryExistsAndActive(1L)).thenReturn(true);
    when(productRepository.insert(
            anyString(),
            anyString(),
            anyLong(),
            any(),
            eq(5),
            eq(10),
            anyDouble(),
            eq("Low Stock"),
            anyLong()))
        .thenReturn(5L);
    when(productRepository.findById(5L)).thenReturn(sampleProduct());

    productService.create(req, 1L);

    verify(productRepository)
        .insert(
            anyString(),
            anyString(),
            anyLong(),
            any(),
            eq(5),
            eq(10),
            anyDouble(),
            eq("Low Stock"),
            anyLong());
  }

  // ─── getById() ────────────────────────────────────────────────────────────────

  @Test
  public void getById_returnsProduct_whenExists() {
    when(productRepository.findById(1L)).thenReturn(sampleProduct());
    ProductData result = productService.getById(1L);
    assertNotNull(result);
    assertEquals("P001", result.getProductCode());
  }

  @Test(expected = NotFoundException.class)
  public void getById_throwsNotFound_whenMissing() {
    when(productRepository.findById(99L)).thenReturn(null);
    productService.getById(99L);
  }

  // ─── update() ─────────────────────────────────────────────────────────────────

  @Test(expected = NotFoundException.class)
  public void update_throwsNotFound_whenProductMissing() {
    when(productRepository.findById(99L)).thenReturn(null);
    productService.update(99L, new ProductUpdateRequest(), 1L, "Administrator");
  }

  @Test(expected = ValidationException.class)
  public void update_throwsValidation_whenCategoryInactive() {
    when(productRepository.findById(1L)).thenReturn(sampleProduct());
    ProductUpdateRequest req = new ProductUpdateRequest();
    req.setCategoryId(99L);
    when(productRepository.categoryExistsAndActive(99L)).thenReturn(false);
    productService.update(1L, req, 1L, "Administrator");
  }

  @Test
  public void update_success_updatesAndReturnsProduct() {
    ProductData existing = sampleProduct();
    when(productRepository.findById(1L)).thenReturn(existing);
    ProductUpdateRequest req = new ProductUpdateRequest();
    req.setProductName("Updated Name");
    req.setUnitPrice(999.0);
    when(productRepository.findById(1L))
        .thenReturn(
            new ProductData(
                1L,
                "P001",
                "Updated Name",
                1L,
                "Accessories",
                1L,
                "Tech Solutions",
                45,
                20,
                999.0,
                "In Stock",
                "2026-01-01T00:00:00Z",
                "2026-01-01T00:00:00Z"));

    ProductData result = productService.update(1L, req, 1L, "Administrator");
    assertNotNull(result);
    verify(productRepository)
        .update(
            eq(1L),
            eq("Updated Name"),
            isNull(),
            isNull(),
            isNull(),
            eq(999.0),
            eq("In Stock"),
            eq(1L));
  }

  @Test(expected = ValidationException.class)
  public void update_throwsValidation_whenReorderNegative() {
    when(productRepository.findById(1L)).thenReturn(sampleProduct());
    ProductUpdateRequest req = new ProductUpdateRequest();
    req.setReorderLevel(-5);
    productService.update(1L, req, 1L, "Administrator");
  }

  @Test(expected = ValidationException.class)
  public void update_throwsValidation_whenInvalidStatus() {
    when(productRepository.findById(1L)).thenReturn(sampleProduct());
    ProductUpdateRequest req = new ProductUpdateRequest();
    req.setStatus("BadStatus");
    productService.update(1L, req, 1L, "Administrator");
  }

  // ─── deactivate() ─────────────────────────────────────────────────────────────

  @Test(expected = NotFoundException.class)
  public void deactivate_throwsNotFound_whenMissing() {
    when(productRepository.findById(99L)).thenReturn(null);
    productService.deactivate(99L, 1L);
  }

  @Test
  public void deactivate_success() {
    when(productRepository.findById(1L)).thenReturn(sampleProduct());
    productService.deactivate(1L, 1L);
    verify(productRepository).deactivate(1L, 1L);
  }

  // ─── list() ───────────────────────────────────────────────────────────────────

  @Test
  public void list_returnsPaginatedResponse() {
    List<ProductData> products = Arrays.asList(sampleProduct());
    when(productRepository.findAll(0, 10, "productName", "asc", null, null, null))
        .thenReturn(products);
    when(productRepository.count(null, null, null)).thenReturn(1L);

    RequestContext ctx = mock(RequestContext.class);
    when(ctx.getQueryParam("page")).thenReturn(null);
    when(ctx.getQueryParam("size")).thenReturn(null);
    when(ctx.getQueryParam("sortBy", "productName")).thenReturn("productName");
    when(ctx.getQueryParam("sortDir", "asc")).thenReturn("asc");
    when(ctx.getQueryParam("search")).thenReturn(null);
    PaginationParams params =
        PaginationParams.from(ctx, "productName", new HashSet<>(Arrays.asList("productName")));

    PaginatedResponse<ProductData> result = productService.list(params, null, null);
    assertNotNull(result);
    assertTrue(result.isSuccess());
    assertEquals(1, result.getData().size());
    assertEquals(1L, result.getMeta().getTotalRecords());
    assertEquals(1, result.getMeta().getTotalPages());
  }

  // ─── getStockMovements() ──────────────────────────────────────────────────────

  @Test(expected = NotFoundException.class)
  public void getStockMovements_throwsNotFound_whenProductMissing() {
    when(productRepository.findById(99L)).thenReturn(null);
    RequestContext ctx = mock(RequestContext.class);
    when(ctx.getQueryParam("page")).thenReturn(null);
    when(ctx.getQueryParam("size")).thenReturn(null);
    when(ctx.getQueryParam("sortBy", "createdAt")).thenReturn("createdAt");
    when(ctx.getQueryParam("sortDir", "asc")).thenReturn("desc");
    when(ctx.getQueryParam("search")).thenReturn(null);
    PaginationParams params = PaginationParams.from(ctx, "createdAt", null);
    productService.getStockMovements(99L, params);
  }

  @Test
  public void getStockMovements_returnsHistory() {
    when(productRepository.findById(1L)).thenReturn(sampleProduct());
    StockMovementData movement =
        new StockMovementData(
            1L,
            1L,
            "OPENING",
            "PRODUCT",
            1L,
            "P001",
            0,
            45,
            45,
            "Opening",
            1L,
            "2026-01-01T00:00:00Z");
    when(stockMovementRepository.findByProductId(1L, 0, 10))
        .thenReturn(Collections.singletonList(movement));
    when(stockMovementRepository.countByProductId(1L)).thenReturn(1L);

    RequestContext ctx = mock(RequestContext.class);
    when(ctx.getQueryParam("page")).thenReturn(null);
    when(ctx.getQueryParam("size")).thenReturn(null);
    when(ctx.getQueryParam("sortBy", "createdAt")).thenReturn("createdAt");
    when(ctx.getQueryParam("sortDir", "asc")).thenReturn("desc");
    when(ctx.getQueryParam("search")).thenReturn(null);
    PaginationParams params = PaginationParams.from(ctx, "createdAt", null);

    PaginatedResponse<StockMovementData> result = productService.getStockMovements(1L, params);
    assertNotNull(result);
    assertEquals(1, result.getData().size());
    assertEquals("OPENING", result.getData().get(0).getMovementType());
  }
}
