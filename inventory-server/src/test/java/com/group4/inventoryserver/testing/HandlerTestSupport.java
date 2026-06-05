package com.group4.inventoryserver.testing;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.group4.inventoryserver.server.RequestContext;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public final class HandlerTestSupport {

  private HandlerTestSupport() {}

  public static class CapturedResponse {
    public int statusCode;
    public String body;
    public Headers headers = new Headers();
  }

  public static RequestContext context(String method, String path) throws IOException {
    return context(method, path, null, null, null);
  }

  public static RequestContext contextWithHost(String method, String path, String hostHeader)
      throws IOException {
    return context(method, path, null, null, hostHeader);
  }

  public static RequestContext context(String method, String path, String jsonBody)
      throws IOException {
    return context(method, path, jsonBody, null, null);
  }

  public static RequestContext context(
      String method, String path, String jsonBody, Set<String> permissions) throws IOException {
    return context(method, path, jsonBody, permissions, null);
  }

  public static RequestContext context(
      String method, String path, String jsonBody, Set<String> permissions, String hostHeader)
      throws IOException {
    HttpExchange exchange = mock(HttpExchange.class);
    when(exchange.getRequestMethod()).thenReturn(method);
    when(exchange.getRequestURI()).thenReturn(URI.create(path));
    Headers requestHeaders = new Headers();
    if (hostHeader != null && !hostHeader.isEmpty()) {
      requestHeaders.add("Host", hostHeader);
    }
    when(exchange.getRequestHeaders()).thenReturn(requestHeaders);
    when(exchange.getResponseHeaders()).thenReturn(new Headers());
    when(exchange.getRemoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 12345));

    if (jsonBody != null) {
      InputStream in = new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8));
      when(exchange.getRequestBody()).thenReturn(in);
    } else {
      when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(new byte[0]));
    }

    CapturedResponse captured = new CapturedResponse();
    ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
    when(exchange.getResponseBody()).thenReturn(responseStream);
    doAnswer(
            new Answer<Void>() {
              @Override
              public Void answer(InvocationOnMock invocation) {
                captured.statusCode = invocation.getArgument(0);
                return null;
              }
            })
        .when(exchange)
        .sendResponseHeaders(anyInt(), anyLong());

    doAnswer(
            new Answer<Void>() {
              @Override
              public Void answer(InvocationOnMock invocation) {
                captured.body = new String(responseStream.toByteArray(), StandardCharsets.UTF_8);
                return null;
              }
            })
        .when(exchange)
        .close();

    if (permissions != null) {
      exchange.setAttribute("authPermissions", permissions);
      when(exchange.getAttribute("authPermissions")).thenReturn(permissions);
    }
    exchange.setAttribute("authUserId", 1L);
    when(exchange.getAttribute("authUserId")).thenReturn(1L);
    exchange.setAttribute("authRole", "Administrator");
    when(exchange.getAttribute("authRole")).thenReturn("Administrator");

    RequestContext ctx = new RequestContext(exchange);
    ctx.setAttribute("capturedResponse", captured);
    ctx.setAttribute("responseStream", responseStream);
    return ctx;
  }

  public static Set<String> allPermissions() {
    return new HashSet<>(
        java.util.Arrays.asList(
            "PRODUCT_READ",
            "PRODUCT_WRITE",
            "PRODUCT_DELETE",
            "CATEGORY_READ",
            "CATEGORY_WRITE",
            "CATEGORY_DELETE",
            "SUPPLIER_READ",
            "SUPPLIER_WRITE",
            "SUPPLIER_DELETE",
            "PURCHASE_READ",
            "PURCHASE_WRITE",
            "PURCHASE_RECEIVE",
            "SALE_READ",
            "SALE_WRITE",
            "SALE_CANCEL",
            "USER_MANAGE",
            "EXPORT_DATA",
            "REPORT_READ",
            "SETTINGS_MANAGE",
            "BACKUP_MANAGE",
            "AUDIT_LOG_READ",
            "STOCK_MOVEMENT_READ",
            "DASHBOARD_VIEW",
            "INVENTORY_CHANGE_REQUEST_CREATE",
            "INVENTORY_CHANGE_REQUEST_REVIEW"));
  }

  public static CapturedResponse captured(RequestContext ctx) {
    CapturedResponse captured = ctx.getAttribute("capturedResponse");
    if (captured.body == null) {
      ByteArrayOutputStream stream = ctx.getAttribute("responseStream");
      if (stream != null && stream.size() > 0) {
        captured.body = new String(stream.toByteArray(), StandardCharsets.UTF_8);
      }
    }
    return captured;
  }
}
