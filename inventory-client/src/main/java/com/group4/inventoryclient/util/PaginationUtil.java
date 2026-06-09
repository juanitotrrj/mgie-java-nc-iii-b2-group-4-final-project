package com.group4.inventoryclient.util;

import com.google.gson.JsonObject;

public final class PaginationUtil {

  private PaginationUtil() {}

  public static int totalPages(JsonObject meta) {
    if (meta == null) {
      return 1;
    }
    if (meta.has("totalPages")) {
      return meta.get("totalPages").getAsInt();
    }
    if (meta.has("lastPage")) {
      return meta.get("lastPage").getAsInt();
    }
    return 1;
  }
}
