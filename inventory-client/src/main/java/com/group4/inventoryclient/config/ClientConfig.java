package com.group4.inventoryclient.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ClientConfig {

  private static final String DEFAULT_SERVER_URL = "http://localhost:8080/api";
  private final String serverUrl;

  public ClientConfig(String serverUrlOverride) {
    if (serverUrlOverride != null && !serverUrlOverride.isEmpty()) {
      this.serverUrl = serverUrlOverride;
    } else {
      this.serverUrl = loadFromProperties();
    }
  }

  public String getServerUrl() {
    return serverUrl;
  }

  private String loadFromProperties() {
    Properties props = new Properties();
    try (InputStream is = getClass().getClassLoader().getResourceAsStream("client.properties")) {
      if (is != null) {
        props.load(is);
        String url = props.getProperty("server.url");
        if (url != null && !url.isEmpty()) return url;
      }
    } catch (IOException e) {
      // ignore, use default
    }
    return DEFAULT_SERVER_URL;
  }
}
