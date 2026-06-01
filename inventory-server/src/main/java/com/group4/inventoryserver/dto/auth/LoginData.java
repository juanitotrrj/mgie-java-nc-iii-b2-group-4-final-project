package com.group4.inventoryserver.dto.auth;

public class LoginData {

  private final String token;
  private final String tokenType;
  private final int expiresInSeconds;
  private final UserProfile user;

  public LoginData(String token, String tokenType, int expiresInSeconds, UserProfile user) {
    this.token = token;
    this.tokenType = tokenType;
    this.expiresInSeconds = expiresInSeconds;
    this.user = user;
  }

  public String getToken() {
    return token;
  }

  public String getTokenType() {
    return tokenType;
  }

  public int getExpiresInSeconds() {
    return expiresInSeconds;
  }

  public UserProfile getUser() {
    return user;
  }
}
