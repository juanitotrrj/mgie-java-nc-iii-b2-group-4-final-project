package com.group4.inventoryserver.middleware;

import static org.mockito.Mockito.*;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import org.junit.Before;
import org.junit.Test;

public class LoggingFilterTest {

  private LoggingFilter loggingFilter;
  private HttpExchange exchange;
  private Filter.Chain chain;

  @Before
  public void setUp() {
    loggingFilter = new LoggingFilter();
    exchange = mock(HttpExchange.class);
    chain = mock(Filter.Chain.class);

    when(exchange.getRequestMethod()).thenReturn("GET");
    when(exchange.getRequestURI()).thenReturn(URI.create("/api/health"));
    when(exchange.getRemoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 54321));
    when(exchange.getResponseCode()).thenReturn(200);
  }

  @Test
  public void invokesFilterChain() throws IOException {
    loggingFilter.doFilter(exchange, chain);

    verify(chain).doFilter(exchange);
  }

  @Test
  public void invokesChain_evenWhenChainThrows() throws IOException {
    doThrow(new IOException("chain error")).when(chain).doFilter(exchange);

    try {
      loggingFilter.doFilter(exchange, chain);
    } catch (IOException ignored) {
    }

    verify(chain).doFilter(exchange);
  }

  @Test
  public void description_returnsNonEmpty() {
    org.junit.Assert.assertEquals("Request Logging Filter", loggingFilter.description());
  }
}
