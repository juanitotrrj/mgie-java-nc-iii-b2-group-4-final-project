package com.group4.inventoryserver.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assume.assumeTrue;

import com.group4.inventoryserver.config.EnvConfig;
import com.group4.inventoryserver.testing.E2eHttpClient;
import org.junit.Test;

public class SwaggerApiIT extends ApiITBase {

  @Test
  public void docs_returns_html_when_swagger_enabled() throws Exception {
    assumeTrue("Swagger disabled in test env", EnvConfig.swaggerEnabled());
    E2eHttpClient.HttpResult result = client.get("/docs");
    assertThat(result.status, is(200));
    assertThat(result.body.contains("swagger") || result.body.contains("Swagger"), is(true));
  }

  @Test
  public void docs_is_not_registered_when_swagger_disabled() throws Exception {
    assumeTrue("Swagger enabled in test env", !EnvConfig.swaggerEnabled());
    E2eHttpClient.HttpResult result = client.get("/docs");
    assertThat(result.status, anyOf(is(404), is(405)));
  }
}
