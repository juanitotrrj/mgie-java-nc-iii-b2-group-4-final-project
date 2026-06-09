package com.group4.inventoryserver.repository;

import com.group4.inventoryserver.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class SetupSessionRepository {

  public void create(String tokenHash, Timestamp expiresAt) {
    String sql = "INSERT INTO setup_sessions (token_hash, expires_at) VALUES (?, ?)";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, tokenHash);
      ps.setTimestamp(2, expiresAt);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to create setup session", e);
    }
  }

  public boolean isValid(String tokenHash) {
    String sql =
        "SELECT setup_session_id FROM setup_sessions "
            + "WHERE token_hash = ? AND status = 'Active' AND expires_at > CURRENT_TIMESTAMP";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, tokenHash);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to validate setup session", e);
    }
  }

  public void touchActivity(String tokenHash) {
    String sql =
        "UPDATE setup_sessions SET last_activity_at = CURRENT_TIMESTAMP WHERE token_hash = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, tokenHash);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to update session activity", e);
    }
  }

  public void finish(String tokenHash) {
    String sql = "UPDATE setup_sessions SET status = 'Finished' WHERE token_hash = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, tokenHash);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to finish setup session", e);
    }
  }

  public void expireAll() {
    String sql = "UPDATE setup_sessions SET status = 'Expired' WHERE status = 'Active'";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to expire sessions", e);
    }
  }
}
