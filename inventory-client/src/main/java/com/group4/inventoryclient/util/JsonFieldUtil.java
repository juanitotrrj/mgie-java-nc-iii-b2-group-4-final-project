package com.group4.inventoryclient.util;

import com.google.gson.JsonObject;

public final class JsonFieldUtil {

  private JsonFieldUtil() {}

  public static long getLong(JsonObject obj, String... keys) {
    for (String key : keys) {
      if (obj.has(key) && !obj.get(key).isJsonNull()) {
        return obj.get(key).getAsLong();
      }
    }
    return 0L;
  }

  public static int getInt(JsonObject obj, String... keys) {
    for (String key : keys) {
      if (obj.has(key) && !obj.get(key).isJsonNull()) {
        return obj.get(key).getAsInt();
      }
    }
    return 0;
  }

  public static String getString(JsonObject obj, String defaultValue, String... keys) {
    for (String key : keys) {
      if (obj.has(key) && !obj.get(key).isJsonNull()) {
        return obj.get(key).getAsString();
      }
    }
    return defaultValue;
  }

  public static double getDouble(JsonObject obj, String... keys) {
    for (String key : keys) {
      if (obj.has(key) && !obj.get(key).isJsonNull()) {
        return obj.get(key).getAsDouble();
      }
    }
    return 0.0;
  }
}
