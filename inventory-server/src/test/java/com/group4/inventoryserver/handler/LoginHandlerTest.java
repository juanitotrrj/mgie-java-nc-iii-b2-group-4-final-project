package com.group4.inventoryserver.handler;

import com.group4.inventoryserver.exception.ApiException;
import com.group4.inventoryserver.server.RequestContext;
import com.group4.inventoryserver.testing.HandlerTestSupport;
import java.io.IOException;
import org.junit.Test;

public class LoginHandlerTest {

  private final LoginHandler handler = new LoginHandler();

  @Test(expected = ApiException.class)
  public void handlePost_withoutBody_throws400() throws IOException {
    RequestContext ctx = HandlerTestSupport.context("POST", "/api/auth/login");
    handler.handle(ctx);
  }
}
