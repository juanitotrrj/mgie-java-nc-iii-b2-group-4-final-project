package com.group4.inventoryserver.handler;

import com.group4.inventoryserver.exception.ApiException;
import com.group4.inventoryserver.exception.ForbiddenException;
import com.group4.inventoryserver.server.RequestContext;
import com.group4.inventoryserver.testing.HandlerTestSupport;
import java.io.IOException;
import java.util.HashSet;
import org.junit.Test;

public class ProductHandlerTest {

  private final ProductHandler handler = new ProductHandler();

  @Test(expected = ApiException.class)
  public void handle_unknownPath_throws404() throws IOException {
    RequestContext ctx =
        HandlerTestSupport.context("GET", "/api/products/unknown", null, new HashSet<String>());
    handler.handle(ctx);
  }

  @Test(expected = ForbiddenException.class)
  public void handlePost_withoutPermission_throwsForbidden() throws IOException {
    RequestContext ctx =
        HandlerTestSupport.context(
            "POST", "/api/products", "{\"productCode\":\"P001\"}", new HashSet<String>());
    handler.handle(ctx);
  }

  @Test(expected = ForbiddenException.class)
  public void handleGet_withoutPermission_throwsForbidden() throws IOException {
    RequestContext ctx =
        HandlerTestSupport.context("GET", "/api/products", null, new HashSet<String>());
    handler.handle(ctx);
  }
}
