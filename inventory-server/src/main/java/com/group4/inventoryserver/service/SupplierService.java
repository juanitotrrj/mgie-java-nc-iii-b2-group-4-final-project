package com.group4.inventoryserver.service;

import com.group4.inventoryserver.dto.PaginatedResponse;
import com.group4.inventoryserver.dto.PaginationMeta;
import com.group4.inventoryserver.dto.PaginationParams;
import com.group4.inventoryserver.dto.supplier.SupplierCreateRequest;
import com.group4.inventoryserver.dto.supplier.SupplierData;
import com.group4.inventoryserver.dto.supplier.SupplierUpdateRequest;
import com.group4.inventoryserver.exception.ConflictException;
import com.group4.inventoryserver.exception.NotFoundException;
import com.group4.inventoryserver.repository.SupplierRepository;
import com.group4.inventoryserver.util.ValidationUtil;
import com.group4.inventoryserver.util.ValidationUtil.FieldError;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SupplierService {

  private static final Logger log = LoggerFactory.getLogger(SupplierService.class);

  private static final Set<String> VALID_STATUSES =
      new HashSet<>(Arrays.asList("Active", "Inactive"));

  private static final Set<String> VALID_TYPES =
      new HashSet<>(Arrays.asList("Local", "International", "Preferred", "Other"));

  private final SupplierRepository supplierRepository;

  public SupplierService() {
    this(new SupplierRepository());
  }

  SupplierService(SupplierRepository supplierRepository) {
    this.supplierRepository = supplierRepository;
  }

  public PaginatedResponse<SupplierData> list(PaginationParams params, String status, String type) {
    List<SupplierData> data =
        supplierRepository.findAll(
            params.getOffset(),
            params.getSize(),
            params.getSortBy(),
            params.getSortDir(),
            params.getSearch(),
            status,
            type);
    long total = supplierRepository.count(params.getSearch(), status, type);
    PaginationMeta meta =
        new PaginationMeta(
            params.getPage(), params.getSize(), total, params.getSortBy(), params.getSortDir());
    return new PaginatedResponse<>(data, meta);
  }

  public List<SupplierData> listAll(String search, String status, String type) {
    return supplierRepository.findAll(
        0, Integer.MAX_VALUE, "supplierName", "asc", search, status, type);
  }

  public SupplierData getById(long supplierId) {
    SupplierData supplier = supplierRepository.findById(supplierId);
    if (supplier == null) {
      throw new NotFoundException("Supplier not found.");
    }
    return supplier;
  }

  public SupplierData create(SupplierCreateRequest request, long authUserId) {
    validateCreateRequest(request);

    if (supplierRepository.existsByName(request.getSupplierName().trim())) {
      throw new ConflictException("Supplier name already exists.");
    }

    String code = supplierRepository.generateNextCode();
    String type = request.getType() != null ? request.getType().trim() : "Local";
    String status = request.getStatus() != null ? request.getStatus() : "Active";
    boolean preferred = request.getPreferred() != null ? request.getPreferred() : false;

    long supplierId =
        supplierRepository.insert(
            code,
            request.getSupplierName().trim(),
            trimOrNull(request.getContactPerson()),
            trimOrNull(request.getPhone()),
            trimOrNull(request.getEmail()),
            trimOrNull(request.getAddress()),
            type,
            preferred,
            status,
            authUserId);

    log.info("Supplier created [id={}, code={}] by user {}", supplierId, code, authUserId);
    return supplierRepository.findById(supplierId);
  }

  public SupplierData update(long supplierId, SupplierUpdateRequest request, long authUserId) {
    SupplierData existing = supplierRepository.findById(supplierId);
    if (existing == null) {
      throw new NotFoundException("Supplier not found.");
    }

    validateUpdateRequest(request);

    if (request.getSupplierName() != null
        && supplierRepository.existsByNameExcluding(request.getSupplierName().trim(), supplierId)) {
      throw new ConflictException("Supplier name already exists.");
    }

    String nameToUpdate =
        request.getSupplierName() != null ? request.getSupplierName().trim() : null;

    supplierRepository.update(
        supplierId,
        nameToUpdate,
        request.getContactPerson(),
        request.getPhone(),
        request.getEmail(),
        request.getAddress(),
        request.getType(),
        request.getPreferred(),
        request.getStatus(),
        authUserId);

    log.info("Supplier updated [id={}] by user {}", supplierId, authUserId);
    return supplierRepository.findById(supplierId);
  }

  public void deactivate(long supplierId, long authUserId) {
    SupplierData existing = supplierRepository.findById(supplierId);
    if (existing == null) {
      throw new NotFoundException("Supplier not found.");
    }

    supplierRepository.deactivate(supplierId, authUserId);
    log.info("Supplier deactivated [id={}] by user {}", supplierId, authUserId);
  }

  private void validateCreateRequest(SupplierCreateRequest request) {
    List<FieldError> errors = ValidationUtil.newErrorList();
    ValidationUtil.requireNonBlank(request.getSupplierName(), "supplierName", errors);
    ValidationUtil.requireMaxLength(request.getSupplierName(), "supplierName", 150, errors);
    ValidationUtil.requireMaxLength(request.getContactPerson(), "contactPerson", 150, errors);
    ValidationUtil.requireMaxLength(request.getPhone(), "phone", 60, errors);
    ValidationUtil.requireMaxLength(request.getEmail(), "email", 150, errors);
    ValidationUtil.requireMaxLength(request.getAddress(), "address", 500, errors);
    ValidationUtil.requireValidEmail(request.getEmail(), "email", errors);
    if (request.getType() != null && !VALID_TYPES.contains(request.getType())) {
      errors.add(
          new FieldError("type", "Type must be one of: Local, International, Preferred, Other."));
    }
    if (request.getStatus() != null && !VALID_STATUSES.contains(request.getStatus())) {
      errors.add(new FieldError("status", "Status must be Active or Inactive."));
    }
    ValidationUtil.throwIfErrors(errors);
  }

  private void validateUpdateRequest(SupplierUpdateRequest request) {
    List<FieldError> errors = ValidationUtil.newErrorList();
    ValidationUtil.requireMaxLength(request.getSupplierName(), "supplierName", 150, errors);
    ValidationUtil.requireMaxLength(request.getContactPerson(), "contactPerson", 150, errors);
    ValidationUtil.requireMaxLength(request.getPhone(), "phone", 60, errors);
    ValidationUtil.requireMaxLength(request.getEmail(), "email", 150, errors);
    ValidationUtil.requireMaxLength(request.getAddress(), "address", 500, errors);
    ValidationUtil.requireValidEmail(request.getEmail(), "email", errors);
    if (request.getType() != null && !VALID_TYPES.contains(request.getType())) {
      errors.add(
          new FieldError("type", "Type must be one of: Local, International, Preferred, Other."));
    }
    if (request.getStatus() != null && !VALID_STATUSES.contains(request.getStatus())) {
      errors.add(new FieldError("status", "Status must be Active or Inactive."));
    }
    ValidationUtil.throwIfErrors(errors);
  }

  private String trimOrNull(String value) {
    return value != null ? value.trim() : null;
  }
}
