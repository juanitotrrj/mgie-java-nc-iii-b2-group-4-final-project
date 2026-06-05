package com.group4.inventoryserver.middleware;

import static org.mockito.Mockito.*;

import com.group4.inventoryserver.repository.SystemInstallationRepository;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import org.junit.Before;
import org.junit.Test;

public class SetupGuardFilterTest {

  private SystemInstallationRepository installationRepo;
  private SetupGuardFilter filter;
  private HttpExchange exchange;
  private Filter.Chain chain;
  private ByteArrayOutputStream responseBody;

  @Before
  public void setUp() {
    installationRepo = mock(SystemInstallationRepository.class);
    filter = new SetupGuardFilter(installationRepo);
    exchange = mock(HttpExchange.class);
    chain = mock(Filter.Chain.class);
    responseBody = new ByteArrayOutputStream();

    when(exchange.getRequestHeaders()).thenReturn(new Headers());
    when(exchange.getResponseHeaders()).thenReturn(new Headers());
    when(exchange.getResponseBody()).thenReturn(responseBody);
  }

  @Test
  public void optionsRequest_passesThrough() throws IOException {
    when(exchange.getRequestMethod()).thenReturn("OPTIONS");
    when(exchange.getRequestURI()).thenReturn(URI.create("/api/setup/business-settings"));

    filter.doFilter(exchange, chain);

    verify(chain).doFilter(exchange);
    verify(installationRepo, never()).getSetupState();
  }

  @Test
  public void initialized_blocksSetupWritePaths() throws IOException {
    when(installationRepo.getSetupState()).thenReturn("INITIALIZED");
    when(exchange.getRequestMethod()).thenReturn("POST");
    when(exchange.getRequestURI()).thenReturn(URI.create("/api/setup/business-settings"));

    filter.doFilter(exchange, chain);

    verify(chain, never()).doFilter(exchange);
    verify(exchange).sendResponseHeaders(eq(403), anyLong());
  }

  @Test
  public void initialized_allowsSetupStatus() throws IOException {
    when(installationRepo.getSetupState()).thenReturn("INITIALIZED");
    when(exchange.getRequestMethod()).thenReturn("GET");
    when(exchange.getRequestURI()).thenReturn(URI.create("/api/setup/status"));

    filter.doFilter(exchange, chain);

    verify(chain).doFilter(exchange);
  }

  @Test
  public void initialized_allowsBusinessApi() throws IOException {
    when(installationRepo.getSetupState()).thenReturn("INITIALIZED");
    when(exchange.getRequestMethod()).thenReturn("GET");
    when(exchange.getRequestURI()).thenReturn(URI.create("/api/products"));

    filter.doFilter(exchange, chain);

    verify(chain).doFilter(exchange);
  }

  @Test
  public void notInitialized_blocksBusinessApi() throws IOException {
    when(installationRepo.getSetupState()).thenReturn("INFRA_READY_APP_SETUP_PENDING");
    when(exchange.getRequestMethod()).thenReturn("GET");
    when(exchange.getRequestURI()).thenReturn(URI.create("/api/products"));

    filter.doFilter(exchange, chain);

    verify(chain, never()).doFilter(exchange);
    verify(exchange).sendResponseHeaders(eq(503), anyLong());
  }

  @Test
  public void notInitialized_allowsSetupPaths() throws IOException {
    when(installationRepo.getSetupState()).thenReturn("APP_SETUP_IN_PROGRESS");
    when(exchange.getRequestMethod()).thenReturn("GET");
    when(exchange.getRequestURI()).thenReturn(URI.create("/api/setup/progress"));

    filter.doFilter(exchange, chain);

    verify(chain).doFilter(exchange);
  }

  @Test
  public void notInitialized_allowsHealth() throws IOException {
    when(installationRepo.getSetupState()).thenReturn("UNCONFIGURED");
    when(exchange.getRequestMethod()).thenReturn("GET");
    when(exchange.getRequestURI()).thenReturn(URI.create("/api/health"));

    filter.doFilter(exchange, chain);

    verify(chain).doFilter(exchange);
  }

  @Test
  public void repoFailure_passesThrough() throws IOException {
    when(installationRepo.getSetupState()).thenThrow(new RuntimeException("DB unavailable"));
    when(exchange.getRequestMethod()).thenReturn("GET");
    when(exchange.getRequestURI()).thenReturn(URI.create("/api/products"));

    filter.doFilter(exchange, chain);

    verify(chain).doFilter(exchange);
  }
}
