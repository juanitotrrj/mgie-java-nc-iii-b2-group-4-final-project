package com.group4.inventoryserver.repository;

import com.group4.inventoryserver.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SystemRootCredentialRepository {

  public String getPasswordHash(String username) {
    String sql = "SELECT password_hash FROM system_root_credentials WHERE username = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, username);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return rs.getString("password_hash");
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to query root credentials", e);
    }
    return null;
  }

  public boolean exists(String username) {
    return getPasswordHash(username) != null;
  }
}
