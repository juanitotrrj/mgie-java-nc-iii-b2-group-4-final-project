package com.group4.inventoryserver.repository;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.dto.audit.AuditLogData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class AuditLogRepository {

  public List<AuditLogData> findAll(
      int offset,
      int limit,
      String sortBy,
      String sortDir,
      String search,
      Long userId,
      String action,
      String module,
      String dateFrom,
      String dateTo) {
    StringBuilder sql =
        new StringBuilder(
            "SELECT audit_log_id, created_at, user_id, username, action, module, "
                + "reference_no, details FROM audit_logs WHERE 1=1");
    List<Object> params = new ArrayList<>();
    appendWhereClause(sql, params, search, userId, action, module, dateFrom, dateTo);
    String col = mapSortColumn(sortBy);
    String dir = "desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
    sql.append(" ORDER BY ").append(col).append(' ').append(dir);
    sql.append(" LIMIT ? OFFSET ?");
    params.add(limit);
    params.add(offset);

    List<AuditLogData> results = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql.toString())) {
      setParams(ps, params);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          results.add(mapRow(rs));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to query audit logs", e);
    }
    return results;
  }

  public long count(
      String search, Long userId, String action, String module, String dateFrom, String dateTo) {
    StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM audit_logs WHERE 1=1");
    List<Object> params = new ArrayList<>();
    appendWhereClause(sql, params, search, userId, action, module, dateFrom, dateTo);

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql.toString())) {
      setParams(ps, params);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? rs.getLong(1) : 0;
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to count audit logs", e);
    }
  }

  public void insert(
      Long userId,
      String username,
      String action,
      String module,
      String referenceNo,
      String details,
      String ipAddress) {
    String sql =
        "INSERT INTO audit_logs (user_id, username, action, module, reference_no, "
            + "details, ip_address) VALUES (?, ?, ?, ?, ?, ?, ?)";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      if (userId != null) {
        ps.setLong(1, userId);
      } else {
        ps.setNull(1, Types.BIGINT);
      }
      ps.setString(2, username);
      ps.setString(3, action);
      ps.setString(4, module);
      ps.setString(5, referenceNo);
      ps.setString(6, details);
      ps.setString(7, ipAddress);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to insert audit log", e);
    }
  }

  private void appendWhereClause(
      StringBuilder sql,
      List<Object> params,
      String search,
      Long userId,
      String action,
      String module,
      String dateFrom,
      String dateTo) {
    if (search != null && !search.trim().isEmpty()) {
      sql.append(
          " AND (username LIKE ? OR action LIKE ? OR module LIKE ? "
              + "OR reference_no LIKE ? OR details LIKE ?)");
      String like = "%" + search.trim() + "%";
      params.add(like);
      params.add(like);
      params.add(like);
      params.add(like);
      params.add(like);
    }
    if (userId != null) {
      sql.append(" AND user_id = ?");
      params.add(userId);
    }
    if (action != null && !action.trim().isEmpty()) {
      sql.append(" AND action = ?");
      params.add(action.trim());
    }
    if (module != null && !module.trim().isEmpty()) {
      sql.append(" AND module = ?");
      params.add(module.trim());
    }
    if (dateFrom != null && !dateFrom.trim().isEmpty()) {
      sql.append(" AND DATE(created_at) >= ?");
      params.add(dateFrom.trim());
    }
    if (dateTo != null && !dateTo.trim().isEmpty()) {
      sql.append(" AND DATE(created_at) <= ?");
      params.add(dateTo.trim());
    }
  }

  private String mapSortColumn(String sortBy) {
    if (sortBy == null) return "created_at";
    switch (sortBy) {
      case "auditLogId":
        return "audit_log_id";
      case "dateTime":
      case "createdAt":
        return "created_at";
      case "username":
        return "username";
      case "action":
        return "action";
      case "module":
        return "module";
      default:
        return "created_at";
    }
  }

  private AuditLogData mapRow(ResultSet rs) throws SQLException {
    Long uid = rs.getObject("user_id") != null ? rs.getLong("user_id") : null;
    return new AuditLogData(
        rs.getLong("audit_log_id"),
        rs.getString("created_at"),
        uid,
        rs.getString("username"),
        rs.getString("action"),
        rs.getString("module"),
        rs.getString("reference_no"),
        rs.getString("details"));
  }

  private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
    for (int i = 0; i < params.size(); i++) {
      Object param = params.get(i);
      if (param instanceof Long) {
        ps.setLong(i + 1, (Long) param);
      } else if (param instanceof Integer) {
        ps.setInt(i + 1, (Integer) param);
      } else {
        ps.setString(i + 1, (String) param);
      }
    }
  }
}
