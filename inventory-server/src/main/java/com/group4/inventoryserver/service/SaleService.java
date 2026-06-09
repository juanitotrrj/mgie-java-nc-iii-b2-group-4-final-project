package com.group4.inventoryserver.service;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.config.EnvConfig;
import com.group4.inventoryserver.dto.PaginatedResponse;
import com.group4.inventoryserver.dto.PaginationMeta;
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
import com.group4.inventoryserver.util.ValidationUtil;
import com.group4.inventoryserver.util.ValidationUtil.FieldError;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaleService {

  private static final Logger log = LoggerFactory.getLogger(SaleService.class);

  private static final Set<String> VALID_PAYMENT_METHODS =
      new HashSet<>(Arrays.asList("Cash", "GCash", "Credit Card", "Bank Transfer", "E-Wallet"));

  private final SaleRepository saleRepository;

  public SaleService() {
    this(new SaleRepository());
  }

  SaleService(SaleRepository saleRepository) {
    this.saleRepository = saleRepository;
  }

  public PaginatedResponse<SaleData> list(
      PaginationParams params,
      String status,
      String paymentMethod,
      String dateFrom,
      String dateTo,
      Long cashierId) {
    List<SaleData> data =
        saleRepository.findAll(
            params.getOffset(),
            params.getSize(),
            params.getSortBy(),
            params.getSortDir(),
            params.getSearch(),
            status,
            paymentMethod,
            dateFrom,
            dateTo,
            cashierId);
    long total =
        saleRepository.count(
            params.getSearch(), status, paymentMethod, dateFrom, dateTo, cashierId);
    PaginationMeta meta =
        new PaginationMeta(
            params.getPage(), params.getSize(), total, params.getSortBy(), params.getSortDir());
    return new PaginatedResponse<>(data, meta);
  }

  public List<SaleData> listAll(
      String search,
      String status,
      String paymentMethod,
      String dateFrom,
      String dateTo,
      Long cashierId) {
    return saleRepository.findAll(
        0,
        Integer.MAX_VALUE,
        "createdAt",
        "desc",
        search,
        status,
        paymentMethod,
        dateFrom,
        dateTo,
        cashierId);
  }

  public SaleDetailData getById(long saleId, long authUserId, boolean isPrivileged) {
    SaleData header = saleRepository.findById(saleId);
    if (header == null) {
      throw new NotFoundException("Sale not found.");
    }
    if (!isPrivileged && header.getCashierId() != null && header.getCashierId() != authUserId) {
      throw new ForbiddenException("You can only view your own sales.");
    }
    List<SaleItemData> items = saleRepository.findItemsBySaleId(saleId);
    return toDetail(header, items);
  }

  public SaleDetailData create(SaleCreateRequest request, long authUserId) {
    validateCreateRequest(request);

    for (SaleItemRequest item : request.getItems()) {
      if (!saleRepository.productExistsAndActive(item.getProductId())) {
        throw new ValidationException(
            "Product with id " + item.getProductId() + " does not exist or is inactive.");
      }
    }

    String status = resolveStatus(request.getStatus());
    boolean deductStock = "Paid".equals(status);

    String invoiceNo = saleRepository.generateNextInvoiceNo();

    double subtotal = 0;
    double[] unitPrices = new double[request.getItems().size()];
    double[] lineTotals = new double[request.getItems().size()];
    for (int i = 0; i < request.getItems().size(); i++) {
      SaleItemRequest item = request.getItems().get(i);
      double unitPrice = saleRepository.getProductUnitPrice(item.getProductId());
      double lineTotal = item.getQuantity() * unitPrice;
      unitPrices[i] = unitPrice;
      lineTotals[i] = lineTotal;
      subtotal += lineTotal;
    }

    double taxAmount = 0;
    double totalAmount = subtotal + taxAmount;

    double amountReceived;
    double changeAmount;
    if ("Cash".equals(request.getPaymentMethod())) {
      amountReceived = request.getAmountReceived() != null ? request.getAmountReceived() : 0;
      changeAmount = amountReceived - totalAmount;
    } else {
      amountReceived = totalAmount;
      changeAmount = 0;
    }

    String customerName =
        request.getCustomerName() != null && !request.getCustomerName().trim().isEmpty()
            ? request.getCustomerName().trim()
            : "Walk-in Customer";

    try (Connection conn = DatabaseConfig.getConnection()) {
      conn.setAutoCommit(false);
      try {
        long saleId =
            saleRepository.insert(
                conn,
                invoiceNo,
                customerName,
                authUserId,
                request.getPaymentMethod(),
                status,
                subtotal,
                taxAmount,
                totalAmount,
                amountReceived,
                changeAmount,
                authUserId);

        for (int i = 0; i < request.getItems().size(); i++) {
          SaleItemRequest item = request.getItems().get(i);
          saleRepository.insertItem(
              conn, saleId, item.getProductId(), item.getQuantity(), unitPrices[i], lineTotals[i]);

          if (deductStock) {
            int currentQty = saleRepository.getProductQuantity(conn, item.getProductId());
            int newQty = currentQty - item.getQuantity();
            int reorderLevel = saleRepository.getProductReorderLevel(conn, item.getProductId());
            saleRepository.updateProductQuantity(conn, item.getProductId(), newQty, reorderLevel);
            createStockMovement(
                conn,
                item.getProductId(),
                saleId,
                invoiceNo,
                currentQty,
                -item.getQuantity(),
                newQty,
                "Stock decreased after sale.",
                authUserId);
          }
        }

        conn.commit();
        log.info("Sale created [id={}, invoice={}] by user {}", saleId, invoiceNo, authUserId);
        return getById(saleId, authUserId, true);
      } catch (Exception e) {
        conn.rollback();
        throw e;
      }
    } catch (ValidationException | ConflictException | NotFoundException e) {
      throw e;
    } catch (SQLException e) {
      throw new RuntimeException("Failed to create sale", e);
    } catch (Exception e) {
      if (e instanceof RuntimeException) throw (RuntimeException) e;
      throw new RuntimeException("Failed to create sale", e);
    }
  }

  public SaleDetailData update(
      long saleId, SaleUpdateRequest request, long authUserId, boolean isPrivileged) {
    SaleData existing = saleRepository.findById(saleId);
    if (existing == null) {
      throw new NotFoundException("Sale not found.");
    }
    if (!"Pending".equals(existing.getStatus())) {
      throw new ConflictException("Only Pending sales can be updated.");
    }
    if (!isPrivileged && existing.getCashierId() != null && existing.getCashierId() != authUserId) {
      throw new ForbiddenException("You can only update your own sales.");
    }

    validateUpdateRequest(request);

    for (SaleItemRequest item : request.getItems()) {
      if (!saleRepository.productExistsAndActive(item.getProductId())) {
        throw new ValidationException(
            "Product with id " + item.getProductId() + " does not exist or is inactive.");
      }
    }

    double subtotal = 0;
    for (SaleItemRequest item : request.getItems()) {
      double unitPrice = saleRepository.getProductUnitPrice(item.getProductId());
      subtotal += item.getQuantity() * unitPrice;
    }
    double taxAmount = 0;
    double totalAmount = subtotal + taxAmount;

    String paymentMethod = request.getPaymentMethod();
    double amountReceived;
    double changeAmount;
    if ("Cash".equals(paymentMethod)) {
      amountReceived = request.getAmountReceived() != null ? request.getAmountReceived() : 0;
      changeAmount = amountReceived - totalAmount;
    } else {
      amountReceived = totalAmount;
      changeAmount = 0;
    }

    String customerName =
        request.getCustomerName() != null && !request.getCustomerName().trim().isEmpty()
            ? request.getCustomerName().trim()
            : "Walk-in Customer";

    saleRepository.deleteItemsBySaleId(saleId);
    saleRepository.updateHeader(
        saleId,
        customerName,
        paymentMethod,
        subtotal,
        taxAmount,
        totalAmount,
        amountReceived,
        changeAmount,
        authUserId);

    for (SaleItemRequest item : request.getItems()) {
      double unitPrice = saleRepository.getProductUnitPrice(item.getProductId());
      double lineTotal = item.getQuantity() * unitPrice;
      saleRepository.insertItem(
          saleId, item.getProductId(), item.getQuantity(), unitPrice, lineTotal);
    }

    log.info("Sale updated [id={}] by user {}", saleId, authUserId);
    return getById(saleId, authUserId, true);
  }

  public void cancel(
      long saleId, CancelSaleRequest request, long authUserId, boolean isPrivileged) {
    SaleData existing = saleRepository.findById(saleId);
    if (existing == null) {
      throw new NotFoundException("Sale not found.");
    }

    if ("Cancelled".equals(existing.getStatus())) {
      throw new ConflictException("Sale is already cancelled.");
    }
    if ("Paid".equals(existing.getStatus()) && !EnvConfig.salesAllowCancelPaidInvoice()) {
      throw new ConflictException("Cancelling paid sales is not allowed.");
    }

    List<FieldError> errors = ValidationUtil.newErrorList();
    ValidationUtil.requireNonBlank(request.getReason(), "reason", errors);
    ValidationUtil.throwIfErrors(errors);

    boolean restoreStock = "Paid".equals(existing.getStatus());

    try (Connection conn = DatabaseConfig.getConnection()) {
      conn.setAutoCommit(false);
      try {
        saleRepository.cancel(conn, saleId, request.getReason().trim(), authUserId);

        if (restoreStock) {
          List<SaleItemData> items = saleRepository.findItemsBySaleId(conn, saleId);
          for (SaleItemData item : items) {
            int currentQty = saleRepository.getProductQuantity(conn, item.getProductId());
            int newQty = currentQty + item.getQuantity();
            int reorderLevel = saleRepository.getProductReorderLevel(conn, item.getProductId());
            saleRepository.updateProductQuantity(conn, item.getProductId(), newQty, reorderLevel);
            createReversalMovement(
                conn,
                item.getProductId(),
                saleId,
                existing.getInvoiceNo(),
                currentQty,
                item.getQuantity(),
                newQty,
                "Stock restored after sale cancellation.",
                authUserId);
          }
        }

        conn.commit();
        log.info(
            "Sale cancelled [id={}, invoice={}] by user {}",
            saleId,
            existing.getInvoiceNo(),
            authUserId);
      } catch (Exception e) {
        conn.rollback();
        throw e;
      }
    } catch (ConflictException | NotFoundException | ValidationException e) {
      throw e;
    } catch (SQLException e) {
      throw new RuntimeException("Failed to cancel sale", e);
    } catch (Exception e) {
      if (e instanceof RuntimeException) throw (RuntimeException) e;
      throw new RuntimeException("Failed to cancel sale", e);
    }
  }

  public ReceiptData getReceipt(long saleId, long authUserId, boolean isPrivileged) {
    SaleDetailData detail = getById(saleId, authUserId, isPrivileged);
    return new ReceiptData(
        detail.getInvoiceNo(),
        detail.getSaleDate(),
        detail.getCashierName(),
        detail.getCustomerName(),
        detail.getItems(),
        detail.getSubtotal(),
        detail.getTaxAmount(),
        detail.getTotalAmount(),
        detail.getPaymentMethod(),
        detail.getAmountReceived(),
        detail.getChangeAmount());
  }

  private void validateCreateRequest(SaleCreateRequest request) {
    List<FieldError> errors = ValidationUtil.newErrorList();
    if (request.getPaymentMethod() == null || request.getPaymentMethod().trim().isEmpty()) {
      errors.add(new FieldError("paymentMethod", "Payment method is required."));
    } else if (!VALID_PAYMENT_METHODS.contains(request.getPaymentMethod())) {
      errors.add(new FieldError("paymentMethod", "Invalid payment method."));
    }
    if (request.getItems() == null || request.getItems().isEmpty()) {
      errors.add(new FieldError("items", "At least one item is required."));
    } else {
      validateItems(request.getItems(), errors);
    }
    if (request.getStatus() != null && !request.getStatus().isEmpty()) {
      if (!"Paid".equals(request.getStatus()) && !"Pending".equals(request.getStatus())) {
        errors.add(new FieldError("status", "Status must be Paid or Pending."));
      }
      if ("Pending".equals(request.getStatus()) && !EnvConfig.salesAllowPendingInvoice()) {
        errors.add(new FieldError("status", "Pending invoices are not allowed."));
      }
    }
    ValidationUtil.throwIfErrors(errors);

    if ("Cash".equals(request.getPaymentMethod())) {
      if (request.getAmountReceived() == null || request.getAmountReceived() < 0) {
        errors.add(
            new FieldError("amountReceived", "Amount received is required for Cash payments."));
        ValidationUtil.throwIfErrors(errors);
      }
    }
  }

  private void validateUpdateRequest(SaleUpdateRequest request) {
    List<FieldError> errors = ValidationUtil.newErrorList();
    if (request.getPaymentMethod() == null || request.getPaymentMethod().trim().isEmpty()) {
      errors.add(new FieldError("paymentMethod", "Payment method is required."));
    } else if (!VALID_PAYMENT_METHODS.contains(request.getPaymentMethod())) {
      errors.add(new FieldError("paymentMethod", "Invalid payment method."));
    }
    if (request.getItems() == null || request.getItems().isEmpty()) {
      errors.add(new FieldError("items", "At least one item is required."));
    } else {
      validateItems(request.getItems(), errors);
    }
    ValidationUtil.throwIfErrors(errors);

    if ("Cash".equals(request.getPaymentMethod())) {
      if (request.getAmountReceived() == null || request.getAmountReceived() < 0) {
        errors.add(
            new FieldError("amountReceived", "Amount received is required for Cash payments."));
        ValidationUtil.throwIfErrors(errors);
      }
    }
  }

  private void validateItems(List<SaleItemRequest> items, List<FieldError> errors) {
    for (int i = 0; i < items.size(); i++) {
      SaleItemRequest item = items.get(i);
      String prefix = "items[" + i + "].";
      if (item.getProductId() == null) {
        errors.add(new FieldError(prefix + "productId", "Product is required."));
      }
      if (item.getQuantity() == null || item.getQuantity() < 1) {
        errors.add(new FieldError(prefix + "quantity", "Quantity must be at least 1."));
      }
    }
  }

  private String resolveStatus(String requestedStatus) {
    if (requestedStatus == null || requestedStatus.isEmpty()) {
      return "Paid";
    }
    return requestedStatus;
  }

  private SaleDetailData toDetail(SaleData header, List<SaleItemData> items) {
    return new SaleDetailData(
        header.getSaleId(),
        header.getInvoiceNo(),
        header.getCustomerName(),
        header.getCashierId(),
        header.getCashierName(),
        header.getSaleDate(),
        header.getPaymentMethod(),
        header.getStatus(),
        header.getItemCount(),
        header.getSubtotal(),
        header.getTaxAmount(),
        header.getTotalAmount(),
        header.getAmountReceived(),
        header.getChangeAmount(),
        header.getCancelReason(),
        header.getCreatedAt(),
        header.getUpdatedAt(),
        items);
  }

  private void createStockMovement(
      Connection conn,
      long productId,
      long saleId,
      String invoiceNo,
      int quantityBefore,
      int quantityChange,
      int quantityAfter,
      String remarks,
      Long createdBy)
      throws SQLException {
    String sql =
        "INSERT INTO stock_movements (product_id, movement_type, reference_type, reference_id, "
            + "reference_no, quantity_before, quantity_change, quantity_after, remarks, created_by) "
            + "VALUES (?, 'SALE_OUT', 'SALE', ?, ?, ?, ?, ?, ?, ?)";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, productId);
      ps.setLong(2, saleId);
      ps.setString(3, invoiceNo);
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

  private void createReversalMovement(
      Connection conn,
      long productId,
      long saleId,
      String invoiceNo,
      int quantityBefore,
      int quantityChange,
      int quantityAfter,
      String remarks,
      Long createdBy)
      throws SQLException {
    String sql =
        "INSERT INTO stock_movements (product_id, movement_type, reference_type, reference_id, "
            + "reference_no, quantity_before, quantity_change, quantity_after, remarks, created_by) "
            + "VALUES (?, 'SALE_REVERSAL', 'SALE', ?, ?, ?, ?, ?, ?, ?)";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, productId);
      ps.setLong(2, saleId);
      ps.setString(3, invoiceNo);
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
