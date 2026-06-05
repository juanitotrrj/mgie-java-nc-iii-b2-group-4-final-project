package com.group4.inventoryserver.middleware;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.net.URI;
import org.junit.Before;
import org.junit.Test;

public class CorsFilterTest {

  private CorsFilter corsFilter;
  private HttpExchange exchange;
  private Filter.Chain chain;
  private Headers responseHeaders;

  @Before
  public void setUp() {
    corsFilter = new CorsFilter();
    exchange = mock(HttpExchange.class);
    chain = mock(Filter.Chain.class);
    responseHeaders = new Headers();

    when(exchange.getResponseHeaders()).thenReturn(responseHeaders);
  }

  @Test
  public void addsCorsHeaders_onGetRequest() throws IOException {
    when(exchange.getRequestMethod()).thenReturn("GET");
    when(exchange.getRequestURI()).thenReturn(URI.create("/api/health"));

    corsFilter.doFilter(exchange, chain);

    assertNotNull(responseHeaders.getFirst("Access-Control-Allow-Origin"));
    assertNotNull(responseHeaders.getFirst("Access-Control-Allow-Methods"));
    assertNotNull(responseHeaders.getFirst("Access-Control-Allow-Headers"));
    assertEquals("3600", responseHeaders.getFirst("Access-Control-Max-Age"));
    verify(chain).doFilter(exchange);
  }

  @Test
  public void optionsRequest_returns204WithoutChain() throws IOException {
    when(exchange.getRequestMethod()).thenReturn("OPTIONS");
    when(exchange.getRequestURI()).thenReturn(URI.create("/api/products"));

    corsFilter.doFilter(exchange, chain);

    verify(exchange).sendResponseHeaders(204, -1);
    verify(exchange).close();
    verify(chain, never()).doFilter(exchange);
    assertNotNull(responseHeaders.getFirst("Access-Control-Allow-Origin"));
  }

  @Test
  public void description_returnsNonEmpty() {
    assertEquals("CORS Filter", corsFilter.description());
  }
}
