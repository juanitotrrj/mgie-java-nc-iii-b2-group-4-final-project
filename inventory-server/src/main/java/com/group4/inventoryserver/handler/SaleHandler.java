package com.group4.inventoryserver.handler;

import com.group4.inventoryserver.config.EnvConfig;
import com.group4.inventoryserver.dto.PaginatedResponse;
import com.group4.inventoryserver.dto.PaginationParams;
import com.group4.inventoryserver.dto.sale.CancelSaleRequest;
import com.group4.inventoryserver.dto.sale.ReceiptData;
import com.group4.inventoryserver.dto.sale.SaleCreateRequest;
import com.group4.inventoryserver.dto.sale.SaleData;
import com.group4.inventoryserver.dto.sale.SaleDetailData;
import com.group4.inventoryserver.dto.sale.SaleUpdateRequest;
import com.group4.inventoryserver.exception.ApiException;
import com.group4.inventoryserver.exception.ValidationException;
import com.group4.inventoryserver.server.JsonResponse;
import com.group4.inventoryserver.server.RequestContext;
import com.group4.inventoryserver.service.SaleService;
import com.group4.inventoryserver.util.ExportUtil;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SaleHandler extends BaseHandler {

  private static final Set<String> ALLOWED_SORT_FIELDS =
      new HashSet<>(
          Arrays.asList(
              "saleId",
              "invoiceNo",
              "customerName",
              "saleDate",
              "totalAmount",
              "status",
              "paymentMethod",
              "createdAt"));

  private static final String BASE_PATH = EnvConfig.appContextPath() + "/sales";

  private final SaleService saleService = new SaleService();

  @Override
  public void handle(RequestContext ctx) throws IOException {
    String pathParam = ctx.getPathParam(BASE_PATH);
    String method = ctx.getMethod();

    if (pathParam == null || pathParam.isEmpty()) {
      handleCollection(ctx, method);
    } else if ("export".equals(pathParam)) {
      handleExport(ctx);
    } else if (pathParam.matches("\\d+/cancel")) {
      handleCancel(ctx, pathParam);
    } else if (pathParam.matches("\\d+/receipt")) {
      handleReceipt(ctx, pathParam);
    } else if (pathParam.matches("\\d+")) {
      handleItem(ctx, method, Long.parseLong(pathParam));
    } else {
      throw new ApiException(404, "Not found.");
    }
  }

  private void handleCollection(RequestContext ctx, String method) throws IOException {
    switch (method) {
      case "GET":
        listSales(ctx);
        break;
      case "POST":
        createSale(ctx);
        break;
      default:
        throw new ApiException(405, "Method Not Allowed: " + method);
    }
  }

  private void handleItem(RequestContext ctx, String method, long saleId) throws IOException {
    switch (method) {
      case "GET":
        getSale(ctx, saleId);
        break;
      case "PUT":
        updateSale(ctx, saleId);
        break;
      default:
        throw new ApiException(405, "Method Not Allowed: " + method);
    }
  }

  private void handleCancel(RequestContext ctx, String pathParam) throws IOException {
    if (!"POST".equals(ctx.getMethod())) {
      throw new ApiException(405, "Method Not Allowed: " + ctx.getMethod());
    }
    requirePermission(ctx, "SALE_CANCEL");
    long saleId = Long.parseLong(pathParam.split("/")[0]);
    long authUserId = getAuthUserId(ctx);
    boolean isPrivileged = isPrivilegedRole(ctx);
    CancelSaleRequest request = parseBody(ctx, CancelSaleRequest.class);
    saleService.cancel(saleId, request, authUserId, isPrivileged);
    sendSuccess(ctx, null);
  }

  private void handleReceipt(RequestContext ctx, String pathParam) throws IOException {
    if (!"GET".equals(ctx.getMethod())) {
      throw new ApiException(405, "Method Not Allowed: " + ctx.getMethod());
    }
    requirePermission(ctx, "SALE_READ");
    long saleId = Long.parseLong(pathParam.split("/")[0]);
    long authUserId = getAuthUserId(ctx);
    boolean isPrivileged = isPrivilegedRole(ctx);
    ReceiptData receipt = saleService.getReceipt(saleId, authUserId, isPrivileged);
    sendSuccess(ctx, receipt);
  }

  private void listSales(RequestContext ctx) throws IOException {
    requirePermission(ctx, "SALE_READ");
    PaginationParams params = PaginationParams.from(ctx, "createdAt", ALLOWED_SORT_FIELDS);
    String status = ctx.getQueryParam("status");
    String paymentMethod = ctx.getQueryParam("paymentMethod");
    String dateFrom = ctx.getQueryParam("dateFrom");
    String dateTo = ctx.getQueryParam("dateTo");

    Long cashierId = null;
    if (!isPrivilegedRole(ctx)) {
      cashierId = getAuthUserId(ctx);
    }

    PaginatedResponse<SaleData> response =
        saleService.list(params, status, paymentMethod, dateFrom, dateTo, cashierId);
    JsonResponse.send(ctx.getExchange(), 200, response);
  }

  private void createSale(RequestContext ctx) throws IOException {
    requirePermission(ctx, "SALE_WRITE");
    long authUserId = getAuthUserId(ctx);
    SaleCreateRequest request = parseBody(ctx, SaleCreateRequest.class);
    SaleDetailData data = saleService.create(request, authUserId);
    sendCreated(ctx, data);
  }

  private void getSale(RequestContext ctx, long saleId) throws IOException {
    requirePermission(ctx, "SALE_READ");
    long authUserId = getAuthUserId(ctx);
    boolean isPrivileged = isPrivilegedRole(ctx);
    SaleDetailData data = saleService.getById(saleId, authUserId, isPrivileged);
    sendSuccess(ctx, data);
  }

  private void updateSale(RequestContext ctx, long saleId) throws IOException {
    requirePermission(ctx, "SALE_WRITE");
    long authUserId = getAuthUserId(ctx);
    boolean isPrivileged = isPrivilegedRole(ctx);
    SaleUpdateRequest request = parseBody(ctx, SaleUpdateRequest.class);
    SaleDetailData data = saleService.update(saleId, request, authUserId, isPrivileged);
    sendSuccess(ctx, data);
  }

  private void handleExport(RequestContext ctx) throws IOException {
    if (!"GET".equals(ctx.getMethod())) {
      throw new ApiException(405, "Method Not Allowed: " + ctx.getMethod());
    }
    requirePermission(ctx, "EXPORT_DATA");
    String search = ctx.getQueryParam("search");
    String status = ctx.getQueryParam("status");
    String paymentMethod = ctx.getQueryParam("paymentMethod");
    String dateFrom = ctx.getQueryParam("dateFrom");
    String dateTo = ctx.getQueryParam("dateTo");
    String format = ctx.getQueryParam("format");
    if (format == null || format.trim().isEmpty()) format = "csv";
    format = format.toLowerCase();
    if (!"csv".equals(format) && !"xlsx".equals(format)) {
      throw new ValidationException("Supported formats: csv, xlsx");
    }

    Long cashierId = null;
    if (!isPrivilegedRole(ctx)) {
      cashierId = getAuthUserId(ctx);
    }

    List<SaleData> sales =
        saleService.listAll(search, status, paymentMethod, dateFrom, dateTo, cashierId);
    String[] headers = {
      "Invoice No",
      "Customer",
      "Sale Date",
      "Total Amount",
      "Payment Method",
      "Status",
      "Created At"
    };
    List<String[]> rows = new ArrayList<>();
    for (SaleData s : sales) {
      rows.add(
          new String[] {
            s.getInvoiceNo(),
            s.getCustomerName() != null ? s.getCustomerName() : "",
            s.getSaleDate() != null ? s.getSaleDate() : "",
            String.valueOf(s.getTotalAmount()),
            s.getPaymentMethod() != null ? s.getPaymentMethod() : "",
            s.getStatus(),
            s.getCreatedAt() != null ? s.getCreatedAt() : ""
          });
    }

    byte[] content;
    String contentType;
    if ("xlsx".equals(format)) {
      content = ExportUtil.toXlsx("sales", headers, rows);
      contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    } else {
      content = ExportUtil.toCsv(headers, rows);
      contentType = "text/csv; charset=UTF-8";
    }
    String filename = "sales-export." + format;
    ctx.getExchange().getResponseHeaders().set("Content-Type", contentType);
    ctx.getExchange()
        .getResponseHeaders()
        .set("Content-Disposition", "attachment; filename=\"" + filename + "\"");
    ctx.getExchange().sendResponseHeaders(200, content.length);
    try (OutputStream os = ctx.getExchange().getResponseBody()) {
      os.write(content);
    }
  }

  private boolean isPrivilegedRole(RequestContext ctx) {
    String role = getAuthRole(ctx);
    return "Administrator".equals(role) || "Manager".equals(role);
  }
}
