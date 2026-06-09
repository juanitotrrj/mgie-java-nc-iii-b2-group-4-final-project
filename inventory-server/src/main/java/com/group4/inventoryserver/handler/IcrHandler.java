package com.group4.inventoryserver.handler;

import com.group4.inventoryserver.config.EnvConfig;
import com.group4.inventoryserver.dto.PaginatedResponse;
import com.group4.inventoryserver.dto.PaginationParams;
import com.group4.inventoryserver.dto.icr.IcrApproveRequest;
import com.group4.inventoryserver.dto.icr.IcrCreateFields;
import com.group4.inventoryserver.dto.icr.IcrData;
import com.group4.inventoryserver.dto.icr.IcrDetailData;
import com.group4.inventoryserver.dto.icr.IcrRejectRequest;
import com.group4.inventoryserver.dto.icr.IcrSummaryData;
import com.group4.inventoryserver.exception.ApiException;
import com.group4.inventoryserver.server.JsonResponse;
import com.group4.inventoryserver.server.RequestContext;
import com.group4.inventoryserver.service.IcrService;
import com.group4.inventoryserver.util.FileUpload;
import com.group4.inventoryserver.util.MultipartData;
import com.group4.inventoryserver.util.MultipartParser;
import com.sun.net.httpserver.Headers;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class IcrHandler extends BaseHandler {

  private static final Set<String> ALLOWED_SORT_FIELDS =
      new HashSet<>(
          Arrays.asList(
              "requestId",
              "requestNo",
              "productName",
              "requestType",
              "status",
              "requestedAt",
              "requestedBy"));

  private static final String BASE_PATH = EnvConfig.appContextPath() + "/inventory-change-requests";
  private static final String PERM_CREATE = "INVENTORY_CHANGE_REQUEST_CREATE";
  private static final String PERM_REVIEW = "INVENTORY_CHANGE_REQUEST_REVIEW";

  private final IcrService icrService = new IcrService();

  @Override
  public void handle(RequestContext ctx) throws IOException {
    requireEitherPermission(ctx, PERM_CREATE, PERM_REVIEW);

    String pathParam = ctx.getPathParam(BASE_PATH);
    String method = ctx.getMethod();

    if (pathParam == null || pathParam.isEmpty()) {
      handleCollection(ctx, method);
    } else if ("my-summary".equals(pathParam)) {
      handleMySummary(ctx);
    } else if (pathParam.matches("\\d+/proof")) {
      handleProofDownload(ctx, pathParam);
    } else if (pathParam.matches("\\d+/approve")) {
      handleApprove(ctx, pathParam);
    } else if (pathParam.matches("\\d+/reject")) {
      handleReject(ctx, pathParam);
    } else if (pathParam.matches("\\d+")) {
      handleItem(ctx, Long.parseLong(pathParam));
    } else {
      throw new ApiException(404, "Not found.");
    }
  }

  private void handleCollection(RequestContext ctx, String method) throws IOException {
    switch (method) {
      case "GET":
        listRequests(ctx);
        break;
      case "POST":
        createRequest(ctx);
        break;
      default:
        throw new ApiException(405, "Method Not Allowed: " + method);
    }
  }

  private void handleItem(RequestContext ctx, long id) throws IOException {
    if (!"GET".equals(ctx.getMethod())) {
      throw new ApiException(405, "Method Not Allowed: " + ctx.getMethod());
    }
    long authUserId = getAuthUserId(ctx);
    boolean isReviewer = hasPermission(ctx, PERM_REVIEW);
    IcrDetailData data = icrService.getById(id, authUserId, isReviewer);
    sendSuccess(ctx, data);
  }

  private void handleMySummary(RequestContext ctx) throws IOException {
    if (!"GET".equals(ctx.getMethod())) {
      throw new ApiException(405, "Method Not Allowed: " + ctx.getMethod());
    }
    requirePermission(ctx, PERM_CREATE);
    long authUserId = getAuthUserId(ctx);
    IcrSummaryData data = icrService.getMySummary(authUserId);
    sendSuccess(ctx, data);
  }

  private void handleApprove(RequestContext ctx, String pathParam) throws IOException {
    if (!"POST".equals(ctx.getMethod())) {
      throw new ApiException(405, "Method Not Allowed: " + ctx.getMethod());
    }
    requirePermission(ctx, PERM_REVIEW);
    long id = Long.parseLong(pathParam.split("/")[0]);
    long authUserId = getAuthUserId(ctx);
    IcrApproveRequest request = parseBody(ctx, IcrApproveRequest.class);
    icrService.approve(id, request, authUserId);
    sendSuccess(ctx, null);
  }

  private void handleReject(RequestContext ctx, String pathParam) throws IOException {
    if (!"POST".equals(ctx.getMethod())) {
      throw new ApiException(405, "Method Not Allowed: " + ctx.getMethod());
    }
    requirePermission(ctx, PERM_REVIEW);
    long id = Long.parseLong(pathParam.split("/")[0]);
    long authUserId = getAuthUserId(ctx);
    IcrRejectRequest request = parseBody(ctx, IcrRejectRequest.class);
    icrService.reject(id, request, authUserId);
    sendSuccess(ctx, null);
  }

  private void handleProofDownload(RequestContext ctx, String pathParam) throws IOException {
    if (!"GET".equals(ctx.getMethod())) {
      throw new ApiException(405, "Method Not Allowed: " + ctx.getMethod());
    }
    long id = Long.parseLong(pathParam.split("/")[0]);
    long authUserId = getAuthUserId(ctx);
    boolean isReviewer = hasPermission(ctx, PERM_REVIEW);

    Map<String, Object> fileInfo = icrService.getProofFile(id, authUserId, isReviewer);
    String storagePath = (String) fileInfo.get("storagePath");
    String mimeType = (String) fileInfo.get("mimeType");
    String originalFilename = (String) fileInfo.get("originalFilename");

    File file = new File(storagePath);
    if (!file.exists()
        || !file.getCanonicalPath()
            .startsWith(new File(EnvConfig.inventoryProofUploadDir()).getCanonicalPath())) {
      throw new ApiException(404, "Proof file not found on disk.");
    }

    Headers headers = ctx.getExchange().getResponseHeaders();
    headers.set("Content-Type", mimeType);
    headers.set("Content-Disposition", "inline; filename=\"" + originalFilename + "\"");
    ctx.getExchange().sendResponseHeaders(200, file.length());

    try (FileInputStream fis = new FileInputStream(file);
        OutputStream os = ctx.getExchange().getResponseBody()) {
      byte[] buffer = new byte[8192];
      int bytesRead;
      while ((bytesRead = fis.read(buffer)) != -1) {
        os.write(buffer, 0, bytesRead);
      }
    }
  }

  private void listRequests(RequestContext ctx) throws IOException {
    PaginationParams params = PaginationParams.from(ctx, "requestedAt", ALLOWED_SORT_FIELDS);
    String status = ctx.getQueryParam("status");
    Long productId = parseLongOrNull(ctx.getQueryParam("productId"));
    Long requesterId = parseLongOrNull(ctx.getQueryParam("requesterId"));
    String dateFrom = ctx.getQueryParam("dateFrom");
    String dateTo = ctx.getQueryParam("dateTo");
    long authUserId = getAuthUserId(ctx);
    boolean isReviewer = hasPermission(ctx, PERM_REVIEW);

    PaginatedResponse<IcrData> response =
        icrService.list(
            params, status, productId, requesterId, dateFrom, dateTo, authUserId, isReviewer);
    JsonResponse.send(ctx.getExchange(), 200, response);
  }

  private void createRequest(RequestContext ctx) throws IOException {
    requirePermission(ctx, PERM_CREATE);
    long authUserId = getAuthUserId(ctx);

    String contentType = ctx.getExchange().getRequestHeaders().getFirst("Content-Type");
    MultipartData multipart =
        MultipartParser.parse(ctx.getExchange().getRequestBody(), contentType);

    IcrCreateFields fields =
        new IcrCreateFields(
            parseLongOrNull(multipart.getField("productId")),
            multipart.getField("requestType"),
            parseIntOrNull(multipart.getField("requestedQuantity")),
            parseIntOrNull(multipart.getField("quantityChange")),
            multipart.getField("reason"));

    FileUpload fileUpload = multipart.getFile();
    IcrDetailData data = icrService.create(fields, fileUpload, authUserId);
    sendCreated(ctx, data);
  }

  @SuppressWarnings("unchecked")
  private void requireEitherPermission(RequestContext ctx, String perm1, String perm2) {
    Set<String> permissions = (Set<String>) ctx.getExchange().getAttribute("authPermissions");
    if (permissions == null || (!permissions.contains(perm1) && !permissions.contains(perm2))) {
      throw new com.group4.inventoryserver.exception.ForbiddenException(
          "You do not have permission to perform this action.");
    }
  }

  @SuppressWarnings("unchecked")
  private boolean hasPermission(RequestContext ctx, String permissionCode) {
    Set<String> permissions = (Set<String>) ctx.getExchange().getAttribute("authPermissions");
    return permissions != null && permissions.contains(permissionCode);
  }

  private Long parseLongOrNull(String value) {
    if (value == null || value.trim().isEmpty()) return null;
    try {
      return Long.parseLong(value.trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private Integer parseIntOrNull(String value) {
    if (value == null || value.trim().isEmpty()) return null;
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
