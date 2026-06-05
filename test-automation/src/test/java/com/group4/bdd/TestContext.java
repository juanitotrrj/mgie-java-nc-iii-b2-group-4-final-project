package com.group4.bdd;

import com.group4.inventoryserver.ServerLauncher;
import com.group4.inventoryserver.testing.E2eHttpClient;
import java.util.HashMap;
import java.util.Map;

public class TestContext {

  private ServerLauncher serverLauncher;
  private E2eHttpClient httpClient;
  private String setupToken;
  private int lastStatus;
  private String lastBody;
  private String fixture = "initialized";
  private final Map<String, Object> vars = new HashMap<>();

  public ServerLauncher getServerLauncher() {
    return serverLauncher;
  }

  public void setServerLauncher(ServerLauncher serverLauncher) {
    this.serverLauncher = serverLauncher;
  }

  public E2eHttpClient getHttpClient() {
    return httpClient;
  }

  public void setHttpClient(E2eHttpClient httpClient) {
    this.httpClient = httpClient;
  }

  public String getSetupToken() {
    return setupToken;
  }

  public void setSetupToken(String setupToken) {
    this.setupToken = setupToken;
  }

  public int getLastStatus() {
    return lastStatus;
  }

  public void setLastStatus(int lastStatus) {
    this.lastStatus = lastStatus;
  }

  public String getLastBody() {
    return lastBody;
  }

  public void setLastBody(String lastBody) {
    this.lastBody = lastBody;
  }

  public String getFixture() {
    return fixture;
  }

  public void setFixture(String fixture) {
    this.fixture = fixture;
  }

  public Map<String, Object> getVars() {
    return vars;
  }

  public void setVar(String key, Object value) {
    vars.put(key, value);
  }

  @SuppressWarnings("unchecked")
  public <T> T getVar(String key) {
    return (T) vars.get(key);
  }

  public long getLongVar(String key) {
    Object value = vars.get(key);
    if (value instanceof Number) {
      return ((Number) value).longValue();
    }
    throw new IllegalStateException("Missing or invalid long var: " + key);
  }

  public int getIntVar(String key) {
    Object value = vars.get(key);
    if (value instanceof Number) {
      return ((Number) value).intValue();
    }
    throw new IllegalStateException("Missing or invalid int var: " + key);
  }
}
