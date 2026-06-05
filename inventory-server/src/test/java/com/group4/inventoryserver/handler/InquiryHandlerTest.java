package com.group4.inventoryserver.handler;

import com.group4.inventoryserver.exception.ApiException;
import com.group4.inventoryserver.exception.ValidationException;
import com.group4.inventoryserver.server.RequestContext;
import com.group4.inventoryserver.testing.HandlerTestSupport;
import java.io.IOException;
import org.junit.Test;

public class InquiryHandlerTest {

  private final InquiryHandler handler = new InquiryHandler();

  @Test(expected = ApiException.class)
  public void handlePost_withoutBody_throws400() throws IOException {
    RequestContext ctx = HandlerTestSupport.context("POST", "/api/public/inquiry");
    handler.handle(ctx);
  }

  @Test(expected = ValidationException.class)
  public void handlePost_withEmptyFields_throwsValidationException() throws IOException {
    RequestContext ctx = HandlerTestSupport.context("POST", "/api/public/inquiry", "{}");
    handler.handle(ctx);
  }
}
