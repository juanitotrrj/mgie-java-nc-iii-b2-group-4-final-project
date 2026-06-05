package com.group4.inventoryserver.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.group4.inventoryserver.server.RequestContext;
import com.group4.inventoryserver.testing.HandlerTestSupport;
import com.group4.inventoryserver.testing.HandlerTestSupport.CapturedResponse;
import java.io.IOException;
import org.junit.Test;

public class HealthHandlerTest {

  private final HealthHandler handler = new HealthHandler();

  @Test
  public void handleGet_runsAndReturnsResponse() throws IOException {
    RequestContext ctx = HandlerTestSupport.context("GET", "/api/health");
    handler.handle(ctx);
    CapturedResponse resp = HandlerTestSupport.captured(ctx);
    assertEquals(200, resp.statusCode);
    assertTrue(resp.body.contains("status"));
    assertTrue(resp.body.contains("healthy") || resp.body.contains("degraded"));
  }
}
