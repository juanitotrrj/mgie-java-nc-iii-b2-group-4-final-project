package com.group4.inventoryserver.handler;

import com.group4.inventoryserver.config.EnvConfig;
import com.group4.inventoryserver.exception.ApiException;
import com.group4.inventoryserver.server.RequestContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SwaggerHandler extends BaseHandler {

  private static final String BASE_PATH = EnvConfig.appContextPath() + "/docs";
  private static final String WEBJAR_PREFIX = "META-INF/resources/webjars/swagger-ui/";
  private static final String WEBJAR_VERSION = "5.17.14";

  private static final Map<String, String> MIME_TYPES = new HashMap<>();
  private static final Pattern SERVERS_BLOCK = Pattern.compile("(?s)servers:\\n.*?(?=\\ntags:)");

  static {
    MIME_TYPES.put("html", "text/html; charset=UTF-8");
    MIME_TYPES.put("css", "text/css; charset=UTF-8");
    MIME_TYPES.put("js", "application/javascript; charset=UTF-8");
    MIME_TYPES.put("json", "application/json; charset=UTF-8");
    MIME_TYPES.put("yaml", "application/x-yaml; charset=UTF-8");
    MIME_TYPES.put("yml", "application/x-yaml; charset=UTF-8");
    MIME_TYPES.put("png", "image/png");
    MIME_TYPES.put("svg", "image/svg+xml");
    MIME_TYPES.put("map", "application/json");
  }

  @Override
  public void handle(RequestContext ctx) throws IOException {
    if (!"GET".equals(ctx.getMethod())) {
      throw new ApiException(405, "Method Not Allowed: " + ctx.getMethod());
    }

    String pathParam = ctx.getPathParam(BASE_PATH);
    if (pathParam == null || pathParam.isEmpty()) {
      serveIndex(ctx);
    } else if ("openapi.yaml".equals(pathParam)) {
      serveOpenApi(ctx);
    } else if (pathParam.startsWith("assets/")) {
      String filename = pathParam.substring("assets/".length());
      serveSwaggerAsset(ctx, filename);
    } else {
      throw new ApiException(404, "Not found.");
    }
  }

  private void serveIndex(RequestContext ctx) throws IOException {
    byte[] template = readClasspathResource("/templates/swagger-index.html");
    if (template == null) {
      throw new ApiException(500, "Swagger UI template not found.");
    }
    String html = new String(template, "UTF-8");
    html = html.replace("{{BASE_PATH}}", EnvConfig.appContextPath());
    byte[] content = html.getBytes("UTF-8");
    sendBytes(ctx, content, "text/html; charset=UTF-8");
  }

  private void serveSwaggerAsset(RequestContext ctx, String filename) throws IOException {
    if (filename.contains("..") || filename.contains("/")) {
      throw new ApiException(400, "Invalid asset path.");
    }
    String resourcePath = WEBJAR_PREFIX + WEBJAR_VERSION + "/" + filename;
    byte[] content = readClasspathResource(resourcePath);
    if (content == null) {
      throw new ApiException(404, "Asset not found: " + filename);
    }
    String ext = getExtension(filename);
    String contentType = MIME_TYPES.getOrDefault(ext, "application/octet-stream");
    sendBytes(ctx, content, contentType);
  }

  private void serveOpenApi(RequestContext ctx) throws IOException {
    byte[] template = readClasspathResource("/docs/openapi.yaml");
    if (template == null) {
      throw new ApiException(404, "OpenAPI specification not found.");
    }
    String yaml = patchServersBlock(new String(template, StandardCharsets.UTF_8), ctx);
    sendBytes(ctx, yaml.getBytes(StandardCharsets.UTF_8), MIME_TYPES.get("yaml"));
  }

  static String patchServersBlock(String yaml, RequestContext ctx) {
    String origin = resolveServerOrigin(ctx);
    String apiBase = resolveApiBaseUrl(origin);
    String serversBlock =
        "servers:\n"
            + "- url: "
            + origin
            + "\n"
            + "  description: Current backend server (from request Host or APP_BASE_URL)\n"
            + "- url: "
            + apiBase
            + "\n"
            + "  description: Current API base path\n";
    return SERVERS_BLOCK.matcher(yaml).replaceFirst(serversBlock);
  }

  static String resolveServerOrigin(RequestContext ctx) {
    String host = ctx.getHeader("Host");
    if (host != null && !host.trim().isEmpty()) {
      String scheme = ctx.getHeader("X-Forwarded-Proto");
      if (scheme == null || scheme.trim().isEmpty()) {
        scheme = "http";
      }
      return scheme.trim().toLowerCase() + "://" + host.trim();
    }
    return stripTrailingSlash(EnvConfig.appBaseUrl());
  }

  static String resolveApiBaseUrl(String origin) {
    String contextPath = EnvConfig.appContextPath();
    if (contextPath == null || contextPath.isEmpty() || "/".equals(contextPath)) {
      return origin;
    }
    String normalizedContext = contextPath.startsWith("/") ? contextPath : "/" + contextPath;
    if (origin.endsWith(normalizedContext)) {
      return origin;
    }
    return origin + normalizedContext;
  }

  private static String stripTrailingSlash(String url) {
    if (url == null || url.isEmpty()) {
      return url;
    }
    return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
  }

  private void sendBytes(RequestContext ctx, byte[] content, String contentType)
      throws IOException {
    ctx.getExchange().getResponseHeaders().set("Content-Type", contentType);
    ctx.getExchange().sendResponseHeaders(200, content.length);
    try (OutputStream os = ctx.getExchange().getResponseBody()) {
      os.write(content);
    }
  }

  private byte[] readClasspathResource(String path) {
    try (InputStream is = getClass().getResourceAsStream(path)) {
      if (is == null) {
        InputStream alt =
            getClass()
                .getClassLoader()
                .getResourceAsStream(path.startsWith("/") ? path.substring(1) : path);
        if (alt == null) return null;
        return toBytes(alt);
      }
      return toBytes(is);
    } catch (IOException e) {
      return null;
    }
  }

  private byte[] toBytes(InputStream is) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buf = new byte[8192];
    int n;
    while ((n = is.read(buf)) != -1) {
      baos.write(buf, 0, n);
    }
    return baos.toByteArray();
  }

  private String getExtension(String filename) {
    int dot = filename.lastIndexOf('.');
    if (dot < 0 || dot == filename.length() - 1) return "";
    return filename.substring(dot + 1).toLowerCase();
  }
}
