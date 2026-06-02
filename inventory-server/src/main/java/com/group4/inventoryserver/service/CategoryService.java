package com.group4.inventoryserver.service;

import com.group4.inventoryserver.dto.PaginatedResponse;
import com.group4.inventoryserver.dto.PaginationMeta;
import com.group4.inventoryserver.dto.PaginationParams;
import com.group4.inventoryserver.dto.category.CategoryCreateRequest;
import com.group4.inventoryserver.dto.category.CategoryData;
import com.group4.inventoryserver.dto.category.CategoryUpdateRequest;
import com.group4.inventoryserver.exception.ConflictException;
import com.group4.inventoryserver.exception.NotFoundException;
import com.group4.inventoryserver.repository.CategoryRepository;
import com.group4.inventoryserver.util.ValidationUtil;
import com.group4.inventoryserver.util.ValidationUtil.FieldError;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CategoryService {

  private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

  private static final Set<String> VALID_STATUSES =
      new HashSet<>(Arrays.asList("Active", "Inactive"));

  private final CategoryRepository categoryRepository;

  public CategoryService() {
    this(new CategoryRepository());
  }

  CategoryService(CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
  }

  public PaginatedResponse<CategoryData> list(PaginationParams params, String status, String type) {
    List<CategoryData> data =
        categoryRepository.findAll(
            params.getOffset(),
            params.getSize(),
            params.getSortBy(),
            params.getSortDir(),
            params.getSearch(),
            status,
            type);
    long total = categoryRepository.count(params.getSearch(), status, type);
    PaginationMeta meta =
        new PaginationMeta(
            params.getPage(), params.getSize(), total, params.getSortBy(), params.getSortDir());
    return new PaginatedResponse<>(data, meta);
  }

  public CategoryData getById(long categoryId) {
    CategoryData category = categoryRepository.findById(categoryId);
    if (category == null) {
      throw new NotFoundException("Category not found.");
    }
    return category;
  }

  public CategoryData create(CategoryCreateRequest request, long authUserId) {
    validateCreateRequest(request);

    if (categoryRepository.existsByName(request.getCategoryName().trim())) {
      throw new ConflictException("Category name already exists.");
    }

    String code = categoryRepository.generateNextCode();
    String type = request.getType() != null ? request.getType().trim() : "Product Group";
    String status = request.getStatus() != null ? request.getStatus() : "Active";

    long categoryId =
        categoryRepository.insert(
            code,
            request.getCategoryName().trim(),
            request.getDescription() != null ? request.getDescription().trim() : null,
            type,
            status,
            authUserId);

    log.info("Category created [id={}, code={}] by user {}", categoryId, code, authUserId);
    return categoryRepository.findById(categoryId);
  }

  public CategoryData update(long categoryId, CategoryUpdateRequest request, long authUserId) {
    CategoryData existing = categoryRepository.findById(categoryId);
    if (existing == null) {
      throw new NotFoundException("Category not found.");
    }

    validateUpdateRequest(request);

    if (request.getCategoryName() != null
        && categoryRepository.existsByNameExcluding(request.getCategoryName().trim(), categoryId)) {
      throw new ConflictException("Category name already exists.");
    }

    String nameToUpdate =
        request.getCategoryName() != null ? request.getCategoryName().trim() : null;
    String descToUpdate = request.getDescription() != null ? request.getDescription().trim() : null;

    categoryRepository.update(
        categoryId, nameToUpdate, descToUpdate, request.getType(), request.getStatus(), authUserId);

    log.info("Category updated [id={}] by user {}", categoryId, authUserId);
    return categoryRepository.findById(categoryId);
  }

  public void deactivate(long categoryId, long authUserId) {
    CategoryData existing = categoryRepository.findById(categoryId);
    if (existing == null) {
      throw new NotFoundException("Category not found.");
    }

    categoryRepository.deactivate(categoryId, authUserId);
    log.info("Category deactivated [id={}] by user {}", categoryId, authUserId);
  }

  private void validateCreateRequest(CategoryCreateRequest request) {
    List<FieldError> errors = ValidationUtil.newErrorList();
    ValidationUtil.requireNonBlank(request.getCategoryName(), "categoryName", errors);
    ValidationUtil.requireMaxLength(request.getCategoryName(), "categoryName", 120, errors);
    ValidationUtil.requireMaxLength(request.getDescription(), "description", 500, errors);
    ValidationUtil.requireMaxLength(request.getType(), "type", 80, errors);
    if (request.getStatus() != null && !VALID_STATUSES.contains(request.getStatus())) {
      errors.add(new FieldError("status", "Status must be Active or Inactive."));
    }
    ValidationUtil.throwIfErrors(errors);
  }

  private void validateUpdateRequest(CategoryUpdateRequest request) {
    List<FieldError> errors = ValidationUtil.newErrorList();
    ValidationUtil.requireMaxLength(request.getCategoryName(), "categoryName", 120, errors);
    ValidationUtil.requireMaxLength(request.getDescription(), "description", 500, errors);
    ValidationUtil.requireMaxLength(request.getType(), "type", 80, errors);
    if (request.getStatus() != null && !VALID_STATUSES.contains(request.getStatus())) {
      errors.add(new FieldError("status", "Status must be Active or Inactive."));
    }
    ValidationUtil.throwIfErrors(errors);
  }
}
