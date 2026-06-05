package com.group4.bdd.steps.common;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.group4.bdd.TestContext;
import com.group4.inventoryserver.ServerLauncher;
import com.group4.inventoryserver.testing.E2eHttpClient;
import java.util.Map;

public final class HttpSupport {

  private HttpSupport() {}

  public static void startServerIfNeeded(TestContext context) throws Exception {
    if (context.getServerLauncher() != null) return;
    ServerLauncher launcher = new ServerLauncher();
    launcher.start(false);
    context.setServerLauncher(launcher);
    String baseUrl = System.getProperty("e2e.server.url", launcher.getBaseUrl());
    E2eHttpClient client = new E2eHttpClient(baseUrl);
    if (context.getSetupToken() != null) {
      client.setSetupToken(context.getSetupToken());
    }
    context.setHttpClient(client);
  }

  public static void record(TestContext context, E2eHttpClient.HttpResult result) {
    context.setLastStatus(result.status);
    context.setLastBody(result.body);
  }

  public static E2eHttpClient.HttpResult login(
      TestContext context, String username, String role, String password) throws Exception {
    startServerIfNeeded(context);
    JsonObject body = new JsonObject();
    body.addProperty("username", username);
    body.addProperty("password", password);
    body.addProperty("role", role);
    E2eHttpClient.HttpResult result = context.getHttpClient().post("/auth/login", body.toString());
    if (result.status >= 200 && result.status < 300) {
      JsonObject root = JsonParser.parseString(result.body).getAsJsonObject();
      if (root.has("data") && root.get("data").isJsonObject()) {
        JsonObject data = root.getAsJsonObject("data");
        if (data.has("token")) {
          context.getHttpClient().setBearerToken(data.get("token").getAsString());
        }
      }
    }
    record(context, result);
    return result;
  }

  public static JsonObject parseBody(TestContext context) {
    String body = context.getLastBody();
    if (body == null || body.isEmpty()) {
      return new JsonObject();
    }
    return JsonParser.parseString(body).getAsJsonObject();
  }

  public static JsonObject dataObject(TestContext context) {
    JsonObject root = parseBody(context);
    if (root.has("data") && root.get("data").isJsonObject()) {
      return root.getAsJsonObject("data");
    }
    return root;
  }

  public static E2eHttpClient authenticatedClient(
      TestContext context, String username, String role, String password) throws Exception {
    startServerIfNeeded(context);
    String baseUrl =
        System.getProperty("e2e.server.url", context.getServerLauncher().getBaseUrl());
    E2eHttpClient client = new E2eHttpClient(baseUrl);
    JsonObject body = new JsonObject();
    body.addProperty("username", username);
    body.addProperty("password", password);
    body.addProperty("role", role);
    E2eHttpClient.HttpResult result = client.post("/auth/login", body.toString());
    if (result.status >= 200 && result.status < 300) {
      JsonObject data = result.jsonRoot().getAsJsonObject("data");
      if (data.has("token")) {
        client.setBearerToken(data.get("token").getAsString());
      }
    }
    return client;
  }

  public static void postMultipart(TestContext context, String path, Map<String, String> fields)
      throws Exception {
    startServerIfNeeded(context);
    record(context, context.getHttpClient().postMultipart(path, fields));
  }
}
