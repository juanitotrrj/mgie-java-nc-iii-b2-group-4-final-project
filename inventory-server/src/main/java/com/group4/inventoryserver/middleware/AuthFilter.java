package com.group4.inventoryserver.middleware;

import com.group4.inventoryserver.dto.ErrorResponse;
import com.group4.inventoryserver.server.JsonResponse;
import com.group4.inventoryserver.service.AuthService;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AuthFilter extends Filter {

  private static final Set<String> PUBLIC_PATHS =
      new HashSet<>(
          Arrays.asList(
              "/api/health",
              "/api/auth/login",
              "/api/public/welcome",
              "/api/public/about",
              "/api/public/contact",
              "/api/public/inquiries"));

  private final AuthService authService = new AuthService();

  @Override
  public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
    String path = exchange.getRequestURI().getPath();

    if (isPublicPath(path) || "OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
      chain.doFilter(exchange);
      return;
    }

    String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      sendUnauthorized(exchange, "Authentication required.");
      return;
    }

    String token = authHeader.substring(7);
    Map<String, Object> context = authService.validateSession(token);
    if (context == null) {
      sendUnauthorized(exchange, "Invalid or expired token.");
      return;
    }

    exchange.setAttribute("authUserId", context.get("userId"));
    exchange.setAttribute("authRole", context.get("role"));
    exchange.setAttribute("authUsername", context.get("username"));
    exchange.setAttribute("authSessionId", context.get("sessionId"));
    exchange.setAttribute("authToken", token);

    chain.doFilter(exchange);
  }

  private boolean isPublicPath(String path) {
    for (String publicPath : PUBLIC_PATHS) {
      if (path.equals(publicPath) || path.startsWith(publicPath + "/")) {
        return true;
      }
    }
    return false;
  }

  private void sendUnauthorized(HttpExchange exchange, String message) throws IOException {
    ErrorResponse error = new ErrorResponse(401, message);
    JsonResponse.send(exchange, 401, error);
  }

  @Override
  public String description() {
    return "Authentication Filter";
  }
}
