package com.group4.inventoryserver.dto;

import java.util.List;

public class PaginatedResponse<T> {

  private final boolean success;
  private final String message;
  private final List<T> data;
  private final PaginationMeta meta;

  public PaginatedResponse(List<T> data, PaginationMeta meta) {
    this.success = true;
    this.message = "Records retrieved successfully.";
    this.data = data;
    this.meta = meta;
  }

  public PaginatedResponse(String message, List<T> data, PaginationMeta meta) {
    this.success = true;
    this.message = message;
    this.data = data;
    this.meta = meta;
  }

  public boolean isSuccess() {
    return success;
  }

  public String getMessage() {
    return message;
  }

  public List<T> getData() {
    return data;
  }

  public PaginationMeta getMeta() {
    return meta;
  }
}
