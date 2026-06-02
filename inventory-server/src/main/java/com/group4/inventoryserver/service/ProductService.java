package com.group4.inventoryserver.service;

import com.group4.inventoryserver.dto.PaginatedResponse;
import com.group4.inventoryserver.dto.PaginationMeta;
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
import com.group4.inventoryserver.util.ValidationUtil;
import com.group4.inventoryserver.util.ValidationUtil.FieldError;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductService {

  private static final Logger log = LoggerFactory.getLogger(ProductService.class);

  private static final Set<String> VALID_STATUSES =
      new HashSet<>(Arrays.asList("In Stock", "Low Stock", "Out of Stock", "Inactive"));

  private final ProductRepository productRepository;
  private final StockMovementRepository stockMovementRepository;

  public ProductService() {
    this(new ProductRepository(), new StockMovementRepository());
  }

  ProductService(
      ProductRepository productRepository, StockMovementRepository stockMovementRepository) {
    this.productRepository = productRepository;
    this.stockMovementRepository = stockMovementRepository;
  }

  public PaginatedResponse<ProductData> list(
      PaginationParams params, Long categoryId, String status) {
    List<ProductData> data =
        productRepository.findAll(
            params.getOffset(),
            params.getSize(),
            params.getSortBy(),
            params.getSortDir(),
            params.getSearch(),
            categoryId,
            status);
    long total = productRepository.count(params.getSearch(), categoryId, status);
    PaginationMeta meta =
        new PaginationMeta(
            params.getPage(), params.getSize(), total, params.getSortBy(), params.getSortDir());
    return new PaginatedResponse<>(data, meta);
  }

  public List<ProductData> listAll(String search, Long categoryId, String status) {
    return productRepository.findAll(
        0, Integer.MAX_VALUE, "productName", "asc", search, categoryId, status);
  }

  public ProductData getById(long productId) {
    ProductData product = productRepository.findById(productId);
    if (product == null) {
      throw new NotFoundException("Product not found.");
    }
    return product;
  }

  public ProductData create(ProductCreateRequest request, long authUserId) {
    validateCreateRequest(request);

    if (productRepository.existsByProductCode(request.getProductCode())) {
      throw new ConflictException("Product code already exists.");
    }

    if (!productRepository.categoryExistsAndActive(request.getCategoryId())) {
      throw new ValidationException("Category does not exist or is inactive.");
    }

    if (request.getSupplierId() != null
        && !productRepository.supplierExists(request.getSupplierId())) {
      throw new ValidationException("Supplier does not exist.");
    }

    int quantity = request.getQuantity() != null ? request.getQuantity() : 0;
    int reorderLevel = request.getReorderLevel() != null ? request.getReorderLevel() : 0;
    double unitPrice = request.getUnitPrice() != null ? request.getUnitPrice() : 0.0;
    String status = computeStatus(quantity, reorderLevel, request.getStatus());

    long productId =
        productRepository.insert(
            request.getProductCode().trim(),
            request.getProductName().trim(),
            request.getCategoryId(),
            request.getSupplierId(),
            quantity,
            reorderLevel,
            unitPrice,
            status,
            authUserId);

    if (quantity > 0) {
      stockMovementRepository.create(
          productId,
          "OPENING",
          "PRODUCT",
          productId,
          request.getProductCode().trim(),
          0,
          quantity,
          quantity,
          "Opening stock on product creation",
          authUserId);
    }

    log.info(
        "Product created [id={}, code={}] by user {}",
        productId,
        request.getProductCode(),
        authUserId);
    return productRepository.findById(productId);
  }

  public ProductData update(
      long productId, ProductUpdateRequest request, long authUserId, String authRole) {
    ProductData existing = productRepository.findById(productId);
    if (existing == null) {
      throw new NotFoundException("Product not found.");
    }

    if ("Inventory Clerk".equals(authRole)) {
      // Clerks cannot directly change stock-related attributes beyond what's allowed
    }

    validateUpdateRequest(request);

    if (request.getCategoryId() != null
        && !productRepository.categoryExistsAndActive(request.getCategoryId())) {
      throw new ValidationException("Category does not exist or is inactive.");
    }

    if (request.getSupplierId() != null
        && !productRepository.supplierExists(request.getSupplierId())) {
      throw new ValidationException("Supplier does not exist.");
    }

    String newStatus = request.getStatus();
    if (newStatus == null) {
      int newReorder =
          request.getReorderLevel() != null
              ? request.getReorderLevel()
              : existing.getReorderLevel();
      newStatus = computeStatus(existing.getQuantity(), newReorder, null);
    }

    productRepository.update(
        productId,
        request.getProductName(),
        request.getCategoryId(),
        request.getSupplierId(),
        request.getReorderLevel(),
        request.getUnitPrice(),
        newStatus,
        authUserId);

    log.info("Product updated [id={}] by user {}", productId, authUserId);
    return productRepository.findById(productId);
  }

  public void deactivate(long productId, long authUserId) {
    ProductData existing = productRepository.findById(productId);
    if (existing == null) {
      throw new NotFoundException("Product not found.");
    }
    productRepository.deactivate(productId, authUserId);
    log.info("Product deactivated [id={}] by user {}", productId, authUserId);
  }

  public PaginatedResponse<StockMovementData> getStockMovements(
      long productId, PaginationParams params) {
    ProductData product = productRepository.findById(productId);
    if (product == null) {
      throw new NotFoundException("Product not found.");
    }
    List<StockMovementData> data =
        stockMovementRepository.findByProductId(productId, params.getOffset(), params.getSize());
    long total = stockMovementRepository.countByProductId(productId);
    PaginationMeta meta =
        new PaginationMeta(params.getPage(), params.getSize(), total, "createdAt", "desc");
    return new PaginatedResponse<>(data, meta);
  }

  private void validateCreateRequest(ProductCreateRequest request) {
    List<FieldError> errors = ValidationUtil.newErrorList();
    ValidationUtil.requireNonBlank(request.getProductCode(), "productCode", errors);
    ValidationUtil.requireNonBlank(request.getProductName(), "productName", errors);
    ValidationUtil.requireMaxLength(request.getProductCode(), "productCode", 30, errors);
    ValidationUtil.requireMaxLength(request.getProductName(), "productName", 150, errors);

    if (request.getCategoryId() == null) {
      errors.add(new FieldError("categoryId", "Category is required."));
    }
    if (request.getQuantity() == null) {
      errors.add(new FieldError("quantity", "Quantity is required."));
    } else if (request.getQuantity() < 0) {
      errors.add(new FieldError("quantity", "Quantity must not be negative."));
    }
    if (request.getUnitPrice() == null) {
      errors.add(new FieldError("unitPrice", "Unit price is required."));
    } else if (request.getUnitPrice() < 0) {
      errors.add(new FieldError("unitPrice", "Unit price must not be negative."));
    }
    if (request.getReorderLevel() != null && request.getReorderLevel() < 0) {
      errors.add(new FieldError("reorderLevel", "Reorder level must not be negative."));
    }
    if (request.getStatus() != null && !VALID_STATUSES.contains(request.getStatus())) {
      errors.add(new FieldError("status", "Invalid status value."));
    }
    ValidationUtil.throwIfErrors(errors);
  }

  private void validateUpdateRequest(ProductUpdateRequest request) {
    List<FieldError> errors = ValidationUtil.newErrorList();
    ValidationUtil.requireMaxLength(request.getProductName(), "productName", 150, errors);
    if (request.getReorderLevel() != null && request.getReorderLevel() < 0) {
      errors.add(new FieldError("reorderLevel", "Reorder level must not be negative."));
    }
    if (request.getUnitPrice() != null && request.getUnitPrice() < 0) {
      errors.add(new FieldError("unitPrice", "Unit price must not be negative."));
    }
    if (request.getStatus() != null && !VALID_STATUSES.contains(request.getStatus())) {
      errors.add(new FieldError("status", "Invalid status value."));
    }
    ValidationUtil.throwIfErrors(errors);
  }

  private String computeStatus(int quantity, int reorderLevel, String explicitStatus) {
    if ("Inactive".equals(explicitStatus)) return "Inactive";
    if (quantity <= 0) return "Out of Stock";
    if (quantity <= reorderLevel) return "Low Stock";
    return "In Stock";
  }
}
