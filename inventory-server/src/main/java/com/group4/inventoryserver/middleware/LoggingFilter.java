package com.group4.inventoryserver.middleware;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingFilter extends Filter {

  private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

  @Override
  public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
    long startTime = System.currentTimeMillis();
    String method = exchange.getRequestMethod();
    String path = exchange.getRequestURI().getPath();
    String remoteAddr = exchange.getRemoteAddress().getAddress().getHostAddress();

    try {
      chain.doFilter(exchange);
    } finally {
      long duration = System.currentTimeMillis() - startTime;
      int responseCode = exchange.getResponseCode();
      log.info("{} {} {} - {} ({}ms)", method, path, responseCode, remoteAddr, duration);
    }
  }

  @Override
  public String description() {
    return "Request Logging Filter";
  }
}
