package com.group4.inventoryserver.testing;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class E2eHttpClient {

  private final String baseUrl;
  private String bearerToken;
  private String setupToken;

  public E2eHttpClient(String baseUrl) {
    this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
  }

  public void setBearerToken(String token) {
    this.bearerToken = token;
  }

  public void setSetupToken(String token) {
    this.setupToken = token;
  }

  public HttpResult get(String path) throws IOException {
    return request("GET", path, null, null);
  }

  public HttpResult post(String path, String jsonBody) throws IOException {
    return request("POST", path, jsonBody, "application/json");
  }

  public HttpResult put(String path, String jsonBody) throws IOException {
    return request("PUT", path, jsonBody, "application/json");
  }

  public HttpResult delete(String path) throws IOException {
    return request("DELETE", path, null, null);
  }

  public HttpResult postMultipart(String path, Map<String, String> fields) throws IOException {
    String boundary = "----E2eBoundary" + System.currentTimeMillis();
    StringBuilder body = new StringBuilder();
    for (Map.Entry<String, String> entry : fields.entrySet()) {
      body.append("--").append(boundary).append("\r\n");
      body.append("Content-Disposition: form-data; name=\"")
          .append(entry.getKey())
          .append("\"\r\n\r\n");
      body.append(entry.getValue()).append("\r\n");
    }
    body.append("--").append(boundary).append("--\r\n");
    return request("POST", path, body.toString(), "multipart/form-data; boundary=" + boundary);
  }

  public HttpResult login(String username, String password) throws IOException {
    return login(username, "Administrator", password);
  }

  public HttpResult login(String username, String role, String password) throws IOException {
    JsonObject body = new JsonObject();
    body.addProperty("username", username);
    body.addProperty("password", password);
    body.addProperty("role", role);
    HttpResult result = post("/auth/login", body.toString());
    if (result.status >= 200 && result.status < 300) {
      JsonObject root = JsonParser.parseString(result.body).getAsJsonObject();
      if (root.has("data") && root.get("data").isJsonObject()) {
        JsonObject data = root.getAsJsonObject("data");
        if (data.has("token")) {
          bearerToken = data.get("token").getAsString();
        }
      }
    }
    return result;
  }

  private HttpResult request(String method, String path, String body, String contentType)
      throws IOException {
    URL url = new URL(baseUrl + path);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod(method);
    conn.setConnectTimeout(10000);
    conn.setReadTimeout(30000);
    conn.setRequestProperty("Accept", "application/json");
    if (contentType != null) {
      conn.setRequestProperty("Content-Type", contentType);
    }
    if (bearerToken != null && !bearerToken.isEmpty()) {
      conn.setRequestProperty("Authorization", "Bearer " + bearerToken);
    }
    if (setupToken != null && !setupToken.isEmpty()) {
      conn.setRequestProperty("X-Setup-Token", setupToken);
    }
    if (body != null) {
      conn.setDoOutput(true);
      try (OutputStream os = conn.getOutputStream()) {
        os.write(body.getBytes(StandardCharsets.UTF_8));
      }
    }
    int status = conn.getResponseCode();
    String responseBody = readBody(conn, status);
    Map<String, String> headers = new LinkedHashMap<>();
    conn.getHeaderFields()
        .forEach(
            (k, v) -> {
              if (k != null && v != null && !v.isEmpty()) headers.put(k, v.get(0));
            });
    conn.disconnect();
    return new HttpResult(status, responseBody, headers);
  }

  private static String readBody(HttpURLConnection conn, int status) throws IOException {
    BufferedReader reader;
    if (status >= 200 && status < 400 && conn.getInputStream() != null) {
      reader =
          new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
    } else if (conn.getErrorStream() != null) {
      reader =
          new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
    } else {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
      sb.append(line);
    }
    reader.close();
    return sb.toString();
  }

  public static final class HttpResult {
    public final int status;
    public final String body;
    public final Map<String, String> headers;

    public HttpResult(int status, String body, Map<String, String> headers) {
      this.status = status;
      this.body = body;
      this.headers = headers;
    }

    public JsonObject jsonRoot() {
      if (body == null || body.isEmpty()) return new JsonObject();
      return JsonParser.parseString(body).getAsJsonObject();
    }
  }
}
