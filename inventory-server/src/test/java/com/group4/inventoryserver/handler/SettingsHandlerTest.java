package com.group4.inventoryserver.handler;

import com.group4.inventoryserver.exception.ForbiddenException;
import com.group4.inventoryserver.server.RequestContext;
import com.group4.inventoryserver.testing.HandlerTestSupport;
import java.io.IOException;
import java.util.HashSet;
import org.junit.Test;

public class SettingsHandlerTest {

  private final SettingsHandler handler = new SettingsHandler();

  @Test(expected = ForbiddenException.class)
  public void handleGet_withoutPermission_throwsForbidden() throws IOException {
    RequestContext ctx =
        HandlerTestSupport.context("GET", "/api/settings", null, new HashSet<String>());
    handler.handle(ctx);
  }
}
