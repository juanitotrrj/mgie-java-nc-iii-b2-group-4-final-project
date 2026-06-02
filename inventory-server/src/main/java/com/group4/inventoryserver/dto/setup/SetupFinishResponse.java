package com.group4.inventoryserver.dto.setup;

public class SetupFinishResponse {

  private String state;
  private String message;
  private String initializedAt;

  public SetupFinishResponse() {}

  public SetupFinishResponse(String state, String message, String initializedAt) {
    this.state = state;
    this.message = message;
    this.initializedAt = initializedAt;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getInitializedAt() {
    return initializedAt;
  }

  public void setInitializedAt(String initializedAt) {
    this.initializedAt = initializedAt;
  }
}
