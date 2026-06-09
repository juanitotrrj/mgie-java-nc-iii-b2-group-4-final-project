package com.group4.inventoryserver.dto.auth;

import java.util.List;

public class UserProfile {

  private final long userId;
  private final String fullName;
  private final String username;
  private final String email;
  private final String role;
  private final String status;
  private final String lastLogin;
  private final List<String> permissions;

  public UserProfile(
      long userId,
      String fullName,
      String username,
      String email,
      String role,
      String status,
      String lastLogin,
      List<String> permissions) {
    this.userId = userId;
    this.fullName = fullName;
    this.username = username;
    this.email = email;
    this.role = role;
    this.status = status;
    this.lastLogin = lastLogin;
    this.permissions = permissions;
  }

  public long getUserId() {
    return userId;
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

  public List<String> getPermissions() {
    return permissions;
  }
}
