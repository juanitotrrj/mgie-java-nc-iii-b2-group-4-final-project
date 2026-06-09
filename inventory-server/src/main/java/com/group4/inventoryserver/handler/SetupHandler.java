package com.group4.inventoryserver.handler;

import com.group4.inventoryserver.config.EnvConfig;
import com.group4.inventoryserver.dto.setup.BusinessSettingsRequest;
import com.group4.inventoryserver.dto.setup.PermissionSeedRequest;
import com.group4.inventoryserver.dto.setup.RoleSeedRequest;
import com.group4.inventoryserver.dto.setup.SetupFinishResponse;
import com.group4.inventoryserver.dto.setup.SetupProgressResponse;
import com.group4.inventoryserver.dto.setup.SetupStatusResponse;
import com.group4.inventoryserver.dto.setup.SetupUsersRequest;
import com.group4.inventoryserver.exception.ApiException;
import com.group4.inventoryserver.server.RequestContext;
import com.group4.inventoryserver.service.SetupService;
import java.io.IOException;

public class SetupHandler extends BaseHandler {

  private static final String BASE_PATH = EnvConfig.appContextPath() + "/setup";
  private final SetupService setupService = new SetupService();

  @Override
  public void handle(RequestContext ctx) throws IOException {
    String pathParam = ctx.getPathParam(BASE_PATH);
    String method = ctx.getMethod();

    if (pathParam == null || pathParam.isEmpty() || "status".equals(pathParam)) {
      if (!"GET".equals(method)) throw new ApiException(405, "Method Not Allowed");
      handleGetStatus(ctx);
      return;
    }

    switch (pathParam) {
      case "progress":
        requireSetupToken(ctx);
        if (!"GET".equals(method)) throw new ApiException(405, "Method Not Allowed");
        handleGetProgress(ctx);
        break;
      case "business-settings":
        requireSetupToken(ctx);
        if (!"PUT".equals(method)) throw new ApiException(405, "Method Not Allowed");
        handleBusinessSettings(ctx);
        break;
      case "roles/seed":
        requireSetupToken(ctx);
        if (!"POST".equals(method)) throw new ApiException(405, "Method Not Allowed");
        handleRolesSeed(ctx);
        break;
      case "permissions/seed":
        requireSetupToken(ctx);
        if (!"POST".equals(method)) throw new ApiException(405, "Method Not Allowed");
        handlePermissionsSeed(ctx);
        break;
      case "users":
        requireSetupToken(ctx);
        if (!"POST".equals(method)) throw new ApiException(405, "Method Not Allowed");
        handleCreateUsers(ctx);
        break;
      case "finish":
        requireSetupToken(ctx);
        if (!"POST".equals(method)) throw new ApiException(405, "Method Not Allowed");
        handleFinish(ctx);
        break;
      default:
        throw new ApiException(404, "Not Found: /api/setup/" + pathParam);
    }
  }

  private void handleGetStatus(RequestContext ctx) throws IOException {
    SetupStatusResponse status = setupService.getStatus();
    sendSuccess(ctx, status);
  }

  private void handleGetProgress(RequestContext ctx) throws IOException {
    SetupProgressResponse progress = setupService.getProgress();
    sendSuccess(ctx, progress);
  }

  private void handleBusinessSettings(RequestContext ctx) throws IOException {
    BusinessSettingsRequest request = parseBody(ctx, BusinessSettingsRequest.class);
    setupService.saveBusinessSettings(request);
    sendSuccess(ctx, "Business settings saved.");
  }

  private void handleRolesSeed(RequestContext ctx) throws IOException {
    RoleSeedRequest request = parseBody(ctx, RoleSeedRequest.class);
    setupService.seedRoles(request);
    sendSuccess(ctx, "Roles seeded successfully.");
  }

  private void handlePermissionsSeed(RequestContext ctx) throws IOException {
    PermissionSeedRequest request = parseBody(ctx, PermissionSeedRequest.class);
    setupService.seedPermissions(request);
    sendSuccess(ctx, "Permissions seeded successfully.");
  }

  private void handleCreateUsers(RequestContext ctx) throws IOException {
    SetupUsersRequest request = parseBody(ctx, SetupUsersRequest.class);
    setupService.createUsers(request);
    sendCreated(ctx, "Users created successfully.");
  }

  private void handleFinish(RequestContext ctx) throws IOException {
    SetupFinishResponse response = setupService.finish();
    sendSuccess(ctx, response);
  }

  private void requireSetupToken(RequestContext ctx) {
    String token = ctx.getExchange().getRequestHeaders().getFirst("X-Setup-Token");
    if (token == null || token.isEmpty()) {
      throw new ApiException(401, "Setup token required (X-Setup-Token header)");
    }
    if (!setupService.validateSetupToken(token)) {
      throw new ApiException(401, "Invalid or expired setup token");
    }
  }
}
