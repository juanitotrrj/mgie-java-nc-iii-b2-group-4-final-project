package com.group4.inventoryserver.handler;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.group4.inventoryserver.exception.ApiException;
import com.group4.inventoryserver.exception.UnauthorizedException;
import com.group4.inventoryserver.server.RequestContext;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import org.junit.Test;

public class BaseHandlerTest {

  private BaseHandler createNoOpHandler() {
    return new BaseHandler() {};
  }

  @Test(expected = ApiException.class)
  public void handle_unsupportedMethod_throws405() throws IOException {
    BaseHandler handler = createNoOpHandler();
    RequestContext ctx = mock(RequestContext.class);
    when(ctx.getMethod()).thenReturn("TRACE");
    handler.handle(ctx);
  }

  @Test(expected = ApiException.class)
  public void handleGet_defaultImplementation_throws405() throws IOException {
    BaseHandler handler = createNoOpHandler();
    RequestContext ctx = mock(RequestContext.class);
    when(ctx.getMethod()).thenReturn("GET");
    handler.handle(ctx);
  }

  @Test(expected = ApiException.class)
  public void handlePost_defaultImplementation_throws405() throws IOException {
    BaseHandler handler = createNoOpHandler();
    RequestContext ctx = mock(RequestContext.class);
    when(ctx.getMethod()).thenReturn("POST");
    handler.handle(ctx);
  }

  @Test(expected = ApiException.class)
  public void handlePut_defaultImplementation_throws405() throws IOException {
    BaseHandler handler = createNoOpHandler();
    RequestContext ctx = mock(RequestContext.class);
    when(ctx.getMethod()).thenReturn("PUT");
    handler.handle(ctx);
  }

  @Test(expected = ApiException.class)
  public void handlePatch_defaultImplementation_throws405() throws IOException {
    BaseHandler handler = createNoOpHandler();
    RequestContext ctx = mock(RequestContext.class);
    when(ctx.getMethod()).thenReturn("PATCH");
    handler.handle(ctx);
  }

  @Test(expected = ApiException.class)
  public void handleDelete_defaultImplementation_throws405() throws IOException {
    BaseHandler handler = createNoOpHandler();
    RequestContext ctx = mock(RequestContext.class);
    when(ctx.getMethod()).thenReturn("DELETE");
    handler.handle(ctx);
  }

  @Test(expected = UnauthorizedException.class)
  public void getAuthUserId_throwsUnauthorized_whenAttributeMissing() {
    BaseHandler handler = createNoOpHandler();
    RequestContext ctx = mock(RequestContext.class);
    HttpExchange exchange = mock(HttpExchange.class);
    when(ctx.getExchange()).thenReturn(exchange);
    when(exchange.getAttribute("authUserId")).thenReturn(null);
    handler.getAuthUserId(ctx);
  }

  @Test
  public void getAuthUserId_returnsValue_whenSet() {
    BaseHandler handler = createNoOpHandler();
    RequestContext ctx = mock(RequestContext.class);
    HttpExchange exchange = mock(HttpExchange.class);
    when(ctx.getExchange()).thenReturn(exchange);
    when(exchange.getAttribute("authUserId")).thenReturn(42L);
    assertEquals(42L, handler.getAuthUserId(ctx));
  }

  @Test
  public void getAuthRole_returnsNull_whenAttributeMissing() {
    BaseHandler handler = createNoOpHandler();
    RequestContext ctx = mock(RequestContext.class);
    HttpExchange exchange = mock(HttpExchange.class);
    when(ctx.getExchange()).thenReturn(exchange);
    when(exchange.getAttribute("authRole")).thenReturn(null);
    assertNull(handler.getAuthRole(ctx));
  }

  @Test
  public void getAuthRole_returnsRoleName() {
    BaseHandler handler = createNoOpHandler();
    RequestContext ctx = mock(RequestContext.class);
    HttpExchange exchange = mock(HttpExchange.class);
    when(ctx.getExchange()).thenReturn(exchange);
    when(exchange.getAttribute("authRole")).thenReturn("Administrator");
    assertEquals("Administrator", handler.getAuthRole(ctx));
  }
}
