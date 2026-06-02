package com.group4.inventoryserver.dto.user;

public class UserData {

  private final long userId;
  private final String userCode;
  private final String fullName;
  private final String username;
  private final String email;
  private final String role;
  private final String status;
  private final String lastLogin;
  private final String createdAt;
  private final String updatedAt;

  public UserData(
      long userId,
      String userCode,
      String fullName,
      String username,
      String email,
      String role,
      String status,
      String lastLogin,
      String createdAt,
      String updatedAt) {
    this.userId = userId;
    this.userCode = userCode;
    this.fullName = fullName;
    this.username = username;
    this.email = email;
    this.role = role;
    this.status = status;
    this.lastLogin = lastLogin;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public long getUserId() {
    return userId;
  }

  public String getUserCode() {
    return userCode;
  }

  public String getFullName() {
    return fullName;
  }

  public String getUsername() {
    return username;
  }

  public String getEmail() {
    return email;
  }

  public String getRole() {
    return role;
  }

  public String getStatus() {
    return status;
  }

  public String getLastLogin() {
    return lastLogin;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public String getUpdatedAt() {
    return updatedAt;
  }
}
