package com.group4.inventoryserver.repository;

import com.group4.inventoryserver.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.Map;

public class SettingsRepository {

  public Map<String, Map<String, String>> findAll() {
    String sql =
        "SELECT setting_group, setting_key, setting_value FROM settings "
            + "ORDER BY setting_group, setting_key";
    Map<String, Map<String, String>> grouped = new LinkedHashMap<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        String group = rs.getString("setting_group");
        String key = rs.getString("setting_key");
        String value = rs.getString("setting_value");
        grouped.computeIfAbsent(group, k -> new LinkedHashMap<>()).put(key, value);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to load settings", e);
    }
    return grouped;
  }

  public Map<String, String> findAllByGroup(String group) {
    String sql =
        "SELECT setting_key, setting_value FROM settings WHERE setting_group = ? "
            + "ORDER BY setting_key";
    Map<String, String> result = new LinkedHashMap<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, group);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          result.put(rs.getString("setting_key"), rs.getString("setting_value"));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to load settings for group: " + group, e);
    }
    return result;
  }

  public void upsert(String group, String key, String value, String dataType, Long updatedBy) {
    String sql =
        "INSERT INTO settings (setting_group, setting_key, setting_value, data_type, "
            + "updated_by) VALUES (?, ?, ?, ?, ?) "
            + "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value), "
            + "data_type = VALUES(data_type), updated_by = VALUES(updated_by)";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, group);
      ps.setString(2, key);
      ps.setString(3, value);
      ps.setString(4, dataType);
      if (updatedBy != null) {
        ps.setLong(5, updatedBy);
      } else {
        ps.setNull(5, Types.BIGINT);
      }
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to upsert setting: " + group + "." + key, e);
    }
  }

  public void upsertBatch(String group, Map<String, String> entries, Long updatedBy) {
    String sql =
        "INSERT INTO settings (setting_group, setting_key, setting_value, updated_by) "
            + "VALUES (?, ?, ?, ?) "
            + "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value), "
            + "updated_by = VALUES(updated_by)";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      for (Map.Entry<String, String> entry : entries.entrySet()) {
        ps.setString(1, group);
        ps.setString(2, entry.getKey());
        ps.setString(3, entry.getValue());
        if (updatedBy != null) {
          ps.setLong(4, updatedBy);
        } else {
          ps.setNull(4, Types.BIGINT);
        }
        ps.addBatch();
      }
      ps.executeBatch();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to batch upsert settings for group: " + group, e);
    }
  }
}
