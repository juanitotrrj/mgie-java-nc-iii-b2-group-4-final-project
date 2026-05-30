package com.group4.inventoryserver.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Type;

public final class JsonUtil {

  private static final Gson GSON =
      new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").disableHtmlEscaping().create();

  private static final Gson PRETTY =
      new GsonBuilder()
          .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
          .disableHtmlEscaping()
          .setPrettyPrinting()
          .create();

  private JsonUtil() {}

  public static String toJson(Object obj) {
    return GSON.toJson(obj);
  }

  public static String toPrettyJson(Object obj) {
    return PRETTY.toJson(obj);
  }

  public static <T> T fromJson(String json, Class<T> clazz) {
    try {
      return GSON.fromJson(json, clazz);
    } catch (JsonSyntaxException e) {
      throw new IllegalArgumentException("Invalid JSON: " + e.getMessage(), e);
    }
  }

  public static <T> T fromJson(String json, Type type) {
    try {
      return GSON.fromJson(json, type);
    } catch (JsonSyntaxException e) {
      throw new IllegalArgumentException("Invalid JSON: " + e.getMessage(), e);
    }
  }
}
