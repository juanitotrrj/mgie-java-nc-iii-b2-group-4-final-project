package com.group4.inventoryserver.repository;

import com.group4.inventoryserver.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class SessionRepository {

  public void create(
      long userId, String tokenHash, String ip, String userAgent, Timestamp expiresAt) {
    String sql =
        "INSERT INTO user_sessions (user_id, token_hash, ip_address, user_agent, expires_at) "
            + "VALUES (?, ?, ?, ?, ?)";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, userId);
      ps.setString(2, tokenHash);
      ps.setString(3, ip);
      ps.setString(4, truncate(userAgent, 255));
      ps.setTimestamp(5, expiresAt);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to create session", e);
    }
  }

  public Map<String, Object> findActiveByTokenHash(String tokenHash) {
    String sql =
        "SELECT session_id, user_id, status, expires_at, last_activity_at "
            + "FROM user_sessions "
            + "WHERE token_hash = ? AND status = 'Active' AND expires_at > NOW()";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, tokenHash);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          Map<String, Object> session = new HashMap<>();
          session.put("sessionId", rs.getLong("session_id"));
          session.put("userId", rs.getLong("user_id"));
          session.put("status", rs.getString("status"));
          session.put("expiresAt", rs.getTimestamp("expires_at"));
          session.put("lastActivityAt", rs.getTimestamp("last_activity_at"));
          return session;
        }
      }
      return null;
    } catch (SQLException e) {
      throw new RuntimeException("Failed to find active session", e);
    }
  }

  public void invalidate(long sessionId, String status) {
    String sql = "UPDATE user_sessions SET status = ?, invalidated_at = NOW() WHERE session_id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, status);
      ps.setLong(2, sessionId);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to invalidate session", e);
    }
  }

  public void invalidateAllForUser(long userId) {
    String sql =
        "UPDATE user_sessions SET status = 'Revoked', invalidated_at = NOW() "
            + "WHERE user_id = ? AND status = 'Active'";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, userId);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to invalidate user sessions", e);
    }
  }

  public void updateLastActivity(long sessionId) {
    String sql = "UPDATE user_sessions SET last_activity_at = NOW() WHERE session_id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, sessionId);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to update session activity", e);
    }
  }

  private String truncate(String value, int maxLength) {
    if (value == null) return null;
    return value.length() <= maxLength ? value : value.substring(0, maxLength);
  }
}
