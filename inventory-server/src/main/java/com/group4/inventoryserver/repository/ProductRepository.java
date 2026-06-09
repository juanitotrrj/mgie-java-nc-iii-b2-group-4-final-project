package com.group4.inventoryserver.repository;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.dto.product.ProductData;
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

public class ProductRepository {

  private static final Map<String, String> SORT_COLUMN_MAP = new HashMap<>();

  static {
    SORT_COLUMN_MAP.put("productId", "p.product_id");
    SORT_COLUMN_MAP.put("productCode", "p.product_code");
    SORT_COLUMN_MAP.put("productName", "p.product_name");
    SORT_COLUMN_MAP.put("categoryName", "c.category_name");
    SORT_COLUMN_MAP.put("quantity", "p.quantity");
    SORT_COLUMN_MAP.put("unitPrice", "p.unit_price");
    SORT_COLUMN_MAP.put("status", "p.status");
    SORT_COLUMN_MAP.put("createdAt", "p.created_at");
    SORT_COLUMN_MAP.put("updatedAt", "p.updated_at");
  }

  private static final String BASE_SELECT =
      "SELECT p.product_id, p.product_code, p.product_name, p.category_id, c.category_name, "
          + "p.supplier_id, s.supplier_name, p.quantity, p.reorder_level, p.unit_price, "
          + "p.status, p.created_at, p.updated_at "
          + "FROM products p "
          + "JOIN categories c ON p.category_id = c.category_id "
          + "LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id";

  public List<ProductData> findAll(
      int offset,
      int limit,
      String sortBy,
      String sortDir,
      String search,
      Long categoryId,
      String status) {
    StringBuilder sql = new StringBuilder(BASE_SELECT);
    List<Object> params = new ArrayList<>();
    appendWhereClause(sql, params, search, categoryId, status);

    String column = SORT_COLUMN_MAP.getOrDefault(sortBy, "p.product_name");
    String direction = "desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
    sql.append(" ORDER BY ").append(column).append(' ').append(direction);
    sql.append(" LIMIT ? OFFSET ?");
    params.add(limit);
    params.add(offset);

    List<ProductData> results = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql.toString())) {
      setParams(ps, params);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          results.add(mapRow(rs));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to list products", e);
    }
    return results;
  }

  public long count(String search, Long categoryId, String status) {
    StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM products p");
    sql.append(" JOIN categories c ON p.category_id = c.category_id");
    List<Object> params = new ArrayList<>();
    appendWhereClause(sql, params, search, categoryId, status);

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql.toString())) {
      setParams(ps, params);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return rs.getLong(1);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to count products", e);
    }
    return 0;
  }

  public ProductData findById(long productId) {
    String sql = BASE_SELECT + " WHERE p.product_id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, productId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return mapRow(rs);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to find product by id", e);
    }
    return null;
  }

  public boolean existsByProductCode(String code) {
    String sql = "SELECT 1 FROM products WHERE product_code = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, code);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to check product code existence", e);
    }
  }

  public boolean existsByProductCodeExcluding(String code, long excludeId) {
    String sql = "SELECT 1 FROM products WHERE product_code = ? AND product_id != ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, code);
      ps.setLong(2, excludeId);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to check product code existence", e);
    }
  }

  public boolean categoryExistsAndActive(long categoryId) {
    String sql = "SELECT 1 FROM categories WHERE category_id = ? AND status = 'Active'";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, categoryId);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to check category", e);
    }
  }

  public boolean supplierExists(long supplierId) {
    String sql = "SELECT 1 FROM suppliers WHERE supplier_id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, supplierId);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to check supplier", e);
    }
  }

  public long insert(
      String productCode,
      String productName,
      long categoryId,
      Long supplierId,
      int quantity,
      int reorderLevel,
      double unitPrice,
      String status,
      Long createdBy) {
    String sql =
        "INSERT INTO products (product_code, product_name, category_id, supplier_id, quantity, "
            + "reorder_level, unit_price, status, created_by, updated_by) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, productCode);
      ps.setString(2, productName);
      ps.setLong(3, categoryId);
      if (supplierId != null) {
        ps.setLong(4, supplierId);
      } else {
        ps.setNull(4, Types.BIGINT);
      }
      ps.setInt(5, quantity);
      ps.setInt(6, reorderLevel);
      ps.setDouble(7, unitPrice);
      ps.setString(8, status);
      if (createdBy != null) {
        ps.setLong(9, createdBy);
        ps.setLong(10, createdBy);
      } else {
        ps.setNull(9, Types.BIGINT);
        ps.setNull(10, Types.BIGINT);
      }
      ps.executeUpdate();
      try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next()) return keys.getLong(1);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to insert product", e);
    }
    return -1;
  }

  public void update(
      long productId,
      String productName,
      Long categoryId,
      Long supplierId,
      Integer reorderLevel,
      Double unitPrice,
      String status,
      Long updatedBy) {
    List<String> setClauses = new ArrayList<>();
    List<Object> params = new ArrayList<>();

    if (productName != null) {
      setClauses.add("product_name = ?");
      params.add(productName);
    }
    if (categoryId != null) {
      setClauses.add("category_id = ?");
      params.add(categoryId);
    }
    if (supplierId != null) {
      setClauses.add("supplier_id = ?");
      params.add(supplierId);
    }
    if (reorderLevel != null) {
      setClauses.add("reorder_level = ?");
      params.add(reorderLevel);
    }
    if (unitPrice != null) {
      setClauses.add("unit_price = ?");
      params.add(unitPrice);
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

    String sql = "UPDATE products SET " + join(setClauses, ", ") + " WHERE product_id = ?";
    params.add(productId);

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      setParams(ps, params);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to update product", e);
    }
  }

  public void deactivate(long productId, Long updatedBy) {
    String sql = "UPDATE products SET status = 'Inactive', updated_by = ? WHERE product_id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      if (updatedBy != null) {
        ps.setLong(1, updatedBy);
      } else {
        ps.setNull(1, Types.BIGINT);
      }
      ps.setLong(2, productId);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to deactivate product", e);
    }
  }

  private void appendWhereClause(
      StringBuilder sql, List<Object> params, String search, Long categoryId, String status) {
    List<String> conditions = new ArrayList<>();
    if (search != null && !search.trim().isEmpty()) {
      conditions.add(
          "(p.product_code LIKE ? OR p.product_name LIKE ? OR c.category_name LIKE ? "
              + "OR s.supplier_name LIKE ?)");
      String pattern = "%" + search.trim() + "%";
      params.add(pattern);
      params.add(pattern);
      params.add(pattern);
      params.add(pattern);
    }
    if (categoryId != null) {
      conditions.add("p.category_id = ?");
      params.add(categoryId);
    }
    if (status != null && !status.trim().isEmpty()) {
      if ("Active".equalsIgnoreCase(status)) {
        conditions.add("p.status <> 'Inactive'");
      } else if ("Inactive".equalsIgnoreCase(status)) {
        conditions.add("p.status = 'Inactive'");
      } else {
        conditions.add("p.status = ?");
        params.add(status);
      }
    }
    if (!conditions.isEmpty()) {
      sql.append(" WHERE ").append(join(conditions, " AND "));
    }
  }

  private ProductData mapRow(ResultSet rs) throws SQLException {
    Timestamp createdAt = rs.getTimestamp("created_at");
    Timestamp updatedAt = rs.getTimestamp("updated_at");
    long supplierId = rs.getLong("supplier_id");
    return new ProductData(
        rs.getLong("product_id"),
        rs.getString("product_code"),
        rs.getString("product_name"),
        rs.getLong("category_id"),
        rs.getString("category_name"),
        rs.wasNull() ? null : supplierId,
        rs.getString("supplier_name"),
        rs.getInt("quantity"),
        rs.getInt("reorder_level"),
        rs.getDouble("unit_price"),
        rs.getString("status"),
        createdAt != null ? DateUtil.formatIso(createdAt) : null,
        updatedAt != null ? DateUtil.formatIso(updatedAt) : null);
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
      } else if (val instanceof Double) {
        ps.setDouble(i + 1, (Double) val);
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
