package com.group4.inventoryserver.repository;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.dto.supplier.SupplierData;
import com.group4.inventoryserver.util.DateUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SupplierRepository {

  private static final Map<String, String> SORT_COLUMN_MAP = new HashMap<>();

  static {
    SORT_COLUMN_MAP.put("supplierId", "s.supplier_id");
    SORT_COLUMN_MAP.put("supplierCode", "s.supplier_code");
    SORT_COLUMN_MAP.put("supplierName", "s.supplier_name");
    SORT_COLUMN_MAP.put("type", "s.supplier_type");
    SORT_COLUMN_MAP.put("productCount", "product_count");
    SORT_COLUMN_MAP.put("status", "s.status");
    SORT_COLUMN_MAP.put("createdAt", "s.created_at");
    SORT_COLUMN_MAP.put("updatedAt", "s.updated_at");
  }

  private static final String BASE_SELECT =
      "SELECT s.supplier_id, s.supplier_code, s.supplier_name, s.contact_person, "
          + "s.phone, s.email, s.address, s.supplier_type, s.preferred, "
          + "s.status, s.created_at, s.updated_at, "
          + "COUNT(p.product_id) AS product_count "
          + "FROM suppliers s "
          + "LEFT JOIN products p ON s.supplier_id = p.supplier_id AND p.status != 'Inactive' ";

  public List<SupplierData> findAll(
      int offset,
      int limit,
      String sortBy,
      String sortDir,
      String search,
      String status,
      String type) {
    StringBuilder sql = new StringBuilder(BASE_SELECT);
    List<Object> params = new ArrayList<>();
    appendWhereClause(sql, params, search, status, type);
    sql.append(" GROUP BY s.supplier_id");

    String column = SORT_COLUMN_MAP.getOrDefault(sortBy, "s.supplier_name");
    String direction = "desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
    sql.append(" ORDER BY ").append(column).append(' ').append(direction);
    sql.append(" LIMIT ? OFFSET ?");
    params.add(limit);
    params.add(offset);

    List<SupplierData> results = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql.toString())) {
      setParams(ps, params);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          results.add(mapRow(rs));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to list suppliers", e);
    }
    return results;
  }

  public long count(String search, String status, String type) {
    StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM suppliers s");
    List<Object> params = new ArrayList<>();
    appendWhereClause(sql, params, search, status, type);

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql.toString())) {
      setParams(ps, params);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return rs.getLong(1);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to count suppliers", e);
    }
    return 0;
  }

  public SupplierData findById(long supplierId) {
    String sql = BASE_SELECT + " WHERE s.supplier_id = ? GROUP BY s.supplier_id";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, supplierId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return mapRow(rs);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to find supplier by id", e);
    }
    return null;
  }

  public boolean existsByName(String name) {
    String sql = "SELECT 1 FROM suppliers WHERE supplier_name = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, name);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to check supplier name existence", e);
    }
  }

  public boolean existsByNameExcluding(String name, long excludeId) {
    String sql = "SELECT 1 FROM suppliers WHERE supplier_name = ? AND supplier_id != ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, name);
      ps.setLong(2, excludeId);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to check supplier name existence", e);
    }
  }

  public long insert(
      String supplierCode,
      String supplierName,
      String contactPerson,
      String phone,
      String email,
      String address,
      String type,
      boolean preferred,
      String status,
      Long createdBy) {
    String sql =
        "INSERT INTO suppliers (supplier_code, supplier_name, contact_person, phone, email, "
            + "address, supplier_type, preferred, status, created_by, updated_by) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, supplierCode);
      ps.setString(2, supplierName);
      setNullableString(ps, 3, contactPerson);
      setNullableString(ps, 4, phone);
      setNullableString(ps, 5, email);
      setNullableString(ps, 6, address);
      ps.setString(7, type);
      ps.setBoolean(8, preferred);
      ps.setString(9, status);
      if (createdBy != null) {
        ps.setLong(10, createdBy);
        ps.setLong(11, createdBy);
      } else {
        ps.setNull(10, Types.BIGINT);
        ps.setNull(11, Types.BIGINT);
      }
      ps.executeUpdate();
      try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next()) return keys.getLong(1);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to insert supplier", e);
    }
    return -1;
  }

  public void update(
      long supplierId,
      String supplierName,
      String contactPerson,
      String phone,
      String email,
      String address,
      String type,
      Boolean preferred,
      String status,
      Long updatedBy) {
    List<String> setClauses = new ArrayList<>();
    List<Object> params = new ArrayList<>();

    if (supplierName != null) {
      setClauses.add("supplier_name = ?");
      params.add(supplierName);
    }
    if (contactPerson != null) {
      setClauses.add("contact_person = ?");
      params.add(contactPerson);
    }
    if (phone != null) {
      setClauses.add("phone = ?");
      params.add(phone);
    }
    if (email != null) {
      setClauses.add("email = ?");
      params.add(email);
    }
    if (address != null) {
      setClauses.add("address = ?");
      params.add(address);
    }
    if (type != null) {
      setClauses.add("supplier_type = ?");
      params.add(type);
    }
    if (preferred != null) {
      setClauses.add("preferred = ?");
      params.add(preferred);
    }
    if (status != null) {
      setClauses.add("status = ?");
      params.add(status);
    }
    if (updatedBy != null) {
      setClauses.add("updated_by = ?");
      params.add(updatedBy);
    }

    if (setClauses.isEmpty()) return;

    String sql = "UPDATE suppliers SET " + join(setClauses, ", ") + " WHERE supplier_id = ?";
    params.add(supplierId);

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      setParams(ps, params);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to update supplier", e);
    }
  }

  public void deactivate(long supplierId, Long updatedBy) {
    String sql = "UPDATE suppliers SET status = 'Inactive', updated_by = ? WHERE supplier_id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      if (updatedBy != null) {
        ps.setLong(1, updatedBy);
      } else {
        ps.setNull(1, Types.BIGINT);
      }
      ps.setLong(2, supplierId);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to deactivate supplier", e);
    }
  }

  public String generateNextCode() {
    String sql = "SELECT supplier_code FROM suppliers ORDER BY supplier_id DESC LIMIT 1";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
      if (rs.next()) {
        String lastCode = rs.getString("supplier_code");
        int num = Integer.parseInt(lastCode.replace("SUP", ""));
        return String.format("SUP%03d", num + 1);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to generate next supplier code", e);
    }
    return "SUP001";
  }

  private void appendWhereClause(
      StringBuilder sql, List<Object> params, String search, String status, String type) {
    List<String> conditions = new ArrayList<>();
    if (search != null && !search.trim().isEmpty()) {
      conditions.add(
          "(s.supplier_code LIKE ? OR s.supplier_name LIKE ? OR s.contact_person LIKE ? "
              + "OR s.phone LIKE ? OR s.email LIKE ? OR s.address LIKE ?)");
      String pattern = "%" + search.trim() + "%";
      params.add(pattern);
      params.add(pattern);
      params.add(pattern);
      params.add(pattern);
      params.add(pattern);
      params.add(pattern);
    }
    if (status != null && !status.trim().isEmpty()) {
      conditions.add("s.status = ?");
      params.add(status);
    }
    if (type != null && !type.trim().isEmpty()) {
      conditions.add("s.supplier_type = ?");
      params.add(type);
    }
    if (!conditions.isEmpty()) {
      sql.append(" WHERE ").append(join(conditions, " AND "));
    }
  }

  private SupplierData mapRow(ResultSet rs) throws SQLException {
    Timestamp createdAt = rs.getTimestamp("created_at");
    Timestamp updatedAt = rs.getTimestamp("updated_at");
    return new SupplierData(
        rs.getLong("supplier_id"),
        rs.getString("supplier_code"),
        rs.getString("supplier_name"),
        rs.getString("contact_person"),
        rs.getString("phone"),
        rs.getString("email"),
        rs.getString("address"),
        rs.getString("supplier_type"),
        rs.getBoolean("preferred"),
        rs.getInt("product_count"),
        rs.getString("status"),
        createdAt != null ? DateUtil.formatIso(createdAt) : null,
        updatedAt != null ? DateUtil.formatIso(updatedAt) : null);
  }

  private void setNullableString(PreparedStatement ps, int index, String value)
      throws SQLException {
    if (value != null) {
      ps.setString(index, value);
    } else {
      ps.setNull(index, Types.VARCHAR);
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
      } else if (val instanceof Boolean) {
        ps.setBoolean(i + 1, (Boolean) val);
      } else {
        ps.setObject(i + 1, val);
      }
    }
  }

  private String join(List<String> parts, String separator) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < parts.size(); i++) {
      if (i > 0) sb.append(separator);
      sb.append(parts.get(i));
    }
    return sb.toString();
  }
}
