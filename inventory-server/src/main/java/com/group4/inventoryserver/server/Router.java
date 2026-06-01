package com.group4.inventoryserver.server;

import com.group4.inventoryserver.dto.ErrorResponse;
import com.group4.inventoryserver.exception.ApiException;
import com.group4.inventoryserver.exception.ValidationException;
import com.group4.inventoryserver.handler.BaseHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Router implements HttpHandler {

  private static final Logger log = LoggerFactory.getLogger(Router.class);

  private final List<Route> routes = new ArrayList<>();

  public void register(String pathPrefix, BaseHandler handler) {
    routes.add(new Route(pathPrefix, handler));
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    String path = exchange.getRequestURI().getPath();

    for (Route route : routes) {
      if (path.equals(route.pathPrefix) || path.startsWith(route.pathPrefix + "/")) {
        try {
          RequestContext ctx = new RequestContext(exchange);
          route.handler.handle(ctx);
        } catch (ValidationException e) {
          sendValidationError(exchange, e);
        } catch (ApiException e) {
          sendError(exchange, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
          log.error("Unhandled exception on {}: {}", path, e.getMessage(), e);
          sendError(exchange, 500, "Internal Server Error");
        }
        return;
      }
    }

    sendError(exchange, 404, "Not Found");
  }

  private void sendError(HttpExchange exchange, int status, String message) throws IOException {
    ErrorResponse error = new ErrorResponse(status, message);
    JsonResponse.send(exchange, status, error);
  }

  private void sendValidationError(HttpExchange exchange, ValidationException e)
      throws IOException {
    ErrorResponse error = new ErrorResponse(e.getMessage(), e.getFieldErrors());
    JsonResponse.send(exchange, e.getStatusCode(), error);
  }

  private static class Route {
    final String pathPrefix;
    final BaseHandler handler;

    Route(String pathPrefix, BaseHandler handler) {
      this.pathPrefix = pathPrefix;
      this.handler = handler;
    }
  }
}
