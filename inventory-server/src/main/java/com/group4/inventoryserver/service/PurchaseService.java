package com.group4.inventoryserver.service;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.dto.PaginatedResponse;
import com.group4.inventoryserver.dto.PaginationMeta;
import com.group4.inventoryserver.dto.PaginationParams;
import com.group4.inventoryserver.dto.purchase.CancelPurchaseRequest;
import com.group4.inventoryserver.dto.purchase.PurchaseCreateRequest;
import com.group4.inventoryserver.dto.purchase.PurchaseData;
import com.group4.inventoryserver.dto.purchase.PurchaseDetailData;
import com.group4.inventoryserver.dto.purchase.PurchaseItemData;
import com.group4.inventoryserver.dto.purchase.PurchaseItemRequest;
import com.group4.inventoryserver.dto.purchase.PurchaseUpdateRequest;
import com.group4.inventoryserver.dto.purchase.ReceivePurchaseRequest;
import com.group4.inventoryserver.exception.ConflictException;
import com.group4.inventoryserver.exception.NotFoundException;
import com.group4.inventoryserver.exception.ValidationException;
import com.group4.inventoryserver.repository.PurchaseRepository;
import com.group4.inventoryserver.repository.StockMovementRepository;
import com.group4.inventoryserver.util.ValidationUtil;
import com.group4.inventoryserver.util.ValidationUtil.FieldError;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PurchaseService {

  private static final Logger log = LoggerFactory.getLogger(PurchaseService.class);

  private final PurchaseRepository purchaseRepository;
  private final StockMovementRepository stockMovementRepository;

  public PurchaseService() {
    this(new PurchaseRepository(), new StockMovementRepository());
  }

  PurchaseService(
      PurchaseRepository purchaseRepository, StockMovementRepository stockMovementRepository) {
    this.purchaseRepository = purchaseRepository;
    this.stockMovementRepository = stockMovementRepository;
  }

  public PaginatedResponse<PurchaseData> list(
      PaginationParams params, Long supplierId, String status, String dateFrom, String dateTo) {
    List<PurchaseData> data =
        purchaseRepository.findAll(
            params.getOffset(),
            params.getSize(),
            params.getSortBy(),
            params.getSortDir(),
            params.getSearch(),
            supplierId,
            status,
            dateFrom,
            dateTo);
    long total = purchaseRepository.count(params.getSearch(), supplierId, status, dateFrom, dateTo);
    PaginationMeta meta =
        new PaginationMeta(
            params.getPage(), params.getSize(), total, params.getSortBy(), params.getSortDir());
    return new PaginatedResponse<>(data, meta);
  }

  public PurchaseDetailData getById(long purchaseOrderId) {
    PurchaseData header = purchaseRepository.findById(purchaseOrderId);
    if (header == null) {
      throw new NotFoundException("Purchase order not found.");
    }
    List<PurchaseItemData> items = purchaseRepository.findItemsByPurchaseId(purchaseOrderId);
    return new PurchaseDetailData(
        header.getPurchaseId(),
        header.getPoNumber(),
        header.getSupplierId(),
        header.getSupplierName(),
        header.getOrderDate(),
        header.getExpectedDeliveryDate(),
        header.getReceivedDate(),
        header.getItemCount(),
        header.getTotalAmount(),
        header.getStatus(),
        header.getNotes(),
        header.getCancelReason(),
        header.getCreatedAt(),
        header.getUpdatedAt(),
        items);
  }

  public PurchaseDetailData create(PurchaseCreateRequest request, long authUserId) {
    validateCreateRequest(request);

    if (!purchaseRepository.supplierExistsAndActive(request.getSupplierId())) {
      throw new ValidationException("Supplier does not exist or is inactive.");
    }

    validateProductsExist(request.getItems());

    String poNumber = purchaseRepository.generateNextPoNumber();
    double totalAmount = computeTotal(request.getItems());

    long purchaseOrderId =
        purchaseRepository.insert(
            poNumber,
            request.getSupplierId(),
            request.getOrderDate(),
            request.getExpectedDeliveryDate(),
            request.getNotes(),
            totalAmount,
            authUserId);

    for (PurchaseItemRequest item : request.getItems()) {
      double lineTotal = item.getQuantity() * item.getUnitCost();
      purchaseRepository.insertItem(
          purchaseOrderId, item.getProductId(), item.getQuantity(), item.getUnitCost(), lineTotal);
    }

    log.info(
        "Purchase order created [id={}, po={}] by user {}", purchaseOrderId, poNumber, authUserId);
    return getById(purchaseOrderId);
  }

  public PurchaseDetailData update(
      long purchaseOrderId, PurchaseUpdateRequest request, long authUserId) {
    PurchaseData existing = purchaseRepository.findById(purchaseOrderId);
    if (existing == null) {
      throw new NotFoundException("Purchase order not found.");
    }
    if (!"Pending".equals(existing.getStatus())) {
      throw new ConflictException("Only Pending purchase orders can be updated.");
    }

    validateUpdateRequest(request);

    if (!purchaseRepository.supplierExistsAndActive(request.getSupplierId())) {
      throw new ValidationException("Supplier does not exist or is inactive.");
    }

    validateProductsExist(request.getItems());

    double totalAmount = computeTotal(request.getItems());

    purchaseRepository.deleteItemsByPurchaseId(purchaseOrderId);
    purchaseRepository.updateHeader(
        purchaseOrderId,
        request.getSupplierId(),
        request.getOrderDate(),
        request.getExpectedDeliveryDate(),
        request.getNotes(),
        totalAmount,
        authUserId);

    for (PurchaseItemRequest item : request.getItems()) {
      double lineTotal = item.getQuantity() * item.getUnitCost();
      purchaseRepository.insertItem(
          purchaseOrderId, item.getProductId(), item.getQuantity(), item.getUnitCost(), lineTotal);
    }

    log.info("Purchase order updated [id={}] by user {}", purchaseOrderId, authUserId);
    return getById(purchaseOrderId);
  }

  public void receive(long purchaseOrderId, ReceivePurchaseRequest request, long authUserId) {
    PurchaseData existing = purchaseRepository.findById(purchaseOrderId);
    if (existing == null) {
      throw new NotFoundException("Purchase order not found.");
    }
    if (!"Pending".equals(existing.getStatus())) {
      throw new ConflictException("Only Pending purchase orders can be received.");
    }

    String receivedDate =
        request != null && request.getReceivedDate() != null
            ? request.getReceivedDate()
            : LocalDate.now().toString();
    String remarks =
        request != null && request.getRemarks() != null
            ? request.getRemarks()
            : "Stock increased after receiving purchase order.";

    try (Connection conn = DatabaseConfig.getConnection()) {
      conn.setAutoCommit(false);
      try {
        purchaseRepository.receive(conn, purchaseOrderId, receivedDate, authUserId);

        List<PurchaseItemData> items =
            purchaseRepository.findItemsByPurchaseId(conn, purchaseOrderId);

        for (PurchaseItemData item : items) {
          int currentQty = purchaseRepository.getProductQuantity(conn, item.getProductId());
          int newQty = currentQty + item.getQuantity();
          int reorderLevel = purchaseRepository.getProductReorderLevel(conn, item.getProductId());
          purchaseRepository.updateProductQuantity(conn, item.getProductId(), newQty, reorderLevel);

          createStockMovement(
              conn,
              item.getProductId(),
              existing.getPoNumber(),
              purchaseOrderId,
              currentQty,
              item.getQuantity(),
              newQty,
              remarks,
              authUserId);
        }

        conn.commit();
        log.info(
            "Purchase order received [id={}, po={}] by user {}",
            purchaseOrderId,
            existing.getPoNumber(),
            authUserId);
      } catch (Exception e) {
        conn.rollback();
        throw e;
      }
    } catch (ConflictException | NotFoundException e) {
      throw e;
    } catch (SQLException e) {
      throw new RuntimeException("Failed to receive purchase order", e);
    } catch (Exception e) {
      if (e instanceof RuntimeException) throw (RuntimeException) e;
      throw new RuntimeException("Failed to receive purchase order", e);
    }
  }

  public void cancel(long purchaseOrderId, CancelPurchaseRequest request, long authUserId) {
    PurchaseData existing = purchaseRepository.findById(purchaseOrderId);
    if (existing == null) {
      throw new NotFoundException("Purchase order not found.");
    }
    if (!"Pending".equals(existing.getStatus())) {
      throw new ConflictException("Only Pending purchase orders can be cancelled.");
    }

    List<FieldError> errors = ValidationUtil.newErrorList();
    ValidationUtil.requireNonBlank(request.getReason(), "reason", errors);
    ValidationUtil.throwIfErrors(errors);

    purchaseRepository.cancel(purchaseOrderId, request.getReason().trim(), authUserId);
    log.info("Purchase order cancelled [id={}] by user {}", purchaseOrderId, authUserId);
  }

  private void validateCreateRequest(PurchaseCreateRequest request) {
    List<FieldError> errors = ValidationUtil.newErrorList();
    if (request.getSupplierId() == null) {
      errors.add(new FieldError("supplierId", "Supplier is required."));
    }
    ValidationUtil.requireNonBlank(request.getOrderDate(), "orderDate", errors);
    if (request.getItems() == null || request.getItems().isEmpty()) {
      errors.add(new FieldError("items", "At least one item is required."));
    } else {
      validateItems(request.getItems(), errors);
    }
    ValidationUtil.requireMaxLength(request.getNotes(), "notes", 1000, errors);
    ValidationUtil.throwIfErrors(errors);
  }

  private void validateUpdateRequest(PurchaseUpdateRequest request) {
    List<FieldError> errors = ValidationUtil.newErrorList();
    if (request.getSupplierId() == null) {
      errors.add(new FieldError("supplierId", "Supplier is required."));
    }
    ValidationUtil.requireNonBlank(request.getOrderDate(), "orderDate", errors);
    if (request.getItems() == null || request.getItems().isEmpty()) {
      errors.add(new FieldError("items", "At least one item is required."));
    } else {
      validateItems(request.getItems(), errors);
    }
    ValidationUtil.requireMaxLength(request.getNotes(), "notes", 1000, errors);
    ValidationUtil.throwIfErrors(errors);
  }

  private void validateItems(List<PurchaseItemRequest> items, List<FieldError> errors) {
    Set<Long> seenProducts = new HashSet<>();
    for (int i = 0; i < items.size(); i++) {
      PurchaseItemRequest item = items.get(i);
      String prefix = "items[" + i + "].";
      if (item.getProductId() == null) {
        errors.add(new FieldError(prefix + "productId", "Product is required."));
      } else {
        if (!seenProducts.add(item.getProductId())) {
          errors.add(new FieldError(prefix + "productId", "Duplicate product in items list."));
        }
      }
      if (item.getQuantity() == null || item.getQuantity() < 1) {
        errors.add(new FieldError(prefix + "quantity", "Quantity must be at least 1."));
      }
      if (item.getUnitCost() == null || item.getUnitCost() < 0) {
        errors.add(new FieldError(prefix + "unitCost", "Unit cost must not be negative."));
      }
    }
  }

  private void validateProductsExist(List<PurchaseItemRequest> items) {
    for (PurchaseItemRequest item : items) {
      if (item.getProductId() != null && !purchaseRepository.productExists(item.getProductId())) {
        throw new ValidationException(
            "Product with id " + item.getProductId() + " does not exist.");
      }
    }
  }

  private double computeTotal(List<PurchaseItemRequest> items) {
    double total = 0;
    for (PurchaseItemRequest item : items) {
      total += item.getQuantity() * item.getUnitCost();
    }
    return total;
  }

  private void createStockMovement(
      Connection conn,
      long productId,
      String poNumber,
      long purchaseOrderId,
      int quantityBefore,
      int quantityChange,
      int quantityAfter,
      String remarks,
      Long createdBy)
      throws SQLException {
    String sql =
        "INSERT INTO stock_movements (product_id, movement_type, reference_type, reference_id, "
            + "reference_no, quantity_before, quantity_change, quantity_after, remarks, created_by) "
            + "VALUES (?, 'PURCHASE_IN', 'PURCHASE', ?, ?, ?, ?, ?, ?, ?)";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, productId);
      ps.setLong(2, purchaseOrderId);
      ps.setString(3, poNumber);
      ps.setInt(4, quantityBefore);
      ps.setInt(5, quantityChange);
      ps.setInt(6, quantityAfter);
      ps.setString(7, remarks);
      if (createdBy != null) {
        ps.setLong(8, createdBy);
      } else {
        ps.setNull(8, java.sql.Types.BIGINT);
      }
      ps.executeUpdate();
    }
  }
}
