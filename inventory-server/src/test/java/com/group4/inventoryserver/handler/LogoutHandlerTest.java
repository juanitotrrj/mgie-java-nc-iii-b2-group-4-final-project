package com.group4.inventoryserver.handler;

import com.group4.inventoryserver.exception.UnauthorizedException;
import com.group4.inventoryserver.server.RequestContext;
import com.group4.inventoryserver.testing.HandlerTestSupport;
import java.io.IOException;
import org.junit.Test;

public class LogoutHandlerTest {

  private final LogoutHandler handler = new LogoutHandler();

  @Test(expected = UnauthorizedException.class)
  public void handlePost_withoutAuth_throwsUnauthorized() throws IOException {
    RequestContext ctx = HandlerTestSupport.context("POST", "/api/auth/logout");
    handler.handle(ctx);
  }
}
