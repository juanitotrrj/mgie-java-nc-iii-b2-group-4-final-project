package com.group4.inventoryclient.api;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiClient {

  private static final Gson GSON = new Gson();
  private final String baseUrl;
  private String setupToken;
  private String bearerToken;

  public ApiClient(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setSetupToken(String token) {
    this.setupToken = token;
  }

  public String getSetupToken() {
    return setupToken;
  }

  public void setBearerToken(String token) {
    this.bearerToken = token;
  }

  public String getBearerToken() {
    return bearerToken;
  }

  public ApiResponse delete(String path) throws IOException {
    return executeRequest("DELETE", path, null);
  }

  public ApiResponse get(String path) throws IOException {
    return executeRequest("GET", path, null);
  }

  public ApiResponse post(String path, Object body) throws IOException {
    return executeRequest("POST", path, body);
  }

  public ApiResponse put(String path, Object body) throws IOException {
    return executeRequest("PUT", path, body);
  }

  protected ApiResponse executeRequest(String method, String path, Object body) throws IOException {
    return request(method, path, body);
  }

  private ApiResponse request(String method, String path, Object body) throws IOException {
    URL url = new URL(baseUrl + path);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod(method);
    conn.setRequestProperty("Content-Type", "application/json");
    conn.setRequestProperty("Accept", "application/json");
    conn.setConnectTimeout(10000);
    conn.setReadTimeout(30000);

    if (bearerToken != null && !bearerToken.isEmpty()) {
      conn.setRequestProperty("Authorization", "Bearer " + bearerToken);
    }
    if (setupToken != null && !setupToken.isEmpty()) {
      conn.setRequestProperty("X-Setup-Token", setupToken);
    }

    if (body != null) {
      conn.setDoOutput(true);
      String json = GSON.toJson(body);
      try (OutputStream os = conn.getOutputStream()) {
        os.write(json.getBytes("UTF-8"));
      }
    }

    int status = conn.getResponseCode();
    String responseBody = readResponse(conn, status);
    conn.disconnect();
    return new ApiResponse(status, responseBody);
  }

  private String readResponse(HttpURLConnection conn, int status) throws IOException {
    BufferedReader reader;
    if (status >= 200 && status < 400) {
      reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
    } else {
      if (conn.getErrorStream() != null) {
        reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
      } else {
        return "";
      }
    }
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
      sb.append(line);
    }
    reader.close();
    return sb.toString();
  }

  public static class ApiResponse {
    private final int statusCode;
    private final String body;

    public ApiResponse(int statusCode, String body) {
      this.statusCode = statusCode;
      this.body = body;
    }

    public int getStatusCode() {
      return statusCode;
    }

    public String getBody() {
      return body;
    }

    public boolean isSuccess() {
      return statusCode >= 200 && statusCode < 300;
    }

    public JsonObject getDataAsObject() {
      JsonObject root = JsonParser.parseString(body).getAsJsonObject();
      JsonElement data = root.get("data");
      if (data != null && data.isJsonObject()) return data.getAsJsonObject();
      return root;
    }

    public com.google.gson.JsonArray getDataAsArray() {
      JsonObject root = JsonParser.parseString(body).getAsJsonObject();
      JsonElement data = root.get("data");
      if (data != null && data.isJsonArray()) return data.getAsJsonArray();
      return new com.google.gson.JsonArray();
    }

    public JsonObject getMeta() {
      JsonObject root = JsonParser.parseString(body).getAsJsonObject();
      JsonElement meta = root.get("meta");
      if (meta != null && meta.isJsonObject()) return meta.getAsJsonObject();
      return null;
    }

    public JsonObject getRawRoot() {
      return JsonParser.parseString(body).getAsJsonObject();
    }

    public String getErrorMessage() {
      try {
        JsonObject root = JsonParser.parseString(body).getAsJsonObject();
        if (root.has("message")) return root.get("message").getAsString();
        if (root.has("error")) return root.get("error").getAsString();
      } catch (Exception e) {
        // ignore parse errors
      }
      return "Request failed with status " + statusCode;
    }
  }
}
