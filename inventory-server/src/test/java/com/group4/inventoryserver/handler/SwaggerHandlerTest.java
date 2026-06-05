package com.group4.inventoryserver.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.group4.inventoryserver.server.RequestContext;
import com.group4.inventoryserver.testing.HandlerTestSupport;
import com.group4.inventoryserver.testing.HandlerTestSupport.CapturedResponse;
import java.io.IOException;
import org.junit.Test;

public class SwaggerHandlerTest {

  private final SwaggerHandler handler = new SwaggerHandler();

  @Test
  public void handleGet_docsPath_returns200() throws IOException {
    RequestContext ctx = HandlerTestSupport.context("GET", "/api/docs");
    handler.handle(ctx);
    CapturedResponse resp = HandlerTestSupport.captured(ctx);
    assertEquals(200, resp.statusCode);
    assertTrue(resp.body.contains("swagger") || resp.body.contains("Swagger"));
  }
}
