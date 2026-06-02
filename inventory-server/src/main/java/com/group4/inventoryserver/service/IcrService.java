package com.group4.inventoryserver.service;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.config.EnvConfig;
import com.group4.inventoryserver.dto.PaginatedResponse;
import com.group4.inventoryserver.dto.PaginationMeta;
import com.group4.inventoryserver.dto.PaginationParams;
import com.group4.inventoryserver.dto.icr.IcrApproveRequest;
import com.group4.inventoryserver.dto.icr.IcrCreateFields;
import com.group4.inventoryserver.dto.icr.IcrData;
import com.group4.inventoryserver.dto.icr.IcrDetailData;
import com.group4.inventoryserver.dto.icr.IcrDetailData.ProofFileData;
import com.group4.inventoryserver.dto.icr.IcrRejectRequest;
import com.group4.inventoryserver.dto.icr.IcrSummaryData;
import com.group4.inventoryserver.exception.ApiException;
import com.group4.inventoryserver.exception.ConflictException;
import com.group4.inventoryserver.exception.NotFoundException;
import com.group4.inventoryserver.exception.ValidationException;
import com.group4.inventoryserver.repository.IcrRepository;
import com.group4.inventoryserver.util.FileUpload;
import com.group4.inventoryserver.util.ValidationUtil;
import com.group4.inventoryserver.util.ValidationUtil.FieldError;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IcrService {

  private static final Logger log = LoggerFactory.getLogger(IcrService.class);

  private static final Set<String> VALID_REQUEST_TYPES =
      new HashSet<>(Arrays.asList("Adjustment", "Correction", "Damage", "Lost Item", "Found Item"));

  private static final Set<String> ALLOWED_MIME_TYPES =
      new HashSet<>(Arrays.asList("image/jpeg", "image/png"));

  private final IcrRepository icrRepository;

  public IcrService() {
    this(new IcrRepository());
  }

  IcrService(IcrRepository icrRepository) {
    this.icrRepository = icrRepository;
  }

  public PaginatedResponse<IcrData> list(
      PaginationParams params,
      String status,
      Long productId,
      Long requesterId,
      String dateFrom,
      String dateTo,
      long authUserId,
      boolean isReviewer) {
    Long effectiveRequesterId = isReviewer ? requesterId : Long.valueOf(authUserId);

    List<IcrData> data =
        icrRepository.findAll(
            params.getOffset(),
            params.getSize(),
            params.getSortBy(),
            params.getSortDir(),
            params.getSearch(),
            status,
            productId,
            effectiveRequesterId,
            dateFrom,
            dateTo);
    long total =
        icrRepository.count(
            params.getSearch(), status, productId, effectiveRequesterId, dateFrom, dateTo);
    PaginationMeta meta =
        new PaginationMeta(
            params.getPage(), params.getSize(), total, params.getSortBy(), params.getSortDir());
    return new PaginatedResponse<>(data, meta);
  }

  public IcrDetailData getById(long id, long authUserId, boolean isReviewer) {
    IcrData icr = icrRepository.findById(id);
    if (icr == null) {
      throw new NotFoundException("Inventory change request not found.");
    }
    if (!isReviewer && icr.getRequestedById() != authUserId) {
      throw new NotFoundException("Inventory change request not found.");
    }

    Map<String, Object> file = icrRepository.findFileByIcrId(id);
    ProofFileData proofFile = null;
    if (file != null) {
      String downloadUrl =
          EnvConfig.appContextPath() + "/inventory-change-requests/" + id + "/proof";
      proofFile =
          new ProofFileData(
              (Long) file.get("fileId"),
              (String) file.get("originalFilename"),
              (String) file.get("mimeType"),
              (Long) file.get("fileSizeBytes"),
              downloadUrl);
    }

    return new IcrDetailData(
        icr.getRequestId(),
        icr.getRequestNo(),
        icr.getProductId(),
        icr.getProductCode(),
        icr.getProductName(),
        icr.getRequestType(),
        icr.getCurrentQuantity(),
        icr.getRequestedQuantity(),
        icr.getQuantityChange(),
        icr.getReason(),
        icr.getStatus(),
        icr.getRequestedById(),
        icr.getRequestedBy(),
        icr.getRequestedAt(),
        icr.getReviewedBy(),
        icr.getReviewedAt(),
        icr.getReviewNotes(),
        proofFile);
  }

  public IcrDetailData create(IcrCreateFields fields, FileUpload fileUpload, long authUserId) {
    validateCreateFields(fields);

    if (fileUpload != null && !ALLOWED_MIME_TYPES.contains(fileUpload.getContentType())) {
      throw new ValidationException("Proof image must be JPEG or PNG.");
    }

    long productId = fields.getProductId();
    int currentQty = getProductCurrentQuantity(productId);

    Integer requestedQty = fields.getRequestedQuantity();
    Integer qtyChange = fields.getQuantityChange();
    if (requestedQty != null && qtyChange == null) {
      qtyChange = requestedQty - currentQty;
    }

    String requestNo = icrRepository.generateNextRequestNo();
    long icrId =
        icrRepository.insert(
            requestNo,
            productId,
            fields.getRequestType(),
            currentQty,
            requestedQty,
            qtyChange,
            fields.getReason(),
            authUserId);

    if (fileUpload != null) {
      saveProofFile(icrId, fileUpload, authUserId);
    }

    log.info(
        "ICR created [id={}, no={}, product={}] by user {}",
        icrId,
        requestNo,
        productId,
        authUserId);
    return getById(icrId, authUserId, true);
  }

  public void approve(long id, IcrApproveRequest request, long authUserId) {
    IcrData icr = icrRepository.findById(id);
    if (icr == null) {
      throw new NotFoundException("Inventory change request not found.");
    }
    if (!"Pending".equals(icr.getStatus())) {
      throw new ConflictException("Only pending requests can be approved.");
    }

    int qtyChange = resolveQuantityChange(icr);
    String notes = request != null ? request.getNotes() : null;

    Connection conn = null;
    try {
      conn = DatabaseConfig.getConnection();
      conn.setAutoCommit(false);

      int currentQty = icrRepository.getProductQuantity(conn, icr.getProductId());
      int newQty = currentQty + qtyChange;
      if (newQty < 0) {
        throw new ValidationException("Approval would result in negative stock (" + newQty + ").");
      }

      icrRepository.updateProductQuantity(conn, icr.getProductId(), newQty);

      String movementType = qtyChange >= 0 ? "ADJUSTMENT_IN" : "ADJUSTMENT_OUT";
      long movementId =
          icrRepository.insertStockMovement(
              conn,
              icr.getProductId(),
              movementType,
              "INVENTORY_CHANGE_REQUEST",
              id,
              icr.getRequestNo(),
              currentQty,
              qtyChange,
              newQty,
              "Approved ICR: " + icr.getRequestNo(),
              authUserId);

      icrRepository.approve(conn, id, authUserId, notes, movementId);
      conn.commit();

      log.info(
          "ICR approved [id={}, no={}, qty change={}] by user {}",
          id,
          icr.getRequestNo(),
          qtyChange,
          authUserId);
    } catch (SQLException e) {
      rollback(conn);
      throw new RuntimeException("Failed to approve ICR", e);
    } finally {
      close(conn);
    }
  }

  public void reject(long id, IcrRejectRequest request, long authUserId) {
    if (request == null || request.getReason() == null || request.getReason().trim().isEmpty()) {
      throw new ValidationException("Rejection reason is required.");
    }

    IcrData icr = icrRepository.findById(id);
    if (icr == null) {
      throw new NotFoundException("Inventory change request not found.");
    }
    if (!"Pending".equals(icr.getStatus())) {
      throw new ConflictException("Only pending requests can be rejected.");
    }

    icrRepository.reject(id, authUserId, request.getReason().trim());
    log.info("ICR rejected [id={}, no={}] by user {}", id, icr.getRequestNo(), authUserId);
  }

  public IcrSummaryData getMySummary(long authUserId) {
    return icrRepository.countByStatus(authUserId);
  }

  public Map<String, Object> getProofFile(long id, long authUserId, boolean isReviewer) {
    IcrData icr = icrRepository.findById(id);
    if (icr == null) {
      throw new NotFoundException("Inventory change request not found.");
    }
    if (!isReviewer && icr.getRequestedById() != authUserId) {
      throw new NotFoundException("Inventory change request not found.");
    }

    Map<String, Object> file = icrRepository.findFileByIcrId(id);
    if (file == null) {
      throw new NotFoundException("No proof file uploaded for this request.");
    }
    return file;
  }

  // ─── Private helpers ──────────────────────────────────────────────────────────

  private void validateCreateFields(IcrCreateFields fields) {
    List<FieldError> errors = ValidationUtil.newErrorList();
    if (fields.getProductId() == null) {
      errors.add(new FieldError("productId", "Product ID is required."));
    }
    ValidationUtil.requireNonBlank(fields.getRequestType(), "requestType", errors);
    if (fields.getRequestType() != null
        && !fields.getRequestType().trim().isEmpty()
        && !VALID_REQUEST_TYPES.contains(fields.getRequestType().trim())) {
      errors.add(new FieldError("requestType", "Invalid request type."));
    }
    ValidationUtil.requireNonBlank(fields.getReason(), "reason", errors);
    ValidationUtil.requireMaxLength(fields.getReason(), "reason", 1000, errors);
    if (fields.getRequestedQuantity() == null && fields.getQuantityChange() == null) {
      errors.add(
          new FieldError(
              "quantityChange", "Either requestedQuantity or quantityChange must be provided."));
    }
    ValidationUtil.throwIfErrors(errors);
  }

  private int getProductCurrentQuantity(long productId) {
    try (Connection conn = DatabaseConfig.getConnection()) {
      int qty = icrRepository.getProductQuantity(conn, productId);
      if (qty == -1) {
        throw new NotFoundException("Product not found.");
      }
      return qty;
    } catch (SQLException e) {
      throw new RuntimeException("Failed to get product quantity", e);
    }
  }

  private int resolveQuantityChange(IcrData icr) {
    if (icr.getQuantityChange() != null) {
      return icr.getQuantityChange();
    }
    if (icr.getRequestedQuantity() != null) {
      return icr.getRequestedQuantity() - icr.getCurrentQuantity();
    }
    throw new ApiException(500, "ICR has no quantity information.");
  }

  private void saveProofFile(long icrId, FileUpload fileUpload, long authUserId) {
    String uploadDir = EnvConfig.inventoryProofUploadDir();
    File dir = new File(uploadDir);
    if (!dir.exists()) {
      dir.mkdirs();
    }

    String extension = fileUpload.getContentType().equals("image/png") ? ".png" : ".jpg";
    String storedFilename = UUID.randomUUID().toString() + extension;
    String storagePath = uploadDir + File.separator + storedFilename;

    try (FileOutputStream fos = new FileOutputStream(storagePath)) {
      fos.write(fileUpload.getData());
    } catch (IOException e) {
      throw new RuntimeException("Failed to save proof file", e);
    }

    icrRepository.insertFile(
        icrId,
        fileUpload.getFilename(),
        storedFilename,
        storagePath,
        fileUpload.getContentType(),
        fileUpload.getSize(),
        authUserId);
  }

  private void rollback(Connection conn) {
    if (conn != null) {
      try {
        conn.rollback();
      } catch (SQLException e) {
        log.error("Failed to rollback transaction", e);
      }
    }
  }

  private void close(Connection conn) {
    if (conn != null) {
      try {
        conn.setAutoCommit(true);
        conn.close();
      } catch (SQLException e) {
        log.error("Failed to close connection", e);
      }
    }
  }
}
