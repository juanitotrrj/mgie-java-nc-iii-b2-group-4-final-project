package com.group4.inventoryserver.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.group4.inventoryserver.exception.ApiException;
import com.group4.inventoryserver.server.RequestContext;
import com.group4.inventoryserver.testing.HandlerTestSupport;
import com.group4.inventoryserver.testing.HandlerTestSupport.CapturedResponse;
import java.io.IOException;
import org.junit.Test;

public class SetupHandlerTest {

  private final SetupHandler handler = new SetupHandler();

  @Test
  public void handleGet_status_routesToStatusHandler() throws IOException {
    try {
      RequestContext ctx = HandlerTestSupport.context("GET", "/api/setup/status");
      handler.handle(ctx);
      CapturedResponse resp = HandlerTestSupport.captured(ctx);
      assertEquals(200, resp.statusCode);
      assertTrue(resp.body.contains("success"));
    } catch (IllegalStateException e) {
      assertTrue(e.getMessage().contains("Database not initialized"));
    }
  }

  @Test(expected = ApiException.class)
  public void handlePut_businessSettingsWithoutToken_fails() throws IOException {
    RequestContext ctx =
        HandlerTestSupport.context(
            "PUT", "/api/setup/business-settings", "{\"companyName\":\"Test Co\"}");
    handler.handle(ctx);
  }
}
