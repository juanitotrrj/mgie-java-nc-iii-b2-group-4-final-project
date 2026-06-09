package com.group4.inventoryserver.handler;

import com.group4.inventoryserver.config.EnvConfig;
import com.group4.inventoryserver.dto.PaginatedResponse;
import com.group4.inventoryserver.dto.PaginationParams;
import com.group4.inventoryserver.dto.supplier.SupplierCreateRequest;
import com.group4.inventoryserver.dto.supplier.SupplierData;
import com.group4.inventoryserver.dto.supplier.SupplierUpdateRequest;
import com.group4.inventoryserver.exception.ApiException;
import com.group4.inventoryserver.exception.ValidationException;
import com.group4.inventoryserver.server.JsonResponse;
import com.group4.inventoryserver.server.RequestContext;
import com.group4.inventoryserver.service.SupplierService;
import com.group4.inventoryserver.util.ExportUtil;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
    } else if ("export".equals(pathParam)) {
      handleExport(ctx);
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

  private void handleExport(RequestContext ctx) throws IOException {
    if (!"GET".equals(ctx.getMethod())) {
      throw new ApiException(405, "Method Not Allowed: " + ctx.getMethod());
    }
    requirePermission(ctx, "EXPORT_DATA");
    String search = ctx.getQueryParam("search");
    String status = ctx.getQueryParam("status");
    String type = ctx.getQueryParam("type");
    String format = ctx.getQueryParam("format");
    if (format == null || format.trim().isEmpty()) format = "csv";
    format = format.toLowerCase();
    if (!"csv".equals(format) && !"xlsx".equals(format)) {
      throw new ValidationException("Supported formats: csv, xlsx");
    }

    List<SupplierData> suppliers = supplierService.listAll(search, status, type);
    String[] headers = {
      "Supplier Code", "Supplier Name", "Type", "Contact Person", "Phone", "Email", "Status"
    };
    List<String[]> rows = new ArrayList<>();
    for (SupplierData s : suppliers) {
      rows.add(
          new String[] {
            s.getSupplierCode(),
            s.getSupplierName(),
            s.getType() != null ? s.getType() : "",
            s.getContactPerson() != null ? s.getContactPerson() : "",
            s.getPhone() != null ? s.getPhone() : "",
            s.getEmail() != null ? s.getEmail() : "",
            s.getStatus()
          });
    }

    byte[] content;
    String contentType;
    if ("xlsx".equals(format)) {
      content = ExportUtil.toXlsx("suppliers", headers, rows);
      contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    } else {
      content = ExportUtil.toCsv(headers, rows);
      contentType = "text/csv; charset=UTF-8";
    }
    String filename = "suppliers-export." + format;
    ctx.getExchange().getResponseHeaders().set("Content-Type", contentType);
    ctx.getExchange()
        .getResponseHeaders()
        .set("Content-Disposition", "attachment; filename=\"" + filename + "\"");
    ctx.getExchange().sendResponseHeaders(200, content.length);
    try (OutputStream os = ctx.getExchange().getResponseBody()) {
      os.write(content);
    }
  }
}
