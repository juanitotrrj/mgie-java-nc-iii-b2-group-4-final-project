package com.group4.inventoryserver.handler;

import com.group4.inventoryserver.dto.ApiResponse;
import com.group4.inventoryserver.server.JsonResponse;
import com.group4.inventoryserver.server.RequestContext;
import com.group4.inventoryserver.service.AuthService;
import java.io.IOException;

public class LogoutHandler extends BaseHandler {

  private final AuthService authService = new AuthService();

  @Override
  protected void handlePost(RequestContext ctx) throws IOException {
    String token = extractToken(ctx);
    authService.logout(token);
    ApiResponse<Object> response = ApiResponse.success(null);
    JsonResponse.send(ctx.getExchange(), 200, response);
  }

  private String extractToken(RequestContext ctx) {
    String authHeader = ctx.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }
    return "";
  }
}
