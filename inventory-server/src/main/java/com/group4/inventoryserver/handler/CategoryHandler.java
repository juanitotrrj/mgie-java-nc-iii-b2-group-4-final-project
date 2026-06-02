package com.group4.inventoryserver.handler;

import com.group4.inventoryserver.config.EnvConfig;
import com.group4.inventoryserver.dto.PaginatedResponse;
import com.group4.inventoryserver.dto.PaginationParams;
import com.group4.inventoryserver.dto.category.CategoryCreateRequest;
import com.group4.inventoryserver.dto.category.CategoryData;
import com.group4.inventoryserver.dto.category.CategoryUpdateRequest;
import com.group4.inventoryserver.exception.ApiException;
import com.group4.inventoryserver.server.JsonResponse;
import com.group4.inventoryserver.server.RequestContext;
import com.group4.inventoryserver.service.CategoryService;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CategoryHandler extends BaseHandler {

  private static final Set<String> ALLOWED_SORT_FIELDS =
      new HashSet<>(
          Arrays.asList(
              "categoryId",
              "categoryCode",
              "categoryName",
              "type",
              "productCount",
              "status",
              "createdAt",
              "updatedAt"));

  private static final String BASE_PATH = EnvConfig.appContextPath() + "/categories";

  private final CategoryService categoryService = new CategoryService();

  @Override
  public void handle(RequestContext ctx) throws IOException {
    String pathParam = ctx.getPathParam(BASE_PATH);
    String method = ctx.getMethod();

    if (pathParam == null || pathParam.isEmpty()) {
      handleCollection(ctx, method);
    } else if (pathParam.matches("\\d+")) {
      handleItem(ctx, method, Long.parseLong(pathParam));
    } else {
      throw new ApiException(404, "Not found.");
    }
  }

  private void handleCollection(RequestContext ctx, String method) throws IOException {
    switch (method) {
      case "GET":
        listCategories(ctx);
        break;
      case "POST":
        createCategory(ctx);
        break;
      default:
        throw new ApiException(405, "Method Not Allowed: " + method);
    }
  }

  private void handleItem(RequestContext ctx, String method, long categoryId) throws IOException {
    switch (method) {
      case "GET":
        getCategory(ctx, categoryId);
        break;
      case "PUT":
        updateCategory(ctx, categoryId);
        break;
      case "DELETE":
        deleteCategory(ctx, categoryId);
        break;
      default:
        throw new ApiException(405, "Method Not Allowed: " + method);
    }
  }

  private void listCategories(RequestContext ctx) throws IOException {
    requirePermission(ctx, "CATEGORY_READ");
    PaginationParams params = PaginationParams.from(ctx, "categoryName", ALLOWED_SORT_FIELDS);
    String status = ctx.getQueryParam("status");
    String type = ctx.getQueryParam("type");
    PaginatedResponse<CategoryData> response = categoryService.list(params, status, type);
    JsonResponse.send(ctx.getExchange(), 200, response);
  }

  private void createCategory(RequestContext ctx) throws IOException {
    requirePermission(ctx, "CATEGORY_WRITE");
    long authUserId = getAuthUserId(ctx);
    CategoryCreateRequest request = parseBody(ctx, CategoryCreateRequest.class);
    CategoryData data = categoryService.create(request, authUserId);
    sendCreated(ctx, data);
  }

  private void getCategory(RequestContext ctx, long categoryId) throws IOException {
    requirePermission(ctx, "CATEGORY_READ");
    CategoryData data = categoryService.getById(categoryId);
    sendSuccess(ctx, data);
  }

  private void updateCategory(RequestContext ctx, long categoryId) throws IOException {
    requirePermission(ctx, "CATEGORY_WRITE");
    long authUserId = getAuthUserId(ctx);
    CategoryUpdateRequest request = parseBody(ctx, CategoryUpdateRequest.class);
    CategoryData data = categoryService.update(categoryId, request, authUserId);
    sendSuccess(ctx, data);
  }

  private void deleteCategory(RequestContext ctx, long categoryId) throws IOException {
    requirePermission(ctx, "CATEGORY_DELETE");
    long authUserId = getAuthUserId(ctx);
    categoryService.deactivate(categoryId, authUserId);
    sendSuccess(ctx, null);
  }
}
