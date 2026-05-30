package com.group4.inventoryserver.middleware;

import com.group4.inventoryserver.config.EnvConfig;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.List;

public class CorsFilter extends Filter {

  @Override
  public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
    List<String> origins = EnvConfig.corsAllowedOrigins();
    String allowedOrigins = String.join(", ", origins);
    String allowedMethods = String.join(", ", EnvConfig.corsAllowedMethods());
    String allowedHeaders = String.join(", ", EnvConfig.corsAllowedHeaders());

    exchange.getResponseHeaders().set("Access-Control-Allow-Origin", allowedOrigins);
    exchange.getResponseHeaders().set("Access-Control-Allow-Methods", allowedMethods);
    exchange.getResponseHeaders().set("Access-Control-Allow-Headers", allowedHeaders);
    exchange.getResponseHeaders().set("Access-Control-Max-Age", "3600");

    if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
      exchange.sendResponseHeaders(204, -1);
      exchange.close();
      return;
    }

    chain.doFilter(exchange);
  }

  @Override
  public String description() {
    return "CORS Filter";
  }
}
