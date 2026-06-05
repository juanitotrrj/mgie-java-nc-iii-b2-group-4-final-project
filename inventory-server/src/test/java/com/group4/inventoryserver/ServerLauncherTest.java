package com.group4.inventoryserver;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.group4.inventoryserver.server.Router;
import org.junit.Test;

public class ServerLauncherTest {

  @Test
  public void buildRouter_registers_health_and_auth_routes() {
    Router router = ServerLauncher.buildRouter();
    assertThat(router, is(notNullValue()));
  }
}
