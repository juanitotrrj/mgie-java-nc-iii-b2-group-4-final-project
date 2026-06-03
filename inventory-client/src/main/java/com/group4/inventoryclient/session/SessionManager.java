package com.group4.inventoryclient.session;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.HashSet;
import java.util.Set;

public class SessionManager {

  private static SessionManager instance;

  private String token;
  private String role;
  private String username;
  private String fullName;
  private String email;
  private long userId;
  private int expiresInSeconds;
  private long loginTimestamp;
  private final Set<String> permissions = new HashSet<>();

  private SessionManager() {}

  public static synchronized SessionManager getInstance() {
    if (instance == null) instance = new SessionManager();
    return instance;
  }

  public void login(JsonObject loginData) {
    this.token = loginData.get("token").getAsString();
    this.expiresInSeconds =
        loginData.has("expiresInSeconds") ? loginData.get("expiresInSeconds").getAsInt() : 1800;
    this.loginTimestamp = System.currentTimeMillis();

    if (loginData.has("user")) {
      JsonObject user = loginData.getAsJsonObject("user");
      this.userId = user.has("userId") ? user.get("userId").getAsLong() : 0;
      this.username = user.has("username") ? user.get("username").getAsString() : "";
      this.fullName = user.has("fullName") ? user.get("fullName").getAsString() : "";
      this.email = user.has("email") ? user.get("email").getAsString() : "";
      this.role = user.has("role") ? user.get("role").getAsString() : "";
      permissions.clear();
      if (user.has("permissions")) {
        JsonArray perms = user.getAsJsonArray("permissions");
        for (int i = 0; i < perms.size(); i++) {
          permissions.add(perms.get(i).getAsString());
        }
      }
    }
  }

  public void logout() {
    token = null;
    role = null;
    username = null;
    fullName = null;
    email = null;
    userId = 0;
    permissions.clear();
  }

  public boolean isLoggedIn() {
    return token != null && !token.isEmpty();
  }

  public boolean isExpired() {
    if (token == null) return true;
    long elapsed = (System.currentTimeMillis() - loginTimestamp) / 1000;
    return elapsed >= expiresInSeconds;
  }

  public boolean hasPermission(String perm) {
    return permissions.contains(perm);
  }

  public String getToken() {
    return token;
  }

  public String getRole() {
    return role;
  }

  public String getUsername() {
    return username;
  }

  public String getFullName() {
    return fullName;
  }

  public long getUserId() {
    return userId;
  }

  public Set<String> getPermissions() {
    return permissions;
  }
}
