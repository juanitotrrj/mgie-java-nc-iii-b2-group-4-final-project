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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockMovementRepository {

  private static final String BASE_SELECT =
      "SELECT sm.stock_movement_id, sm.product_id, p.product_code, p.product_name, "
          + "sm.movement_type, sm.reference_type, sm.reference_id, sm.reference_no, "
          + "sm.quantity_before, sm.quantity_change, sm.quantity_after, sm.remarks, "
          + "sm.created_by, u.full_name AS created_by_name, sm.created_at "
          + "FROM stock_movements sm "
          + "JOIN products p ON sm.product_id = p.product_id "
          + "LEFT JOIN users u ON sm.created_by = u.user_id";

  private static final Map<String, String> SORT_COLUMN_MAP = new HashMap<>();

  static {
    SORT_COLUMN_MAP.put("createdAt", "sm.created_at");
    SORT_COLUMN_MAP.put("movementType", "sm.movement_type");
    SORT_COLUMN_MAP.put("productName", "p.product_name");
    SORT_COLUMN_MAP.put("quantityChange", "sm.quantity_change");
    SORT_COLUMN_MAP.put("referenceType", "sm.reference_type");
  }

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

  public List<StockMovementData> findAll(
      int offset,
      int limit,
      String sortBy,
      String sortDir,
      String search,
      Long productId,
      String movementType,
      String referenceType,
      String dateFrom,
      String dateTo) {
    StringBuilder sql = new StringBuilder(BASE_SELECT);
    List<Object> params = new ArrayList<>();
    appendWhereClause(
        sql, params, search, productId, movementType, referenceType, dateFrom, dateTo);

    String column = SORT_COLUMN_MAP.getOrDefault(sortBy, "sm.created_at");
    String direction = "desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
    sql.append(" ORDER BY ").append(column).append(' ').append(direction);
    sql.append(" LIMIT ? OFFSET ?");
    params.add(limit);
    params.add(offset);

    List<StockMovementData> results = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql.toString())) {
      setParams(ps, params);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          results.add(mapRow(rs));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to list stock movements", e);
    }
    return results;
  }

  public long count(
      String search,
      Long productId,
      String movementType,
      String referenceType,
      String dateFrom,
      String dateTo) {
    StringBuilder sql =
        new StringBuilder(
            "SELECT COUNT(*) FROM stock_movements sm "
                + "JOIN products p ON sm.product_id = p.product_id "
                + "LEFT JOIN users u ON sm.created_by = u.user_id");
    List<Object> params = new ArrayList<>();
    appendWhereClause(
        sql, params, search, productId, movementType, referenceType, dateFrom, dateTo);

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql.toString())) {
      setParams(ps, params);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return rs.getLong(1);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to count stock movements", e);
    }
    return 0;
  }

  public List<StockMovementData> findByProductId(long productId, int offset, int limit) {
    String sql =
        BASE_SELECT + " WHERE sm.product_id = ? ORDER BY sm.created_at DESC LIMIT ? OFFSET ?";
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
    boolean refIdNull = rs.wasNull();
    long createdBy = rs.getLong("created_by");
    boolean createdByNull = rs.wasNull();
    return new StockMovementData(
        rs.getLong("stock_movement_id"),
        rs.getLong("product_id"),
        rs.getString("product_code"),
        rs.getString("product_name"),
        rs.getString("movement_type"),
        rs.getString("reference_type"),
        refIdNull ? null : refId,
        rs.getString("reference_no"),
        rs.getInt("quantity_before"),
        rs.getInt("quantity_change"),
        rs.getInt("quantity_after"),
        rs.getString("remarks"),
        createdByNull ? null : createdBy,
        rs.getString("created_by_name"),
        createdAt != null ? DateUtil.formatIso(createdAt) : null);
  }

  private void appendWhereClause(
      StringBuilder sql,
      List<Object> params,
      String search,
      Long productId,
      String movementType,
      String referenceType,
      String dateFrom,
      String dateTo) {
    List<String> conditions = new ArrayList<>();
    if (search != null && !search.trim().isEmpty()) {
      conditions.add("(p.product_name LIKE ? OR p.product_code LIKE ? OR sm.reference_no LIKE ?)");
      String pattern = "%" + search.trim() + "%";
      params.add(pattern);
      params.add(pattern);
      params.add(pattern);
    }
    if (productId != null) {
      conditions.add("sm.product_id = ?");
      params.add(productId);
    }
    if (movementType != null && !movementType.trim().isEmpty()) {
      conditions.add("sm.movement_type = ?");
      params.add(movementType.trim());
    }
    if (referenceType != null && !referenceType.trim().isEmpty()) {
      conditions.add("sm.reference_type = ?");
      params.add(referenceType.trim());
    }
    if (dateFrom != null && !dateFrom.trim().isEmpty()) {
      conditions.add("sm.created_at >= ?");
      params.add(dateFrom.trim() + " 00:00:00");
    }
    if (dateTo != null && !dateTo.trim().isEmpty()) {
      conditions.add("sm.created_at <= ?");
      params.add(dateTo.trim() + " 23:59:59");
    }
    if (!conditions.isEmpty()) {
      sql.append(" WHERE ");
      for (int i = 0; i < conditions.size(); i++) {
        if (i > 0) sql.append(" AND ");
        sql.append(conditions.get(i));
      }
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
}
