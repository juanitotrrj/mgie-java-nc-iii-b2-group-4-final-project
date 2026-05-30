package com.group4.inventoryserver.server;

import com.group4.inventoryserver.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public final class JsonResponse {

  private JsonResponse() {}

  public static void send(HttpExchange exchange, int statusCode, Object body) throws IOException {
    String json = JsonUtil.toJson(body);
    byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

    exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
    exchange.sendResponseHeaders(statusCode, bytes.length);

    try (OutputStream os = exchange.getResponseBody()) {
      os.write(bytes);
    }
  }

  public static void sendEmpty(HttpExchange exchange, int statusCode) throws IOException {
    exchange.sendResponseHeaders(statusCode, -1);
    exchange.close();
  }
}
