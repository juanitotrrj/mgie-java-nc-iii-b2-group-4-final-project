package com.group4.inventoryserver.server;

import com.group4.inventoryserver.config.EnvConfig;
import com.group4.inventoryserver.middleware.AuthFilter;
import com.group4.inventoryserver.middleware.CorsFilter;
import com.group4.inventoryserver.middleware.LoggingFilter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServerBootstrap {

  private static final Logger log = LoggerFactory.getLogger(HttpServerBootstrap.class);

  private final Router router;
  private HttpServer server;

  public HttpServerBootstrap(Router router) {
    this.router = router;
  }

  public void start() throws IOException {
    String host = EnvConfig.serverHost();
    int port = EnvConfig.serverPort();
    int backlog = EnvConfig.serverBacklog();

    server = HttpServer.create(new InetSocketAddress(host, port), backlog);

    String contextPath = EnvConfig.appContextPath();
    HttpContext context = server.createContext(contextPath, router);

    context.getFilters().add(new LoggingFilter());
    if (EnvConfig.corsEnabled()) {
      context.getFilters().add(new CorsFilter());
    }
    context.getFilters().add(new AuthFilter());

    server.setExecutor(
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2));
    server.start();

    log.info("HTTP server started on {}:{}{}", host, port, contextPath);
  }

  public void stop() {
    if (server != null) {
      server.stop(3);
      log.info("HTTP server stopped.");
    }
  }
}
