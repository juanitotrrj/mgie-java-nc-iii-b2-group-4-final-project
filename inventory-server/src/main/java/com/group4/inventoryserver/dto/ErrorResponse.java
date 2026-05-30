package com.group4.inventoryserver.dto;

public class ErrorResponse {

  private final boolean success;
  private final int status;
  private final String message;
  private final String timestamp;

  public ErrorResponse(int status, String message) {
    this.success = false;
    this.status = status;
    this.message = message;
    this.timestamp = java.time.Instant.now().toString();
  }

  public boolean isSuccess() {
    return success;
  }

  public int getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }

  public String getTimestamp() {
    return timestamp;
  }
}
