package com.group4.inventoryserver.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.group4.inventoryserver.server.RequestContext;
import com.group4.inventoryserver.testing.HandlerTestSupport;
import com.group4.inventoryserver.testing.HandlerTestSupport.CapturedResponse;
import java.io.IOException;
import org.junit.Test;

public class ContactHandlerTest {

  private final ContactHandler handler = new ContactHandler();

  @Test
  public void handleGet_returns200() throws IOException {
    RequestContext ctx = HandlerTestSupport.context("GET", "/api/public/contact");
    handler.handle(ctx);
    CapturedResponse resp = HandlerTestSupport.captured(ctx);
    assertEquals(200, resp.statusCode);
    assertTrue(resp.body.contains("support@inventoryms.com"));
  }
}
