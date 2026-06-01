package com.group4.inventoryserver.repository;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.dto.auth.UserProfile;
import com.group4.inventoryserver.util.DateUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRepository {

  private static final String FIND_BY_USERNAME =
      "SELECT u.user_id, u.user_code, u.full_name, u.username, u.email, "
          + "u.password_hash, u.role_id, u.status, u.failed_login_attempts, "
          + "u.last_login_at, r.role_name "
          + "FROM users u JOIN roles r ON u.role_id = r.role_id "
          + "WHERE u.username = ?";

  private static final String FIND_PERMISSIONS_BY_ROLE_ID =
      "SELECT p.permission_code FROM role_permissions rp "
          + "JOIN permissions p ON rp.permission_id = p.permission_id "
          + "WHERE rp.role_id = ?";

  public Map<String, Object> findByUsername(String username) {
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(FIND_BY_USERNAME)) {
      ps.setString(1, username);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          Map<String, Object> user = new HashMap<>();
          user.put("userId", rs.getLong("user_id"));
          user.put("userCode", rs.getString("user_code"));
          user.put("fullName", rs.getString("full_name"));
          user.put("username", rs.getString("username"));
          user.put("email", rs.getString("email"));
          user.put("passwordHash", rs.getString("password_hash"));
          user.put("roleId", rs.getLong("role_id"));
          user.put("status", rs.getString("status"));
          user.put("failedLoginAttempts", rs.getInt("failed_login_attempts"));
          user.put("roleName", rs.getString("role_name"));
          Timestamp lastLogin = rs.getTimestamp("last_login_at");
          user.put("lastLoginAt", lastLogin != null ? DateUtil.formatIso(lastLogin) : null);
          return user;
        }
      }
      return null;
    } catch (SQLException e) {
      throw new RuntimeException("Failed to find user by username", e);
    }
  }

  public List<String> findPermissionsByRoleId(long roleId) {
    List<String> permissions = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(FIND_PERMISSIONS_BY_ROLE_ID)) {
      ps.setLong(1, roleId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          permissions.add(rs.getString("permission_code"));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to find permissions for role", e);
    }
    return permissions;
  }

  public void updateLastLogin(long userId) {
    execute("UPDATE users SET last_login_at = NOW() WHERE user_id = ?", userId);
  }

  public void incrementFailedAttempts(long userId) {
    execute(
        "UPDATE users SET failed_login_attempts = failed_login_attempts + 1 WHERE user_id = ?",
        userId);
  }

  public void resetFailedAttempts(long userId) {
    execute("UPDATE users SET failed_login_attempts = 0 WHERE user_id = ?", userId);
  }

  public void lockUser(long userId) {
    execute("UPDATE users SET status = 'Locked' WHERE user_id = ?", userId);
  }

  public UserProfile buildProfile(Map<String, Object> user, List<String> permissions) {
    return new UserProfile(
        (Long) user.get("userId"),
        (String) user.get("fullName"),
        (String) user.get("username"),
        (String) user.get("email"),
        (String) user.get("roleName"),
        (String) user.get("status"),
        (String) user.get("lastLoginAt"),
        permissions);
  }

  public Map<String, Object> findById(long userId) {
    String sql =
        "SELECT u.user_id, u.user_code, u.full_name, u.username, u.email, "
            + "u.role_id, u.status, u.last_login_at, r.role_name "
            + "FROM users u JOIN roles r ON u.role_id = r.role_id "
            + "WHERE u.user_id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, userId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          Map<String, Object> user = new HashMap<>();
          user.put("userId", rs.getLong("user_id"));
          user.put("userCode", rs.getString("user_code"));
          user.put("fullName", rs.getString("full_name"));
          user.put("username", rs.getString("username"));
          user.put("email", rs.getString("email"));
          user.put("roleId", rs.getLong("role_id"));
          user.put("status", rs.getString("status"));
          user.put("roleName", rs.getString("role_name"));
          Timestamp lastLogin = rs.getTimestamp("last_login_at");
          user.put("lastLoginAt", lastLogin != null ? DateUtil.formatIso(lastLogin) : null);
          return user;
        }
      }
      return null;
    } catch (SQLException e) {
      throw new RuntimeException("Failed to find user by id", e);
    }
  }

  private void execute(String sql, long userId) {
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, userId);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to execute user update", e);
    }
  }
}
