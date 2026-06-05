package com.group4.inventoryserver.handler;

import com.group4.inventoryserver.exception.UnauthorizedException;
import com.group4.inventoryserver.server.RequestContext;
import com.group4.inventoryserver.testing.HandlerTestSupport;
import java.io.IOException;
import org.junit.Test;

public class CurrentUserHandlerTest {

  private final CurrentUserHandler handler = new CurrentUserHandler();

  @Test(expected = UnauthorizedException.class)
  public void handleGet_withoutAuth_throwsUnauthorized() throws IOException {
    RequestContext ctx = HandlerTestSupport.context("GET", "/api/auth/me");
    handler.handle(ctx);
  }
}
