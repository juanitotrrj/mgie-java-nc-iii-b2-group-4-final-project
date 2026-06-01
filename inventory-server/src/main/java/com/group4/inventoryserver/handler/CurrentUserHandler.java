package com.group4.inventoryserver.handler;

import com.group4.inventoryserver.dto.auth.UserProfile;
import com.group4.inventoryserver.server.RequestContext;
import com.group4.inventoryserver.service.AuthService;
import java.io.IOException;

public class CurrentUserHandler extends BaseHandler {

  private final AuthService authService = new AuthService();

  @Override
  protected void handleGet(RequestContext ctx) throws IOException {
    String token = extractToken(ctx);
    UserProfile profile = authService.getCurrentUser(token);
    sendSuccess(ctx, profile);
  }

  private String extractToken(RequestContext ctx) {
    String authHeader = ctx.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }
    return "";
  }
}
