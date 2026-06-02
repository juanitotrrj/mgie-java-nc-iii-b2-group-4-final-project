package com.group4.inventoryserver.handler;

import com.group4.inventoryserver.config.EnvConfig;
import com.group4.inventoryserver.dto.PaginatedResponse;
import com.group4.inventoryserver.dto.PaginationParams;
import com.group4.inventoryserver.dto.user.ResetPasswordRequest;
import com.group4.inventoryserver.dto.user.UserCreateRequest;
import com.group4.inventoryserver.dto.user.UserData;
import com.group4.inventoryserver.dto.user.UserUpdateRequest;
import com.group4.inventoryserver.exception.ApiException;
import com.group4.inventoryserver.server.JsonResponse;
import com.group4.inventoryserver.server.RequestContext;
import com.group4.inventoryserver.service.UserService;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class UserHandler extends BaseHandler {

  private static final Set<String> ALLOWED_SORT_FIELDS =
      new HashSet<>(
          Arrays.asList(
              "userId",
              "userCode",
              "fullName",
              "username",
              "email",
              "role",
              "status",
              "createdAt"));

  private static final String BASE_PATH = EnvConfig.appContextPath() + "/users";
  private static final String PERMISSION = "USER_MANAGE";

  private final UserService userService = new UserService();

  @Override
  public void handle(RequestContext ctx) throws IOException {
    requirePermission(ctx, PERMISSION);

    String pathParam = ctx.getPathParam(BASE_PATH);
    String method = ctx.getMethod();

    if (pathParam == null || pathParam.isEmpty()) {
      handleCollection(ctx, method);
    } else if (pathParam.matches("\\d+/reset-password")) {
      handleResetPassword(ctx, pathParam);
    } else if (pathParam.matches("\\d+")) {
      handleItem(ctx, method, Long.parseLong(pathParam));
    } else {
      throw new ApiException(404, "Not found.");
    }
  }

  private void handleCollection(RequestContext ctx, String method) throws IOException {
    switch (method) {
      case "GET":
        listUsers(ctx);
        break;
      case "POST":
        createUser(ctx);
        break;
      default:
        throw new ApiException(405, "Method Not Allowed: " + method);
    }
  }

  private void handleItem(RequestContext ctx, String method, long userId) throws IOException {
    switch (method) {
      case "GET":
        getUser(ctx, userId);
        break;
      case "PUT":
        updateUser(ctx, userId);
        break;
      case "DELETE":
        deleteUser(ctx, userId);
        break;
      default:
        throw new ApiException(405, "Method Not Allowed: " + method);
    }
  }

  private void handleResetPassword(RequestContext ctx, String pathParam) throws IOException {
    if (!"POST".equals(ctx.getMethod())) {
      throw new ApiException(405, "Method Not Allowed: " + ctx.getMethod());
    }
    long userId = Long.parseLong(pathParam.split("/")[0]);
    long authUserId = getAuthUserId(ctx);
    ResetPasswordRequest request = parseBody(ctx, ResetPasswordRequest.class);
    userService.resetPassword(userId, request, authUserId);
    sendSuccess(ctx, null);
  }

  private void listUsers(RequestContext ctx) throws IOException {
    PaginationParams params = PaginationParams.from(ctx, "createdAt", ALLOWED_SORT_FIELDS);
    String role = ctx.getQueryParam("role");
    String status = ctx.getQueryParam("status");
    PaginatedResponse<UserData> response = userService.list(params, role, status);
    JsonResponse.send(ctx.getExchange(), 200, response);
  }

  private void createUser(RequestContext ctx) throws IOException {
    long authUserId = getAuthUserId(ctx);
    UserCreateRequest request = parseBody(ctx, UserCreateRequest.class);
    UserData data = userService.create(request, authUserId);
    sendCreated(ctx, data);
  }

  private void getUser(RequestContext ctx, long userId) throws IOException {
    UserData data = userService.getById(userId);
    sendSuccess(ctx, data);
  }

  private void updateUser(RequestContext ctx, long userId) throws IOException {
    long authUserId = getAuthUserId(ctx);
    UserUpdateRequest request = parseBody(ctx, UserUpdateRequest.class);
    UserData data = userService.update(userId, request, authUserId);
    sendSuccess(ctx, data);
  }

  private void deleteUser(RequestContext ctx, long userId) throws IOException {
    long authUserId = getAuthUserId(ctx);
    userService.deactivate(userId, authUserId);
    sendNoContent(ctx);
  }
}
