package com.group4.inventoryserver.handler;

import com.group4.inventoryserver.config.EnvConfig;
import com.group4.inventoryserver.dto.PaginatedResponse;
import com.group4.inventoryserver.dto.PaginationParams;
import com.group4.inventoryserver.dto.product.ProductCreateRequest;
import com.group4.inventoryserver.dto.product.ProductData;
import com.group4.inventoryserver.dto.product.ProductUpdateRequest;
import com.group4.inventoryserver.dto.product.StockMovementData;
import com.group4.inventoryserver.exception.ApiException;
import com.group4.inventoryserver.server.JsonResponse;
import com.group4.inventoryserver.server.RequestContext;
import com.group4.inventoryserver.service.ProductService;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ProductHandler extends BaseHandler {

  private static final Set<String> ALLOWED_SORT_FIELDS =
      new HashSet<>(
          Arrays.asList(
              "productId",
              "productCode",
              "productName",
              "categoryName",
              "quantity",
              "unitPrice",
              "status",
              "createdAt",
              "updatedAt"));

  private static final String BASE_PATH = EnvConfig.appContextPath() + "/products";

  private final ProductService productService = new ProductService();

  @Override
  public void handle(RequestContext ctx) throws IOException {
    String pathParam = ctx.getPathParam(BASE_PATH);
    String method = ctx.getMethod();

    if (pathParam == null || pathParam.isEmpty()) {
      handleCollection(ctx, method);
    } else if (pathParam.matches("\\d+/stock-movements")) {
      handleStockMovements(ctx, pathParam);
    } else if (pathParam.matches("\\d+")) {
      handleItem(ctx, method, Long.parseLong(pathParam));
    } else {
      throw new ApiException(404, "Not found.");
    }
  }

  private void handleCollection(RequestContext ctx, String method) throws IOException {
    switch (method) {
      case "GET":
        listProducts(ctx);
        break;
      case "POST":
        createProduct(ctx);
        break;
      default:
        throw new ApiException(405, "Method Not Allowed: " + method);
    }
  }

  private void handleItem(RequestContext ctx, String method, long productId) throws IOException {
    switch (method) {
      case "GET":
        getProduct(ctx, productId);
        break;
      case "PUT":
        updateProduct(ctx, productId);
        break;
      case "DELETE":
        deleteProduct(ctx, productId);
        break;
      default:
        throw new ApiException(405, "Method Not Allowed: " + method);
    }
  }

  private void handleStockMovements(RequestContext ctx, String pathParam) throws IOException {
    if (!"GET".equals(ctx.getMethod())) {
      throw new ApiException(405, "Method Not Allowed: " + ctx.getMethod());
    }
    requirePermission(ctx, "PRODUCT_READ");
    long productId = Long.parseLong(pathParam.split("/")[0]);
    PaginationParams params = PaginationParams.from(ctx, "createdAt", null);
    PaginatedResponse<StockMovementData> response =
        productService.getStockMovements(productId, params);
    JsonResponse.send(ctx.getExchange(), 200, response);
  }

  private void listProducts(RequestContext ctx) throws IOException {
    requirePermission(ctx, "PRODUCT_READ");
    PaginationParams params = PaginationParams.from(ctx, "productName", ALLOWED_SORT_FIELDS);
    String categoryIdParam = ctx.getQueryParam("categoryId");
    Long categoryId = categoryIdParam != null ? parseLongOrNull(categoryIdParam) : null;
    String status = ctx.getQueryParam("status");
    PaginatedResponse<ProductData> response = productService.list(params, categoryId, status);
    JsonResponse.send(ctx.getExchange(), 200, response);
  }

  private void createProduct(RequestContext ctx) throws IOException {
    requirePermission(ctx, "PRODUCT_WRITE");
    long authUserId = getAuthUserId(ctx);
    ProductCreateRequest request = parseBody(ctx, ProductCreateRequest.class);
    ProductData data = productService.create(request, authUserId);
    sendCreated(ctx, data);
  }

  private void getProduct(RequestContext ctx, long productId) throws IOException {
    requirePermission(ctx, "PRODUCT_READ");
    ProductData data = productService.getById(productId);
    sendSuccess(ctx, data);
  }

  private void updateProduct(RequestContext ctx, long productId) throws IOException {
    requirePermission(ctx, "PRODUCT_WRITE");
    long authUserId = getAuthUserId(ctx);
    String authRole = getAuthRole(ctx);
    ProductUpdateRequest request = parseBody(ctx, ProductUpdateRequest.class);
    ProductData data = productService.update(productId, request, authUserId, authRole);
    sendSuccess(ctx, data);
  }

  private void deleteProduct(RequestContext ctx, long productId) throws IOException {
    requirePermission(ctx, "PRODUCT_DELETE");
    long authUserId = getAuthUserId(ctx);
    productService.deactivate(productId, authUserId);
    sendSuccess(ctx, null);
  }

  private Long parseLongOrNull(String value) {
    if (value == null || value.trim().isEmpty()) return null;
    try {
      return Long.parseLong(value.trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
