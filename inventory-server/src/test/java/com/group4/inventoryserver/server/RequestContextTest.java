package com.group4.inventoryserver.server;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.junit.Before;
import org.junit.Test;

public class RequestContextTest {

  private HttpExchange exchange;
  private Headers requestHeaders;

  @Before
  public void setUp() {
    exchange = mock(HttpExchange.class);
    requestHeaders = new Headers();
    when(exchange.getRequestHeaders()).thenReturn(requestHeaders);
    when(exchange.getRequestMethod()).thenReturn("POST");
  }

  @Test
  public void parsesQueryParams() {
    when(exchange.getRequestURI())
        .thenReturn(URI.create("/api/products?page=2&size=25&search=keyboard"));

    RequestContext ctx = new RequestContext(exchange);

    assertEquals("2", ctx.getQueryParam("page"));
    assertEquals("25", ctx.getQueryParam("size"));
    assertEquals("keyboard", ctx.getQueryParam("search"));
    assertEquals("default", ctx.getQueryParam("missing", "default"));
  }

  @Test
  public void parsesEmptyQuery() {
    when(exchange.getRequestURI()).thenReturn(URI.create("/api/health"));

    RequestContext ctx = new RequestContext(exchange);

    assertTrue(ctx.getQueryParams().isEmpty());
    assertNull(ctx.getQueryParam("page"));
  }

  @Test
  public void getPathParam_extractsRemainder() {
    when(exchange.getRequestURI()).thenReturn(URI.create("/api/products/42/stock"));

    RequestContext ctx = new RequestContext(exchange);

    assertEquals("42/stock", ctx.getPathParam("/api/products"));
    assertNull(ctx.getPathParam("/api/products/42/stock"));
  }

  @Test
  public void getBody_readsRequestBody() throws IOException {
    when(exchange.getRequestURI()).thenReturn(URI.create("/api/auth/login"));
    String json = "{\"username\":\"admin\"}";
    when(exchange.getRequestBody())
        .thenReturn(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));

    RequestContext ctx = new RequestContext(exchange);

    assertEquals(json, ctx.getBody());
    assertEquals(json, ctx.getBody());
  }

  @Test
  public void attributes_storeAndRetrieve() {
    when(exchange.getRequestURI()).thenReturn(URI.create("/api/users"));

    RequestContext ctx = new RequestContext(exchange);
    ctx.setAttribute("authUserId", 7L);
    ctx.setAttribute("authRole", "Manager");

    assertEquals(Long.valueOf(7L), ctx.getAttribute("authUserId"));
    assertEquals("Manager", ctx.getAttribute("authRole"));
  }

  @Test
  public void getHeader_readsRequestHeader() {
    when(exchange.getRequestURI()).thenReturn(URI.create("/api/users"));
    requestHeaders.set("Authorization", "Bearer token");

    RequestContext ctx = new RequestContext(exchange);

    assertEquals("Bearer token", ctx.getHeader("Authorization"));
    assertEquals("POST", ctx.getMethod());
    assertEquals("/api/users", ctx.getPath());
  }
}
