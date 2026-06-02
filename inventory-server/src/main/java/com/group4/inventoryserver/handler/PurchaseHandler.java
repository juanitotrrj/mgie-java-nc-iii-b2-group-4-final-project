package com.group4.inventoryserver.handler;

import com.group4.inventoryserver.config.EnvConfig;
import com.group4.inventoryserver.dto.PaginatedResponse;
import com.group4.inventoryserver.dto.PaginationParams;
import com.group4.inventoryserver.dto.purchase.CancelPurchaseRequest;
import com.group4.inventoryserver.dto.purchase.PurchaseCreateRequest;
import com.group4.inventoryserver.dto.purchase.PurchaseData;
import com.group4.inventoryserver.dto.purchase.PurchaseDetailData;
import com.group4.inventoryserver.dto.purchase.PurchaseUpdateRequest;
import com.group4.inventoryserver.dto.purchase.ReceivePurchaseRequest;
import com.group4.inventoryserver.exception.ApiException;
import com.group4.inventoryserver.server.JsonResponse;
import com.group4.inventoryserver.server.RequestContext;
import com.group4.inventoryserver.service.PurchaseService;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PurchaseHandler extends BaseHandler {

  private static final Set<String> ALLOWED_SORT_FIELDS =
      new HashSet<>(
          Arrays.asList(
              "purchaseId",
              "poNumber",
              "supplierName",
              "orderDate",
              "totalAmount",
              "status",
              "createdAt"));

  private static final String BASE_PATH = EnvConfig.appContextPath() + "/purchases";

  private final PurchaseService purchaseService = new PurchaseService();

  @Override
  public void handle(RequestContext ctx) throws IOException {
    String pathParam = ctx.getPathParam(BASE_PATH);
    String method = ctx.getMethod();

    if (pathParam == null || pathParam.isEmpty()) {
      handleCollection(ctx, method);
    } else if (pathParam.matches("\\d+/receive")) {
      handleReceive(ctx, pathParam);
    } else if (pathParam.matches("\\d+/cancel")) {
      handleCancel(ctx, pathParam);
    } else if (pathParam.matches("\\d+")) {
      handleItem(ctx, method, Long.parseLong(pathParam));
    } else {
      throw new ApiException(404, "Not found.");
    }
  }

  private void handleCollection(RequestContext ctx, String method) throws IOException {
    switch (method) {
      case "GET":
        listPurchases(ctx);
        break;
      case "POST":
        createPurchase(ctx);
        break;
      default:
        throw new ApiException(405, "Method Not Allowed: " + method);
    }
  }

  private void handleItem(RequestContext ctx, String method, long purchaseId) throws IOException {
    switch (method) {
      case "GET":
        getPurchase(ctx, purchaseId);
        break;
      case "PUT":
        updatePurchase(ctx, purchaseId);
        break;
      default:
        throw new ApiException(405, "Method Not Allowed: " + method);
    }
  }

  private void handleReceive(RequestContext ctx, String pathParam) throws IOException {
    if (!"POST".equals(ctx.getMethod())) {
      throw new ApiException(405, "Method Not Allowed: " + ctx.getMethod());
    }
    requirePermission(ctx, "PURCHASE_RECEIVE");
    long purchaseId = Long.parseLong(pathParam.split("/")[0]);
    long authUserId = getAuthUserId(ctx);
    ReceivePurchaseRequest request = parseBody(ctx, ReceivePurchaseRequest.class);
    purchaseService.receive(purchaseId, request, authUserId);
    sendSuccess(ctx, null);
  }

  private void handleCancel(RequestContext ctx, String pathParam) throws IOException {
    if (!"POST".equals(ctx.getMethod())) {
      throw new ApiException(405, "Method Not Allowed: " + ctx.getMethod());
    }
    requirePermission(ctx, "PURCHASE_WRITE");
    long purchaseId = Long.parseLong(pathParam.split("/")[0]);
    long authUserId = getAuthUserId(ctx);
    CancelPurchaseRequest request = parseBody(ctx, CancelPurchaseRequest.class);
    purchaseService.cancel(purchaseId, request, authUserId);
    sendSuccess(ctx, null);
  }

  private void listPurchases(RequestContext ctx) throws IOException {
    requirePermission(ctx, "PURCHASE_READ");
    PaginationParams params = PaginationParams.from(ctx, "createdAt", ALLOWED_SORT_FIELDS);
    String supplierIdParam = ctx.getQueryParam("supplierId");
    Long supplierId = supplierIdParam != null ? parseLongOrNull(supplierIdParam) : null;
    String status = ctx.getQueryParam("status");
    String dateFrom = ctx.getQueryParam("dateFrom");
    String dateTo = ctx.getQueryParam("dateTo");
    PaginatedResponse<PurchaseData> response =
        purchaseService.list(params, supplierId, status, dateFrom, dateTo);
    JsonResponse.send(ctx.getExchange(), 200, response);
  }

  private void createPurchase(RequestContext ctx) throws IOException {
    requirePermission(ctx, "PURCHASE_WRITE");
    long authUserId = getAuthUserId(ctx);
    PurchaseCreateRequest request = parseBody(ctx, PurchaseCreateRequest.class);
    PurchaseDetailData data = purchaseService.create(request, authUserId);
    sendCreated(ctx, data);
  }

  private void getPurchase(RequestContext ctx, long purchaseId) throws IOException {
    requirePermission(ctx, "PURCHASE_READ");
    PurchaseDetailData data = purchaseService.getById(purchaseId);
    sendSuccess(ctx, data);
  }

  private void updatePurchase(RequestContext ctx, long purchaseId) throws IOException {
    requirePermission(ctx, "PURCHASE_WRITE");
    long authUserId = getAuthUserId(ctx);
    PurchaseUpdateRequest request = parseBody(ctx, PurchaseUpdateRequest.class);
    PurchaseDetailData data = purchaseService.update(purchaseId, request, authUserId);
    sendSuccess(ctx, data);
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
