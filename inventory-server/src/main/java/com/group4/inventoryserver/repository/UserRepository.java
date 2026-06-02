package com.group4.inventoryserver.repository;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.dto.auth.UserProfile;
import com.group4.inventoryserver.dto.user.UserData;
import com.group4.inventoryserver.util.DateUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
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

  // ─── User Management CRUD ──────────────────────────────────────────────────

  private static final Map<String, String> SORT_COLUMN_MAP = new HashMap<>();

  static {
    SORT_COLUMN_MAP.put("userId", "u.user_id");
    SORT_COLUMN_MAP.put("userCode", "u.user_code");
    SORT_COLUMN_MAP.put("fullName", "u.full_name");
    SORT_COLUMN_MAP.put("username", "u.username");
    SORT_COLUMN_MAP.put("email", "u.email");
    SORT_COLUMN_MAP.put("role", "r.role_name");
    SORT_COLUMN_MAP.put("status", "u.status");
    SORT_COLUMN_MAP.put("createdAt", "u.created_at");
  }

  private static final String USER_LIST_SELECT =
      "SELECT u.user_id, u.user_code, u.full_name, u.username, u.email, "
          + "r.role_name, u.status, u.last_login_at, u.created_at, u.updated_at "
          + "FROM users u JOIN roles r ON u.role_id = r.role_id";

  public List<UserData> findAll(
      int offset,
      int limit,
      String sortBy,
      String sortDir,
      String search,
      String role,
      String status) {
    StringBuilder sql = new StringBuilder(USER_LIST_SELECT);
    List<Object> params = new ArrayList<>();
    appendUserWhereClause(sql, params, search, role, status);

    String column = SORT_COLUMN_MAP.getOrDefault(sortBy, "u.created_at");
    String direction = "desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
    sql.append(" ORDER BY ").append(column).append(' ').append(direction);
    sql.append(" LIMIT ? OFFSET ?");
    params.add(limit);
    params.add(offset);

    List<UserData> results = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql.toString())) {
      setParams(ps, params);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          results.add(mapUserData(rs));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to list users", e);
    }
    return results;
  }

  public long count(String search, String role, String status) {
    StringBuilder sql =
        new StringBuilder("SELECT COUNT(*) FROM users u JOIN roles r ON u.role_id = r.role_id");
    List<Object> params = new ArrayList<>();
    appendUserWhereClause(sql, params, search, role, status);

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql.toString())) {
      setParams(ps, params);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return rs.getLong(1);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to count users", e);
    }
    return 0;
  }

  public UserData findDetailById(long userId) {
    String sql = USER_LIST_SELECT + " WHERE u.user_id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, userId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return mapUserData(rs);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to find user detail", e);
    }
    return null;
  }

  public long insert(
      String userCode,
      String fullName,
      String username,
      String email,
      String passwordHash,
      long roleId,
      String status,
      Long createdBy) {
    String sql =
        "INSERT INTO users (user_code, full_name, username, email, password_hash, role_id, "
            + "status, created_by, updated_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, userCode);
      ps.setString(2, fullName);
      ps.setString(3, username);
      ps.setString(4, email);
      ps.setString(5, passwordHash);
      ps.setLong(6, roleId);
      ps.setString(7, status);
      setNullableLong(ps, 8, createdBy);
      setNullableLong(ps, 9, createdBy);
      ps.executeUpdate();
      try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next()) return keys.getLong(1);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to insert user", e);
    }
    return -1;
  }

  public void update(
      long userId, String fullName, String email, long roleId, String status, Long updatedBy) {
    String sql =
        "UPDATE users SET full_name = ?, email = ?, role_id = ?, status = ?, updated_by = ? "
            + "WHERE user_id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, fullName);
      ps.setString(2, email);
      ps.setLong(3, roleId);
      ps.setString(4, status);
      setNullableLong(ps, 5, updatedBy);
      ps.setLong(6, userId);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to update user", e);
    }
  }

  public void deactivate(long userId, Long updatedBy) {
    String sql = "UPDATE users SET status = 'Inactive', updated_by = ? WHERE user_id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      setNullableLong(ps, 1, updatedBy);
      ps.setLong(2, userId);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to deactivate user", e);
    }
  }

  public void resetPassword(long userId, String passwordHash, boolean mustChangePassword) {
    String sql =
        "UPDATE users SET password_hash = ?, must_change_password = ?, "
            + "password_changed_at = NOW() WHERE user_id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, passwordHash);
      ps.setInt(2, mustChangePassword ? 1 : 0);
      ps.setLong(3, userId);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to reset password", e);
    }
  }

  public String generateNextUserCode() {
    String sql = "SELECT user_code FROM users ORDER BY user_id DESC LIMIT 1";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          String last = rs.getString("user_code");
          String numPart = last.substring(1);
          int num = Integer.parseInt(numPart);
          return "U" + String.format("%03d", num + 1);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to generate user code", e);
    }
    return "U001";
  }

  public boolean existsByUsername(String username) {
    return existsBy("SELECT 1 FROM users WHERE username = ?", username);
  }

  public boolean existsByEmail(String email) {
    return existsBy("SELECT 1 FROM users WHERE email = ?", email);
  }

  public boolean existsByEmailExcluding(String email, long userId) {
    String sql = "SELECT 1 FROM users WHERE email = ? AND user_id != ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, email);
      ps.setLong(2, userId);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to check email uniqueness", e);
    }
  }

  public Long getRoleIdByName(String roleName) {
    String sql = "SELECT role_id FROM roles WHERE role_name = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, roleName);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return rs.getLong("role_id");
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to get role by name", e);
    }
    return null;
  }

  public void invalidateSessions(long userId) {
    String sql =
        "UPDATE user_sessions SET status = 'Revoked' WHERE user_id = ? AND status = 'Active'";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, userId);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to invalidate user sessions", e);
    }
  }

  // ─── Private helpers ──────────────────────────────────────────────────────────

  private UserData mapUserData(ResultSet rs) throws SQLException {
    Timestamp lastLogin = rs.getTimestamp("last_login_at");
    Timestamp createdAt = rs.getTimestamp("created_at");
    Timestamp updatedAt = rs.getTimestamp("updated_at");
    return new UserData(
        rs.getLong("user_id"),
        rs.getString("user_code"),
        rs.getString("full_name"),
        rs.getString("username"),
        rs.getString("email"),
        rs.getString("role_name"),
        rs.getString("status"),
        lastLogin != null ? DateUtil.formatIso(lastLogin) : null,
        createdAt != null ? DateUtil.formatIso(createdAt) : null,
        updatedAt != null ? DateUtil.formatIso(updatedAt) : null);
  }

  private void appendUserWhereClause(
      StringBuilder sql, List<Object> params, String search, String role, String status) {
    List<String> conditions = new ArrayList<>();
    if (search != null && !search.trim().isEmpty()) {
      conditions.add(
          "(u.full_name LIKE ? OR u.username LIKE ? OR u.email LIKE ? OR u.user_code LIKE ?)");
      String pattern = "%" + search.trim() + "%";
      params.add(pattern);
      params.add(pattern);
      params.add(pattern);
      params.add(pattern);
    }
    if (role != null && !role.trim().isEmpty()) {
      conditions.add("r.role_name = ?");
      params.add(role);
    }
    if (status != null && !status.trim().isEmpty()) {
      conditions.add("u.status = ?");
      params.add(status);
    }
    if (!conditions.isEmpty()) {
      sql.append(" WHERE ");
      for (int i = 0; i < conditions.size(); i++) {
        if (i > 0) sql.append(" AND ");
        sql.append(conditions.get(i));
      }
    }
  }

  private boolean existsBy(String sql, String value) {
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, value);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to check existence", e);
    }
  }

  private void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
    if (value != null) {
      ps.setLong(index, value);
    } else {
      ps.setNull(index, Types.BIGINT);
    }
  }

  private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
    for (int i = 0; i < params.size(); i++) {
      Object val = params.get(i);
      if (val instanceof String) {
        ps.setString(i + 1, (String) val);
      } else if (val instanceof Long) {
        ps.setLong(i + 1, (Long) val);
      } else if (val instanceof Integer) {
        ps.setInt(i + 1, (Integer) val);
      } else {
        ps.setObject(i + 1, val);
      }
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
