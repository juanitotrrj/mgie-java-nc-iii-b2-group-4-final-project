package com.group4.inventoryserver.handler;

import com.group4.inventoryserver.dto.ApiResponse;
import com.group4.inventoryserver.exception.ApiException;
import com.group4.inventoryserver.exception.ForbiddenException;
import com.group4.inventoryserver.exception.UnauthorizedException;
import com.group4.inventoryserver.server.JsonResponse;
import com.group4.inventoryserver.server.RequestContext;
import com.group4.inventoryserver.util.JsonUtil;
import java.io.IOException;
import java.util.Set;

public abstract class BaseHandler {

  public void handle(RequestContext ctx) throws IOException {
    String method = ctx.getMethod();
    switch (method) {
      case "GET":
        handleGet(ctx);
        break;
      case "POST":
        handlePost(ctx);
        break;
      case "PUT":
        handlePut(ctx);
        break;
      case "PATCH":
        handlePatch(ctx);
        break;
      case "DELETE":
        handleDelete(ctx);
        break;
      default:
        throw new ApiException(405, "Method Not Allowed: " + method);
    }
  }

  protected void handleGet(RequestContext ctx) throws IOException {
    throw new ApiException(405, "GET not supported");
  }

  protected void handlePost(RequestContext ctx) throws IOException {
    throw new ApiException(405, "POST not supported");
  }

  protected void handlePut(RequestContext ctx) throws IOException {
    throw new ApiException(405, "PUT not supported");
  }

  protected void handlePatch(RequestContext ctx) throws IOException {
    throw new ApiException(405, "PATCH not supported");
  }

  protected void handleDelete(RequestContext ctx) throws IOException {
    throw new ApiException(405, "DELETE not supported");
  }

  protected void sendSuccess(RequestContext ctx, Object data) throws IOException {
    ApiResponse<Object> response = ApiResponse.success(data);
    JsonResponse.send(ctx.getExchange(), 200, response);
  }

  protected void sendSuccess(RequestContext ctx, Object data, int statusCode) throws IOException {
    ApiResponse<Object> response = ApiResponse.success(data);
    JsonResponse.send(ctx.getExchange(), statusCode, response);
  }

  protected void sendCreated(RequestContext ctx, Object data) throws IOException {
    ApiResponse<Object> response = ApiResponse.success(data);
    JsonResponse.send(ctx.getExchange(), 201, response);
  }

  protected void sendNoContent(RequestContext ctx) throws IOException {
    JsonResponse.sendEmpty(ctx.getExchange(), 204);
  }

  protected <T> T parseBody(RequestContext ctx, Class<T> clazz) throws IOException {
    String body = ctx.getBody();
    if (body == null || body.trim().isEmpty()) {
      throw new ApiException(400, "Request body is required");
    }
    return JsonUtil.fromJson(body, clazz);
  }

  protected long getAuthUserId(RequestContext ctx) {
    Object userId = ctx.getExchange().getAttribute("authUserId");
    if (userId == null) {
      throw new UnauthorizedException("Authentication required.");
    }
    return ((Number) userId).longValue();
  }

  protected String getAuthRole(RequestContext ctx) {
    Object role = ctx.getExchange().getAttribute("authRole");
    return role != null ? role.toString() : null;
  }

  @SuppressWarnings("unchecked")
  protected void requirePermission(RequestContext ctx, String permissionCode) {
    Set<String> permissions = (Set<String>) ctx.getExchange().getAttribute("authPermissions");
    if (permissions == null || !permissions.contains(permissionCode)) {
      throw new ForbiddenException("You do not have permission to perform this action.");
    }
  }
}
