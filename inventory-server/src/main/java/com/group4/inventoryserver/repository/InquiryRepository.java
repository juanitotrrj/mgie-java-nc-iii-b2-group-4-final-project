package com.group4.inventoryserver.repository;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.dto.guest.InquiryData;
import com.group4.inventoryserver.dto.guest.InquiryRequest;
import com.group4.inventoryserver.util.DateUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

public class InquiryRepository {

  private static final String INSERT_SQL =
      "INSERT INTO guest_inquiries (name, email, subject, message) VALUES (?, ?, ?, ?)";

  public InquiryData save(InquiryRequest request) {
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, request.getName().trim());
      ps.setString(2, request.getEmail().trim());
      ps.setString(3, request.getSubject().trim());
      ps.setString(4, request.getMessage().trim());
      ps.executeUpdate();

      try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next()) {
          long id = keys.getLong(1);
          String submittedAt = fetchCreatedAt(conn, id);
          return new InquiryData(id, submittedAt);
        }
      }
      throw new SQLException("Failed to retrieve generated key for guest_inquiries insert.");
    } catch (SQLException e) {
      throw new RuntimeException("Failed to save guest inquiry", e);
    }
  }

  private String fetchCreatedAt(Connection conn, long id) throws SQLException {
    try (PreparedStatement ps =
        conn.prepareStatement(
            "SELECT created_at FROM guest_inquiries WHERE guest_inquiry_id = ?")) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          Timestamp ts = rs.getTimestamp("created_at");
          return DateUtil.formatIso(ts);
        }
      }
    }
    return DateUtil.nowIso();
  }
}
