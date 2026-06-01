package com.group4.inventoryserver.middleware;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import org.junit.Before;
import org.junit.Test;

public class AuthFilterTest {

  private AuthFilter authFilter;
  private HttpExchange exchange;
  private Filter.Chain chain;
  private Headers requestHeaders;
  private Headers responseHeaders;
  private ByteArrayOutputStream responseBody;

  @Before
  public void setUp() {
    authFilter = new AuthFilter();
    exchange = mock(HttpExchange.class);
    chain = mock(Filter.Chain.class);
    requestHeaders = new Headers();
    responseHeaders = new Headers();
    responseBody = new ByteArrayOutputStream();

    when(exchange.getRequestHeaders()).thenReturn(requestHeaders);
    when(exchange.getResponseHeaders()).thenReturn(responseHeaders);
    when(exchange.getResponseBody()).thenReturn(responseBody);
  }

  @Test
  public void publicPath_health_passesThrough() throws IOException {
    when(exchange.getRequestURI()).thenReturn(URI.create("/api/health"));
    when(exchange.getRequestMethod()).thenReturn("GET");

    authFilter.doFilter(exchange, chain);

    verify(chain).doFilter(exchange);
  }

  @Test
  public void publicPath_login_passesThrough() throws IOException {
    when(exchange.getRequestURI()).thenReturn(URI.create("/api/auth/login"));
    when(exchange.getRequestMethod()).thenReturn("POST");

    authFilter.doFilter(exchange, chain);

    verify(chain).doFilter(exchange);
  }

  @Test
  public void publicPath_welcome_passesThrough() throws IOException {
    when(exchange.getRequestURI()).thenReturn(URI.create("/api/public/welcome"));
    when(exchange.getRequestMethod()).thenReturn("GET");

    authFilter.doFilter(exchange, chain);

    verify(chain).doFilter(exchange);
  }

  @Test
  public void publicPath_inquiries_passesThrough() throws IOException {
    when(exchange.getRequestURI()).thenReturn(URI.create("/api/public/inquiries"));
    when(exchange.getRequestMethod()).thenReturn("POST");

    authFilter.doFilter(exchange, chain);

    verify(chain).doFilter(exchange);
  }

  @Test
  public void optionsRequest_passesThrough() throws IOException {
    when(exchange.getRequestURI()).thenReturn(URI.create("/api/protected/resource"));
    when(exchange.getRequestMethod()).thenReturn("OPTIONS");

    authFilter.doFilter(exchange, chain);

    verify(chain).doFilter(exchange);
  }

  @Test
  public void protectedPath_noAuthHeader_returns401() throws IOException {
    when(exchange.getRequestURI()).thenReturn(URI.create("/api/users"));
    when(exchange.getRequestMethod()).thenReturn("GET");

    authFilter.doFilter(exchange, chain);

    verify(chain, never()).doFilter(exchange);
    verify(exchange).sendResponseHeaders(eq(401), anyLong());
  }

  @Test
  public void protectedPath_malformedAuthHeader_returns401() throws IOException {
    when(exchange.getRequestURI()).thenReturn(URI.create("/api/users"));
    when(exchange.getRequestMethod()).thenReturn("GET");
    requestHeaders.set("Authorization", "Basic dXNlcjpwYXNz");

    authFilter.doFilter(exchange, chain);

    verify(chain, never()).doFilter(exchange);
    verify(exchange).sendResponseHeaders(eq(401), anyLong());
  }

  @Test
  public void protectedPath_invalidToken_returns401() throws IOException {
    when(exchange.getRequestURI()).thenReturn(URI.create("/api/users"));
    when(exchange.getRequestMethod()).thenReturn("GET");
    requestHeaders.set("Authorization", "Bearer invalid-garbage-token");

    authFilter.doFilter(exchange, chain);

    verify(chain, never()).doFilter(exchange);
    verify(exchange).sendResponseHeaders(eq(401), anyLong());
  }

  @Test
  public void description_returnsNonEmpty() {
    assertNotNull(authFilter.description());
    assertFalse(authFilter.description().isEmpty());
  }
}
