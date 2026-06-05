package com.group4.inventoryserver.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.group4.inventoryserver.server.RequestContext;
import com.group4.inventoryserver.testing.HandlerTestSupport;
import com.group4.inventoryserver.testing.HandlerTestSupport.CapturedResponse;
import java.io.IOException;
import org.junit.Test;

public class SwaggerHandlerTest {

  private final SwaggerHandler handler = new SwaggerHandler();

  @Test
  public void handleGet_docsPath_returns200() throws IOException {
    RequestContext ctx = HandlerTestSupport.context("GET", "/api/docs");
    handler.handle(ctx);
    CapturedResponse resp = HandlerTestSupport.captured(ctx);
    assertEquals(200, resp.statusCode);
    assertTrue(resp.body.contains("swagger") || resp.body.contains("Swagger"));
  }

  @Test
  public void handleGet_openapiYaml_usesRequestHostHeader() throws IOException {
    RequestContext ctx =
        HandlerTestSupport.contextWithHost("GET", "/api/docs/openapi.yaml", "localhost:8082");
    handler.handle(ctx);
    CapturedResponse resp = HandlerTestSupport.captured(ctx);
    assertEquals(200, resp.statusCode);
    assertTrue(resp.body.contains("url: http://localhost:8082"));
    assertTrue(resp.body.contains("url: http://localhost:8082/api"));
    assertFalse(resp.body.contains("url: http://localhost:8080"));
  }

  @Test
  public void patchServersBlock_replacesHardcodedUrls() throws IOException {
    String yaml =
        "info:\n  title: Test\nservers:\n- url: http://localhost:8080\n"
            + "  description: old\n"
            + "tags:\n- name: System\n";
    RequestContext ctx =
        HandlerTestSupport.contextWithHost("GET", "/api/docs/openapi.yaml", "myhost:9090");
    String patched = SwaggerHandler.patchServersBlock(yaml, ctx);
    assertTrue(patched.contains("url: http://myhost:9090"));
    assertTrue(patched.contains("url: http://myhost:9090/api"));
    assertFalse(patched.contains("localhost:8080"));
  }
}
