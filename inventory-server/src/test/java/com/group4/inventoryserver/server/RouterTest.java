package com.group4.inventoryserver.server;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.group4.inventoryserver.exception.ApiException;
import com.group4.inventoryserver.handler.BaseHandler;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Before;
import org.junit.Test;

public class RouterTest {

  private Router router;
  private HttpExchange exchange;
  private ByteArrayOutputStream responseBody;
  private AtomicBoolean handlerInvoked;

  @Before
  public void setUp() {
    router = new Router();
    exchange = mock(HttpExchange.class);
    responseBody = new ByteArrayOutputStream();
    handlerInvoked = new AtomicBoolean(false);

    when(exchange.getRequestHeaders()).thenReturn(new Headers());
    when(exchange.getResponseHeaders()).thenReturn(new Headers());
    when(exchange.getResponseBody()).thenReturn(responseBody);
    when(exchange.getRequestMethod()).thenReturn("GET");
  }

  @Test
  public void register_matchesExactPath() throws IOException {
    router.register(
        "/api/test",
        new BaseHandler() {
          @Override
          public void handle(RequestContext ctx) throws IOException {
            handlerInvoked.set(true);
          }
        });
    when(exchange.getRequestURI()).thenReturn(URI.create("/api/test"));

    router.handle(exchange);

    assertTrue(handlerInvoked.get());
  }

  @Test
  public void register_matchesPathPrefix() throws IOException {
    router.register(
        "/api/products",
        new BaseHandler() {
          @Override
          public void handle(RequestContext ctx) throws IOException {
            handlerInvoked.set(true);
          }
        });
    when(exchange.getRequestURI()).thenReturn(URI.create("/api/products/42"));

    router.handle(exchange);

    assertTrue(handlerInvoked.get());
  }

  @Test
  public void unmatchedPath_returns404() throws IOException {
    when(exchange.getRequestURI()).thenReturn(URI.create("/api/unknown"));

    router.handle(exchange);

    verify(exchange).sendResponseHeaders(eq(404), anyLong());
    assertTrue(responseBody.toString().contains("Not Found"));
  }

  @Test
  public void apiException_returnsStatusCode() throws IOException {
    router.register(
        "/api/error",
        new BaseHandler() {
          @Override
          public void handle(RequestContext ctx) throws IOException {
            throw new ApiException(418, "Teapot");
          }
        });
    when(exchange.getRequestURI()).thenReturn(URI.create("/api/error"));

    router.handle(exchange);

    verify(exchange).sendResponseHeaders(eq(418), anyLong());
    assertTrue(responseBody.toString().contains("Teapot"));
  }
}
