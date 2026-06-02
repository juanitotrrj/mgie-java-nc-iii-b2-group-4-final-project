package com.group4.inventoryserver.repository;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.dto.product.StockMovementData;
import com.group4.inventoryserver.util.DateUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class StockMovementRepository {

  public void create(
      long productId,
      String movementType,
      String referenceType,
      Long referenceId,
      String referenceNo,
      int quantityBefore,
      int quantityChange,
      int quantityAfter,
      String remarks,
      Long createdBy) {
    String sql =
        "INSERT INTO stock_movements (product_id, movement_type, reference_type, reference_id, "
            + "reference_no, quantity_before, quantity_change, quantity_after, remarks, created_by) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, productId);
      ps.setString(2, movementType);
      ps.setString(3, referenceType);
      if (referenceId != null) {
        ps.setLong(4, referenceId);
      } else {
        ps.setNull(4, Types.BIGINT);
      }
      ps.setString(5, referenceNo);
      ps.setInt(6, quantityBefore);
      ps.setInt(7, quantityChange);
      ps.setInt(8, quantityAfter);
      ps.setString(9, remarks);
      if (createdBy != null) {
        ps.setLong(10, createdBy);
      } else {
        ps.setNull(10, Types.BIGINT);
      }
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to create stock movement", e);
    }
  }

  public List<StockMovementData> findByProductId(long productId, int offset, int limit) {
    String sql =
        "SELECT stock_movement_id, product_id, movement_type, reference_type, reference_id, "
            + "reference_no, quantity_before, quantity_change, quantity_after, remarks, "
            + "created_by, created_at "
            + "FROM stock_movements WHERE product_id = ? "
            + "ORDER BY created_at DESC LIMIT ? OFFSET ?";
    List<StockMovementData> results = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, productId);
      ps.setInt(2, limit);
      ps.setInt(3, offset);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          results.add(mapRow(rs));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to find stock movements by product", e);
    }
    return results;
  }

  public long countByProductId(long productId) {
    String sql = "SELECT COUNT(*) FROM stock_movements WHERE product_id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, productId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return rs.getLong(1);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to count stock movements", e);
    }
    return 0;
  }

  private StockMovementData mapRow(ResultSet rs) throws SQLException {
    Timestamp createdAt = rs.getTimestamp("created_at");
    long refId = rs.getLong("reference_id");
    long createdBy = rs.getLong("created_by");
    return new StockMovementData(
        rs.getLong("stock_movement_id"),
        rs.getLong("product_id"),
        rs.getString("movement_type"),
        rs.getString("reference_type"),
        rs.wasNull() ? null : refId,
        rs.getString("reference_no"),
        rs.getInt("quantity_before"),
        rs.getInt("quantity_change"),
        rs.getInt("quantity_after"),
        rs.getString("remarks"),
        rs.wasNull() ? null : createdBy,
        createdAt != null ? DateUtil.formatIso(createdAt) : null);
  }
}
