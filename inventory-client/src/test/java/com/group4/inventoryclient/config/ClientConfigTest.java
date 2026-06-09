package com.group4.inventoryclient.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

public class ClientConfigTest {

  @Test
  public void constructor_usesOverrideWhenProvided() {
    ClientConfig config = new ClientConfig("http://custom:9090/api");
    assertThat(config.getServerUrl(), is("http://custom:9090/api"));
  }

  @Test
  public void constructor_loadsFromClasspathProperties() {
    ClientConfig config = new ClientConfig(null);
    assertThat(config.getServerUrl(), is("http://localhost:8080/api"));
  }

  @Test
  public void constructor_usesDefaultWhenOverrideEmpty() {
    ClientConfig config = new ClientConfig("");
    assertThat(config.getServerUrl(), is("http://localhost:8080/api"));
  }
}
