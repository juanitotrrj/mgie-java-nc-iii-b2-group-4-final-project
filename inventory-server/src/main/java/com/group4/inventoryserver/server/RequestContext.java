package com.group4.inventoryserver.server;

import com.sun.net.httpserver.HttpExchange;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RequestContext {

  private final HttpExchange exchange;
  private final String method;
  private final String path;
  private final Map<String, String> queryParams;
  private final Map<String, Object> attributes;
  private String body;

  public RequestContext(HttpExchange exchange) {
    this.exchange = exchange;
    this.method = exchange.getRequestMethod().toUpperCase();
    URI uri = exchange.getRequestURI();
    this.path = uri.getPath();
    this.queryParams = parseQueryParams(uri.getRawQuery());
    this.attributes = new HashMap<>();
  }

  public HttpExchange getExchange() {
    return exchange;
  }

  public String getMethod() {
    return method;
  }

  public String getPath() {
    return path;
  }

  public String getBody() throws IOException {
    if (body == null) {
      StringBuilder sb = new StringBuilder();
      try (BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          sb.append(line);
        }
      }
      body = sb.toString();
    }
    return body;
  }

  public String getQueryParam(String name) {
    return queryParams.get(name);
  }

  public String getQueryParam(String name, String defaultValue) {
    return queryParams.getOrDefault(name, defaultValue);
  }

  public Map<String, String> getQueryParams() {
    return queryParams;
  }

  public String getHeader(String name) {
    return exchange.getRequestHeaders().getFirst(name);
  }

  public void setAttribute(String key, Object value) {
    attributes.put(key, value);
  }

  @SuppressWarnings("unchecked")
  public <T> T getAttribute(String key) {
    return (T) attributes.get(key);
  }

  public String getPathParam(String basePath) {
    if (path.length() > basePath.length()) {
      String remainder = path.substring(basePath.length());
      if (remainder.startsWith("/")) {
        remainder = remainder.substring(1);
      }
      return remainder.isEmpty() ? null : remainder;
    }
    return null;
  }

  private Map<String, String> parseQueryParams(String query) {
    Map<String, String> params = new HashMap<>();
    if (query == null || query.isEmpty()) {
      return params;
    }
    for (String pair : query.split("&")) {
      int idx = pair.indexOf('=');
      if (idx > 0) {
        String key = pair.substring(0, idx);
        String value = idx < pair.length() - 1 ? pair.substring(idx + 1) : "";
        params.put(key, value);
      } else {
        params.put(pair, "");
      }
    }
    return params;
  }
}
