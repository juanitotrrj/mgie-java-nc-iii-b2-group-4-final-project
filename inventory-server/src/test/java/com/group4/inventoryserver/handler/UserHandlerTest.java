package com.group4.inventoryserver.handler;

import com.group4.inventoryserver.exception.ApiException;
import com.group4.inventoryserver.exception.ForbiddenException;
import com.group4.inventoryserver.server.RequestContext;
import com.group4.inventoryserver.testing.HandlerTestSupport;
import java.io.IOException;
import java.util.HashSet;
import org.junit.Test;

public class UserHandlerTest {

  private final UserHandler handler = new UserHandler();

  @Test(expected = ApiException.class)
  public void handle_unknownPath_throws404() throws IOException {
    RequestContext ctx =
        HandlerTestSupport.context(
            "GET", "/api/users/unknown", null, HandlerTestSupport.allPermissions());
    handler.handle(ctx);
  }

  @Test(expected = ForbiddenException.class)
  public void handleGet_withoutPermission_throwsForbidden() throws IOException {
    RequestContext ctx =
        HandlerTestSupport.context("GET", "/api/users", null, new HashSet<String>());
    handler.handle(ctx);
  }

  @Test(expected = ForbiddenException.class)
  public void handlePost_withoutPermission_throwsForbidden() throws IOException {
    RequestContext ctx =
        HandlerTestSupport.context("POST", "/api/users", "{}", new HashSet<String>());
    handler.handle(ctx);
  }
}
