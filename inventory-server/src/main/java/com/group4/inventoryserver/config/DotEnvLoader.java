package com.group4.inventoryserver.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DotEnvLoader {

  private static final Logger log = LoggerFactory.getLogger(DotEnvLoader.class);
  private static final Map<String, String> ENV_MAP = new LinkedHashMap<>();
  private static boolean loaded = false;

  private DotEnvLoader() {}

  public static synchronized void load() {
    if (loaded) return;
    String path = System.getProperty("g4ims.env.file", ".env");
    File envFile = new File(path);
    if (!envFile.exists()) {
      log.debug(".env file not found at {}, relying on system environment variables", path);
      loaded = true;
      return;
    }
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(new FileInputStream(envFile), "UTF-8"))) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("#")) continue;
        int eq = line.indexOf('=');
        if (eq <= 0) continue;
        String key = line.substring(0, eq).trim();
        String value = line.substring(eq + 1).trim();
        value = stripQuotes(value);
        value = stripInlineComment(value);
        ENV_MAP.put(key, value);
      }
      log.info(
          "Loaded {} configuration entries from {}", ENV_MAP.size(), envFile.getAbsolutePath());
    } catch (IOException e) {
      log.warn("Failed to read .env file at {}: {}", path, e.getMessage());
    }
    loaded = true;
  }

  public static String resolve(String key, String defaultValue) {
    if (!loaded) load();
    String value = ENV_MAP.get(key);
    if (value != null && !value.isEmpty()) return value;
    value = System.getenv(key);
    if (value != null && !value.isEmpty()) return value;
    return defaultValue;
  }

  public static Map<String, String> getAll() {
    if (!loaded) load();
    return new LinkedHashMap<>(ENV_MAP);
  }

  public static void reset() {
    ENV_MAP.clear();
    loaded = false;
  }

  private static String stripQuotes(String value) {
    if (value.length() >= 2) {
      char first = value.charAt(0);
      char last = value.charAt(value.length() - 1);
      if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
        return value.substring(1, value.length() - 1);
      }
    }
    return value;
  }

  private static String stripInlineComment(String value) {
    if (value.startsWith("\"") || value.startsWith("'")) return value;
    int hash = value.indexOf(" #");
    if (hash > 0) return value.substring(0, hash).trim();
    return value;
  }
}
