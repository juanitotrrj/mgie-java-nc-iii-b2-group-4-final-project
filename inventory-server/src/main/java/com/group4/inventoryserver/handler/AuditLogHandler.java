package com.group4.inventoryserver.handler;

import com.group4.inventoryserver.config.EnvConfig;
import com.group4.inventoryserver.dto.PaginatedResponse;
import com.group4.inventoryserver.dto.PaginationMeta;
import com.group4.inventoryserver.dto.PaginationParams;
import com.group4.inventoryserver.dto.audit.AuditLogData;
import com.group4.inventoryserver.exception.ApiException;
import com.group4.inventoryserver.repository.AuditLogRepository;
import com.group4.inventoryserver.server.JsonResponse;
import com.group4.inventoryserver.server.RequestContext;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AuditLogHandler extends BaseHandler {

  private static final Set<String> ALLOWED_SORT_FIELDS =
      new HashSet<>(
          Arrays.asList("auditLogId", "dateTime", "createdAt", "username", "action", "module"));

  private static final String BASE_PATH = EnvConfig.appContextPath() + "/audit-logs";
  private static final String PERMISSION = "AUDIT_LOG_READ";

  private final AuditLogRepository auditLogRepository = new AuditLogRepository();

  @Override
  public void handle(RequestContext ctx) throws IOException {
    requirePermission(ctx, PERMISSION);
    String pathParam = ctx.getPathParam(BASE_PATH);
    if (pathParam != null && !pathParam.isEmpty()) {
      throw new ApiException(404, "Not found.");
    }
    if (!"GET".equals(ctx.getMethod())) {
      throw new ApiException(405, "Method Not Allowed: " + ctx.getMethod());
    }
    handleList(ctx);
  }

  private void handleList(RequestContext ctx) throws IOException {
    PaginationParams params = PaginationParams.from(ctx, "createdAt", ALLOWED_SORT_FIELDS);
    String userIdParam = ctx.getQueryParam("userId");
    Long userId = parseLongOrNull(userIdParam);
    String action = ctx.getQueryParam("action");
    String module = ctx.getQueryParam("module");
    String dateFrom = ctx.getQueryParam("dateFrom");
    String dateTo = ctx.getQueryParam("dateTo");

    List<AuditLogData> data =
        auditLogRepository.findAll(
            params.getOffset(),
            params.getSize(),
            params.getSortBy(),
            params.getSortDir(),
            params.getSearch(),
            userId,
            action,
            module,
            dateFrom,
            dateTo);
    long total =
        auditLogRepository.count(params.getSearch(), userId, action, module, dateFrom, dateTo);
    PaginationMeta meta =
        new PaginationMeta(
            params.getPage(), params.getSize(), total, params.getSortBy(), params.getSortDir());
    PaginatedResponse<AuditLogData> response = new PaginatedResponse<>(data, meta);
    JsonResponse.send(ctx.getExchange(), 200, response);
  }

  private Long parseLongOrNull(String value) {
    if (value == null || value.trim().isEmpty()) return null;
    try {
      return Long.valueOf(value.trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
