package com.group4.inventoryserver.repository;

import com.group4.inventoryserver.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class SystemInstallationRepository {

  public String getSetupState() {
    String sql = "SELECT setup_state FROM system_installation WHERE installation_id = 1";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
      if (rs.next()) return rs.getString("setup_state");
    } catch (SQLException e) {
      throw new RuntimeException("Failed to query setup state", e);
    }
    return null;
  }

  public String getServerVersion() {
    String sql = "SELECT server_version FROM system_installation WHERE installation_id = 1";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
      if (rs.next()) return rs.getString("server_version");
    } catch (SQLException e) {
      throw new RuntimeException("Failed to query server version", e);
    }
    return null;
  }

  public void updateState(String newState) {
    String sql = "UPDATE system_installation SET setup_state = ? WHERE installation_id = 1";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, newState);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to update setup state", e);
    }
  }

  public void markInitialized() {
    String sql =
        "UPDATE system_installation SET setup_state = 'INITIALIZED', "
            + "initialized_at = CURRENT_TIMESTAMP WHERE installation_id = 1";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to mark initialized", e);
    }
  }

  public Timestamp getInitializedAt() {
    String sql = "SELECT initialized_at FROM system_installation WHERE installation_id = 1";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
      if (rs.next()) return rs.getTimestamp("initialized_at");
    } catch (SQLException e) {
      throw new RuntimeException("Failed to query initialized_at", e);
    }
    return null;
  }
}
