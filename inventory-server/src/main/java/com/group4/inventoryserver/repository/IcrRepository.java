package com.group4.inventoryserver.repository;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.dto.icr.IcrData;
import com.group4.inventoryserver.dto.icr.IcrSummaryData;
import com.group4.inventoryserver.util.DateUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IcrRepository {

  private static final String BASE_SELECT =
      "SELECT icr.inventory_change_request_id, icr.request_no, icr.product_id, "
          + "p.product_code, p.product_name, icr.request_type, icr.current_quantity, "
          + "icr.requested_quantity, icr.quantity_change, icr.reason, icr.status, "
          + "icr.requested_by, req_u.full_name AS requester_name, icr.requested_at, "
          + "rev_u.full_name AS reviewer_name, icr.reviewed_at, icr.review_notes "
          + "FROM inventory_change_requests icr "
          + "JOIN products p ON icr.product_id = p.product_id "
          + "JOIN users req_u ON icr.requested_by = req_u.user_id "
          + "LEFT JOIN users rev_u ON icr.reviewed_by = rev_u.user_id";

  private static final Map<String, String> SORT_COLUMN_MAP = new HashMap<>();

  static {
    SORT_COLUMN_MAP.put("requestId", "icr.inventory_change_request_id");
    SORT_COLUMN_MAP.put("requestNo", "icr.request_no");
    SORT_COLUMN_MAP.put("productName", "p.product_name");
    SORT_COLUMN_MAP.put("requestType", "icr.request_type");
    SORT_COLUMN_MAP.put("status", "icr.status");
    SORT_COLUMN_MAP.put("requestedAt", "icr.requested_at");
    SORT_COLUMN_MAP.put("requestedBy", "req_u.full_name");
  }

  public List<IcrData> findAll(
      int offset,
      int limit,
      String sortBy,
      String sortDir,
      String search,
      String status,
      Long productId,
      Long requesterId,
      String dateFrom,
      String dateTo) {
    StringBuilder sql = new StringBuilder(BASE_SELECT);
    List<Object> params = new ArrayList<>();
    appendWhereClause(sql, params, search, status, productId, requesterId, dateFrom, dateTo);

    String column = SORT_COLUMN_MAP.getOrDefault(sortBy, "icr.requested_at");
    String direction = "desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
    sql.append(" ORDER BY ").append(column).append(' ').append(direction);
    sql.append(" LIMIT ? OFFSET ?");
    params.add(limit);
    params.add(offset);

    List<IcrData> results = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql.toString())) {
      setParams(ps, params);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          results.add(mapIcrData(rs));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to list ICRs", e);
    }
    return results;
  }

  public long count(
      String search,
      String status,
      Long productId,
      Long requesterId,
      String dateFrom,
      String dateTo) {
    StringBuilder sql =
        new StringBuilder(
            "SELECT COUNT(*) FROM inventory_change_requests icr "
                + "JOIN products p ON icr.product_id = p.product_id "
                + "JOIN users req_u ON icr.requested_by = req_u.user_id");
    List<Object> params = new ArrayList<>();
    appendWhereClause(sql, params, search, status, productId, requesterId, dateFrom, dateTo);

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql.toString())) {
      setParams(ps, params);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return rs.getLong(1);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to count ICRs", e);
    }
    return 0;
  }

  public IcrData findById(long id) {
    String sql = BASE_SELECT + " WHERE icr.inventory_change_request_id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return mapIcrData(rs);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to find ICR by id", e);
    }
    return null;
  }

  public long insert(
      String requestNo,
      long productId,
      String requestType,
      int currentQty,
      Integer requestedQty,
      Integer qtyChange,
      String reason,
      long requestedBy) {
    String sql =
        "INSERT INTO inventory_change_requests "
            + "(request_no, product_id, request_type, current_quantity, requested_quantity, "
            + "quantity_change, reason, requested_by) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, requestNo);
      ps.setLong(2, productId);
      ps.setString(3, requestType);
      ps.setInt(4, currentQty);
      setNullableInt(ps, 5, requestedQty);
      setNullableInt(ps, 6, qtyChange);
      ps.setString(7, reason);
      ps.setLong(8, requestedBy);
      ps.executeUpdate();
      try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next()) return keys.getLong(1);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to insert ICR", e);
    }
    return -1;
  }

  public void insertFile(
      long icrId,
      String originalFilename,
      String storedFilename,
      String storagePath,
      String mimeType,
      long fileSizeBytes,
      long uploadedBy) {
    String sql =
        "INSERT INTO inventory_change_request_files "
            + "(inventory_change_request_id, original_filename, stored_filename, "
            + "storage_path, mime_type, file_size_bytes, uploaded_by) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, icrId);
      ps.setString(2, originalFilename);
      ps.setString(3, storedFilename);
      ps.setString(4, storagePath);
      ps.setString(5, mimeType);
      ps.setLong(6, fileSizeBytes);
      ps.setLong(7, uploadedBy);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to insert ICR file", e);
    }
  }

  public Map<String, Object> findFileByIcrId(long icrId) {
    String sql =
        "SELECT file_id, original_filename, stored_filename, storage_path, mime_type, "
            + "file_size_bytes FROM inventory_change_request_files "
            + "WHERE inventory_change_request_id = ? LIMIT 1";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, icrId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          Map<String, Object> file = new HashMap<>();
          file.put("fileId", rs.getLong("file_id"));
          file.put("originalFilename", rs.getString("original_filename"));
          file.put("storedFilename", rs.getString("stored_filename"));
          file.put("storagePath", rs.getString("storage_path"));
          file.put("mimeType", rs.getString("mime_type"));
          file.put("fileSizeBytes", rs.getLong("file_size_bytes"));
          return file;
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to find ICR file", e);
    }
    return null;
  }

  public void approve(Connection conn, long id, long reviewedBy, String notes, long stockMovementId)
      throws SQLException {
    String sql =
        "UPDATE inventory_change_requests SET status = 'Approved', reviewed_by = ?, "
            + "reviewed_at = NOW(), review_notes = ?, stock_movement_id = ? "
            + "WHERE inventory_change_request_id = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, reviewedBy);
      ps.setString(2, notes);
      ps.setLong(3, stockMovementId);
      ps.setLong(4, id);
      ps.executeUpdate();
    }
  }

  public void reject(long id, long reviewedBy, String reason) {
    String sql =
        "UPDATE inventory_change_requests SET status = 'Rejected', reviewed_by = ?, "
            + "reviewed_at = NOW(), review_notes = ? WHERE inventory_change_request_id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, reviewedBy);
      ps.setString(2, reason);
      ps.setLong(3, id);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to reject ICR", e);
    }
  }

  public String generateNextRequestNo() {
    String year = String.valueOf(Year.now().getValue());
    String prefix = "ICR-" + year + "-";
    String sql =
        "SELECT request_no FROM inventory_change_requests "
            + "WHERE request_no LIKE ? ORDER BY inventory_change_request_id DESC LIMIT 1";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, prefix + "%");
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          String last = rs.getString("request_no");
          String numPart = last.substring(prefix.length());
          int num = Integer.parseInt(numPart);
          return prefix + String.format("%06d", num + 1);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to generate ICR request number", e);
    }
    return prefix + "000001";
  }

  public IcrSummaryData countByStatus(long requestedBy) {
    String sql =
        "SELECT "
            + "SUM(CASE WHEN status = 'Pending' THEN 1 ELSE 0 END) AS pending, "
            + "SUM(CASE WHEN status = 'Approved' THEN 1 ELSE 0 END) AS approved, "
            + "SUM(CASE WHEN status = 'Rejected' THEN 1 ELSE 0 END) AS rejected "
            + "FROM inventory_change_requests WHERE requested_by = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, requestedBy);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return new IcrSummaryData(
              rs.getLong("pending"), rs.getLong("approved"), rs.getLong("rejected"));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to count ICRs by status", e);
    }
    return new IcrSummaryData(0, 0, 0);
  }

  public int getProductQuantity(Connection conn, long productId) throws SQLException {
    String sql = "SELECT quantity FROM products WHERE product_id = ? FOR UPDATE";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, productId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return rs.getInt("quantity");
      }
    }
    return -1;
  }

  public void updateProductQuantity(Connection conn, long productId, int newQuantity)
      throws SQLException {
    String sql = "UPDATE products SET quantity = ? WHERE product_id = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, newQuantity);
      ps.setLong(2, productId);
      ps.executeUpdate();
    }
  }

  public long insertStockMovement(
      Connection conn,
      long productId,
      String movementType,
      String referenceType,
      long referenceId,
      String referenceNo,
      int qtyBefore,
      int qtyChange,
      int qtyAfter,
      String remarks,
      long createdBy)
      throws SQLException {
    String sql =
        "INSERT INTO stock_movements (product_id, movement_type, reference_type, "
            + "reference_id, reference_no, quantity_before, quantity_change, quantity_after, "
            + "remarks, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setLong(1, productId);
      ps.setString(2, movementType);
      ps.setString(3, referenceType);
      ps.setLong(4, referenceId);
      ps.setString(5, referenceNo);
      ps.setInt(6, qtyBefore);
      ps.setInt(7, qtyChange);
      ps.setInt(8, qtyAfter);
      ps.setString(9, remarks);
      ps.setLong(10, createdBy);
      ps.executeUpdate();
      try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next()) return keys.getLong(1);
      }
    }
    return -1;
  }

  // ─── Helpers ────────────────────────────────────────────────────────────────

  private IcrData mapIcrData(ResultSet rs) throws SQLException {
    Timestamp requestedAt = rs.getTimestamp("requested_at");
    Timestamp reviewedAt = rs.getTimestamp("reviewed_at");
    int reqQty = rs.getInt("requested_quantity");
    Integer requestedQuantity = rs.wasNull() ? null : reqQty;
    int qtyChange = rs.getInt("quantity_change");
    Integer quantityChange = rs.wasNull() ? null : qtyChange;

    return new IcrData(
        rs.getLong("inventory_change_request_id"),
        rs.getString("request_no"),
        rs.getLong("product_id"),
        rs.getString("product_code"),
        rs.getString("product_name"),
        rs.getString("request_type"),
        rs.getInt("current_quantity"),
        requestedQuantity,
        quantityChange,
        rs.getString("reason"),
        rs.getString("status"),
        rs.getLong("requested_by"),
        rs.getString("requester_name"),
        requestedAt != null ? DateUtil.formatIso(requestedAt) : null,
        rs.getString("reviewer_name"),
        reviewedAt != null ? DateUtil.formatIso(reviewedAt) : null,
        rs.getString("review_notes"));
  }

  private void appendWhereClause(
      StringBuilder sql,
      List<Object> params,
      String search,
      String status,
      Long productId,
      Long requesterId,
      String dateFrom,
      String dateTo) {
    List<String> conditions = new ArrayList<>();
    if (search != null && !search.trim().isEmpty()) {
      conditions.add("(icr.request_no LIKE ? OR p.product_name LIKE ? OR req_u.full_name LIKE ?)");
      String pattern = "%" + search.trim() + "%";
      params.add(pattern);
      params.add(pattern);
      params.add(pattern);
    }
    if (status != null && !status.trim().isEmpty()) {
      conditions.add("icr.status = ?");
      params.add(status);
    }
    if (productId != null) {
      conditions.add("icr.product_id = ?");
      params.add(productId);
    }
    if (requesterId != null) {
      conditions.add("icr.requested_by = ?");
      params.add(requesterId);
    }
    if (dateFrom != null && !dateFrom.trim().isEmpty()) {
      conditions.add("icr.requested_at >= ?");
      params.add(dateFrom + " 00:00:00");
    }
    if (dateTo != null && !dateTo.trim().isEmpty()) {
      conditions.add("icr.requested_at <= ?");
      params.add(dateTo + " 23:59:59");
    }
    if (!conditions.isEmpty()) {
      sql.append(" WHERE ");
      for (int i = 0; i < conditions.size(); i++) {
        if (i > 0) sql.append(" AND ");
        sql.append(conditions.get(i));
      }
    }
  }

  private void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
    if (value != null) {
      ps.setInt(index, value);
    } else {
      ps.setNull(index, Types.INTEGER);
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
