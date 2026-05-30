package com.group4.inventoryserver.middleware;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
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

  @Override
  public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
    String path = exchange.getRequestURI().getPath();

    if (isPublicPath(path) || "OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
      chain.doFilter(exchange);
      return;
    }

    String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      // Token validation will be handled by individual handlers/services
      // This filter only ensures the header is present for protected routes
      exchange.setAttribute("authToken", authHeader.substring(7));
    }

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

  @Override
  public String description() {
    return "Authentication Filter";
  }
}
