package com.group4.inventoryserver.handler;

import com.group4.inventoryserver.config.EnvConfig;
import com.group4.inventoryserver.dto.PaginatedResponse;
import com.group4.inventoryserver.dto.PaginationParams;
import com.group4.inventoryserver.dto.supplier.SupplierCreateRequest;
import com.group4.inventoryserver.dto.supplier.SupplierData;
import com.group4.inventoryserver.dto.supplier.SupplierUpdateRequest;
import com.group4.inventoryserver.exception.ApiException;
import com.group4.inventoryserver.server.JsonResponse;
import com.group4.inventoryserver.server.RequestContext;
import com.group4.inventoryserver.service.SupplierService;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SupplierHandler extends BaseHandler {

  private static final Set<String> ALLOWED_SORT_FIELDS =
      new HashSet<>(
          Arrays.asList(
              "supplierId",
              "supplierCode",
              "supplierName",
              "type",
              "productCount",
              "status",
              "createdAt",
              "updatedAt"));

  private static final String BASE_PATH = EnvConfig.appContextPath() + "/suppliers";

  private final SupplierService supplierService = new SupplierService();

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
        listSuppliers(ctx);
        break;
      case "POST":
        createSupplier(ctx);
        break;
      default:
        throw new ApiException(405, "Method Not Allowed: " + method);
    }
  }

  private void handleItem(RequestContext ctx, String method, long supplierId) throws IOException {
    switch (method) {
      case "GET":
        getSupplier(ctx, supplierId);
        break;
      case "PUT":
        updateSupplier(ctx, supplierId);
        break;
      case "DELETE":
        deleteSupplier(ctx, supplierId);
        break;
      default:
        throw new ApiException(405, "Method Not Allowed: " + method);
    }
  }

  private void listSuppliers(RequestContext ctx) throws IOException {
    requirePermission(ctx, "SUPPLIER_READ");
    PaginationParams params = PaginationParams.from(ctx, "supplierName", ALLOWED_SORT_FIELDS);
    String status = ctx.getQueryParam("status");
    String type = ctx.getQueryParam("type");
    PaginatedResponse<SupplierData> response = supplierService.list(params, status, type);
    JsonResponse.send(ctx.getExchange(), 200, response);
  }

  private void createSupplier(RequestContext ctx) throws IOException {
    requirePermission(ctx, "SUPPLIER_WRITE");
    long authUserId = getAuthUserId(ctx);
    SupplierCreateRequest request = parseBody(ctx, SupplierCreateRequest.class);
    SupplierData data = supplierService.create(request, authUserId);
    sendCreated(ctx, data);
  }

  private void getSupplier(RequestContext ctx, long supplierId) throws IOException {
    requirePermission(ctx, "SUPPLIER_READ");
    SupplierData data = supplierService.getById(supplierId);
    sendSuccess(ctx, data);
  }

  private void updateSupplier(RequestContext ctx, long supplierId) throws IOException {
    requirePermission(ctx, "SUPPLIER_WRITE");
    long authUserId = getAuthUserId(ctx);
    SupplierUpdateRequest request = parseBody(ctx, SupplierUpdateRequest.class);
    SupplierData data = supplierService.update(supplierId, request, authUserId);
    sendSuccess(ctx, data);
  }

  private void deleteSupplier(RequestContext ctx, long supplierId) throws IOException {
    requirePermission(ctx, "SUPPLIER_DELETE");
    long authUserId = getAuthUserId(ctx);
    supplierService.deactivate(supplierId, authUserId);
    sendSuccess(ctx, null);
  }
}
