package com.group4.inventoryserver.repository;

import com.group4.inventoryserver.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class DatabaseBackupRepository {

  public long insert(String filename, String path, long sizeBytes, String status, Long createdBy) {
    String sql =
        "INSERT INTO database_backups (backup_filename, backup_path, file_size_bytes, "
            + "status, created_by) VALUES (?, ?, ?, ?, ?)";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps =
            conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, filename);
      ps.setString(2, path);
      ps.setLong(3, sizeBytes);
      ps.setString(4, status);
      if (createdBy != null) {
        ps.setLong(5, createdBy);
      } else {
        ps.setNull(5, Types.BIGINT);
      }
      ps.executeUpdate();
      try (ResultSet rs = ps.getGeneratedKeys()) {
        if (rs.next()) return rs.getLong(1);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to insert database backup record", e);
    }
    return 0;
  }

  public String findPathByFilename(String filename) {
    String sql =
        "SELECT backup_path FROM database_backups "
            + "WHERE backup_filename = ? AND status = 'Created' ORDER BY created_at DESC LIMIT 1";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, filename);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return rs.getString("backup_path");
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to find backup by filename", e);
    }
    return null;
  }

  public void markRestored(String filename, Long restoredBy) {
    String sql =
        "UPDATE database_backups SET status = 'Restored', restored_by = ?, "
            + "restored_at = NOW() WHERE backup_filename = ? AND status = 'Created'";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      if (restoredBy != null) {
        ps.setLong(1, restoredBy);
      } else {
        ps.setNull(1, Types.BIGINT);
      }
      ps.setString(2, filename);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to mark backup as restored", e);
    }
  }
}
