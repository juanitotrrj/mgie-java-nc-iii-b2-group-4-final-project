package com.group4.inventoryserver.handler;

import com.group4.inventoryserver.dto.auth.LoginData;
import com.group4.inventoryserver.dto.auth.LoginRequest;
import com.group4.inventoryserver.server.RequestContext;
import com.group4.inventoryserver.service.AuthService;
import java.io.IOException;

public class LoginHandler extends BaseHandler {

  private final AuthService authService = new AuthService();

  @Override
  protected void handlePost(RequestContext ctx) throws IOException {
    LoginRequest request = parseBody(ctx, LoginRequest.class);
    String ip = ctx.getExchange().getRemoteAddress().getAddress().getHostAddress();
    String userAgent = ctx.getHeader("User-Agent");
    LoginData data = authService.login(request, ip, userAgent);
    sendSuccess(ctx, data);
  }
}
