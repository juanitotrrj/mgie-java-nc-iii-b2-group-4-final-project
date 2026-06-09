package com.group4.inventoryserver.handler;

import com.group4.inventoryserver.exception.ForbiddenException;
import com.group4.inventoryserver.server.RequestContext;
import com.group4.inventoryserver.testing.HandlerTestSupport;
import java.io.IOException;
import java.util.HashSet;
import org.junit.Test;

public class DashboardHandlerTest {

  private final DashboardHandler handler = new DashboardHandler();

  @Test(expected = ForbiddenException.class)
  public void handleGet_adminWithoutPermission_throwsForbidden() throws IOException {
    RequestContext ctx =
        HandlerTestSupport.context("GET", "/api/dashboard/admin", null, new HashSet<String>());
    handler.handle(ctx);
  }
}
