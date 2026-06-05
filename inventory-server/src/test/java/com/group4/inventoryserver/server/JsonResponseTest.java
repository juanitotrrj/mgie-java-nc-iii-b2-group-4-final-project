package com.group4.inventoryserver.server;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.group4.inventoryserver.dto.ApiResponse;
import com.group4.inventoryserver.dto.ErrorResponse;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;

public class JsonResponseTest {

  private HttpExchange exchange;
  private Headers responseHeaders;
  private ByteArrayOutputStream responseBody;

  @Before
  public void setUp() {
    exchange = mock(HttpExchange.class);
    responseHeaders = new Headers();
    responseBody = new ByteArrayOutputStream();

    when(exchange.getResponseHeaders()).thenReturn(responseHeaders);
    when(exchange.getResponseBody()).thenReturn(responseBody);
  }

  @Test
  public void send_writesJsonBody() throws IOException {
    ApiResponse<String> body = ApiResponse.success("ok");

    JsonResponse.send(exchange, 200, body);

    verify(exchange).sendResponseHeaders(200, responseBody.size());
    assertEquals("application/json; charset=UTF-8", responseHeaders.getFirst("Content-Type"));
    String json = responseBody.toString("UTF-8");
    assertTrue(json.contains("\"success\":true"));
    assertTrue(json.contains("\"data\":\"ok\""));
  }

  @Test
  public void sendEmpty_sendsNoBody() throws IOException {
    JsonResponse.sendEmpty(exchange, 204);

    verify(exchange).sendResponseHeaders(204, -1);
    verify(exchange).close();
  }

  @Test
  public void send_serializesErrorResponse() throws IOException {
    ErrorResponse error = new ErrorResponse(400, "Bad request");

    JsonResponse.send(exchange, 400, error);

    String json = responseBody.toString("UTF-8");
    assertTrue(json.contains("\"success\":false"));
    assertTrue(json.contains("Bad request"));
  }
}
