package com.group4.inventoryserver.middleware;

import com.group4.inventoryserver.dto.ErrorResponse;
import com.group4.inventoryserver.repository.SystemInstallationRepository;
import com.group4.inventoryserver.server.JsonResponse;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SetupGuardFilter extends Filter {

  private static final long CACHE_TTL_MS = 5000;
  private static final Set<String> ALLOWED_DURING_SETUP =
      new HashSet<>(Arrays.asList("/api/health", "/api/setup", "/api/docs"));

  private final SystemInstallationRepository installationRepo = new SystemInstallationRepository();
  private volatile String cachedState;
  private volatile long lastFetchTime;

  @Override
  public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
    if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
      chain.doFilter(exchange);
      return;
    }

    String state = getCachedState();
    String path = exchange.getRequestURI().getPath();

    if (state == null) {
      chain.doFilter(exchange);
      return;
    }

    if ("INITIALIZED".equals(state)) {
      if (isSetupWritePath(path)) {
        ErrorResponse error =
            new ErrorResponse(403, "System is already initialized. Setup endpoints are locked.");
        JsonResponse.send(exchange, 403, error);
        return;
      }
      chain.doFilter(exchange);
      return;
    }

    if (isAllowedDuringSetup(path)) {
      chain.doFilter(exchange);
      return;
    }

    ErrorResponse error =
        new ErrorResponse(503, "System setup not complete. Please complete the setup wizard.");
    JsonResponse.send(exchange, 503, error);
  }

  private String getCachedState() {
    long now = System.currentTimeMillis();
    if (cachedState == null || (now - lastFetchTime) > CACHE_TTL_MS) {
      try {
        cachedState = installationRepo.getSetupState();
      } catch (Exception e) {
        return null;
      }
      lastFetchTime = now;
    }
    return cachedState;
  }

  private boolean isAllowedDuringSetup(String path) {
    for (String allowed : ALLOWED_DURING_SETUP) {
      if (path.equals(allowed) || path.startsWith(allowed + "/")) {
        return true;
      }
    }
    return false;
  }

  private boolean isSetupWritePath(String path) {
    return path.startsWith("/api/setup")
        && !path.equals("/api/setup/status")
        && !path.equals("/api/setup");
  }

  public void invalidateCache() {
    this.lastFetchTime = 0;
  }

  @Override
  public String description() {
    return "Setup Guard Filter";
  }
}
