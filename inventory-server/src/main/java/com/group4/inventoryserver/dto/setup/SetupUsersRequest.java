package com.group4.inventoryserver.dto.setup;

import java.util.List;

public class SetupUsersRequest {

  private List<SetupUserEntry> users;

  public SetupUsersRequest() {}

  public List<SetupUserEntry> getUsers() {
    return users;
  }

  public void setUsers(List<SetupUserEntry> users) {
    this.users = users;
  }

  public static class SetupUserEntry {
    private String username;
    private String password;
    private String email;
    private String fullName;
    private String role;

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public String getFullName() {
      return fullName;
    }

    public void setFullName(String fullName) {
      this.fullName = fullName;
    }

    public String getRole() {
      return role;
    }

    public void setRole(String role) {
      this.role = role;
    }
  }
}
