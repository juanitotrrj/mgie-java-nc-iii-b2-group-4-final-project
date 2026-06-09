package com.group4.inventoryserver.repository;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.dto.category.CategoryData;
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

public class CategoryRepository {

  private static final Map<String, String> SORT_COLUMN_MAP = new HashMap<>();

  static {
    SORT_COLUMN_MAP.put("categoryId", "c.category_id");
    SORT_COLUMN_MAP.put("categoryCode", "c.category_code");
    SORT_COLUMN_MAP.put("categoryName", "c.category_name");
    SORT_COLUMN_MAP.put("type", "c.category_type");
    SORT_COLUMN_MAP.put("productCount", "product_count");
    SORT_COLUMN_MAP.put("status", "c.status");
    SORT_COLUMN_MAP.put("createdAt", "c.created_at");
    SORT_COLUMN_MAP.put("updatedAt", "c.updated_at");
  }

  private static final String BASE_SELECT =
      "SELECT c.category_id, c.category_code, c.category_name, c.description, "
          + "c.category_type, c.status, c.created_at, c.updated_at, "
          + "COUNT(p.product_id) AS product_count "
          + "FROM categories c "
          + "LEFT JOIN products p ON c.category_id = p.category_id AND p.status != 'Inactive' ";

  public List<CategoryData> findAll(
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
    sql.append(" GROUP BY c.category_id");

    String column = SORT_COLUMN_MAP.getOrDefault(sortBy, "c.category_name");
    String direction = "desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
    sql.append(" ORDER BY ").append(column).append(' ').append(direction);
    sql.append(" LIMIT ? OFFSET ?");
    params.add(limit);
    params.add(offset);

    List<CategoryData> results = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql.toString())) {
      setParams(ps, params);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          results.add(mapRow(rs));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to list categories", e);
    }
    return results;
  }

  public long count(String search, String status, String type) {
    StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM categories c");
    List<Object> params = new ArrayList<>();
    appendWhereClause(sql, params, search, status, type);

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql.toString())) {
      setParams(ps, params);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return rs.getLong(1);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to count categories", e);
    }
    return 0;
  }

  public CategoryData findById(long categoryId) {
    String sql = BASE_SELECT + " WHERE c.category_id = ? GROUP BY c.category_id";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, categoryId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return mapRow(rs);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to find category by id", e);
    }
    return null;
  }

  public boolean existsByName(String name) {
    String sql = "SELECT 1 FROM categories WHERE category_name = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, name);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to check category name existence", e);
    }
  }

  public boolean existsByNameExcluding(String name, long excludeId) {
    String sql = "SELECT 1 FROM categories WHERE category_name = ? AND category_id != ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, name);
      ps.setLong(2, excludeId);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to check category name existence", e);
    }
  }

  public long insert(
      String categoryCode,
      String categoryName,
      String description,
      String type,
      String status,
      Long createdBy) {
    String sql =
        "INSERT INTO categories (category_code, category_name, description, category_type, "
            + "status, created_by, updated_by) VALUES (?, ?, ?, ?, ?, ?, ?)";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, categoryCode);
      ps.setString(2, categoryName);
      if (description != null) {
        ps.setString(3, description);
      } else {
        ps.setNull(3, Types.VARCHAR);
      }
      ps.setString(4, type);
      ps.setString(5, status);
      if (createdBy != null) {
        ps.setLong(6, createdBy);
        ps.setLong(7, createdBy);
      } else {
        ps.setNull(6, Types.BIGINT);
        ps.setNull(7, Types.BIGINT);
      }
      ps.executeUpdate();
      try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next()) return keys.getLong(1);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to insert category", e);
    }
    return -1;
  }

  public void update(
      long categoryId,
      String categoryName,
      String description,
      String type,
      String status,
      Long updatedBy) {
    List<String> setClauses = new ArrayList<>();
    List<Object> params = new ArrayList<>();

    if (categoryName != null) {
      setClauses.add("category_name = ?");
      params.add(categoryName);
    }
    if (description != null) {
      setClauses.add("description = ?");
      params.add(description);
    }
    if (type != null) {
      setClauses.add("category_type = ?");
      params.add(type);
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

    String sql = "UPDATE categories SET " + join(setClauses, ", ") + " WHERE category_id = ?";
    params.add(categoryId);

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      setParams(ps, params);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to update category", e);
    }
  }

  public void deactivate(long categoryId, Long updatedBy) {
    String sql = "UPDATE categories SET status = 'Inactive', updated_by = ? WHERE category_id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      if (updatedBy != null) {
        ps.setLong(1, updatedBy);
      } else {
        ps.setNull(1, Types.BIGINT);
      }
      ps.setLong(2, categoryId);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to deactivate category", e);
    }
  }

  public boolean hasActiveProducts(long categoryId) {
    String sql = "SELECT 1 FROM products WHERE category_id = ? AND status != 'Inactive' LIMIT 1";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, categoryId);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to check active products for category", e);
    }
  }

  public String generateNextCode() {
    String sql = "SELECT category_code FROM categories ORDER BY category_id DESC LIMIT 1";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
      if (rs.next()) {
        String lastCode = rs.getString("category_code");
        int num = Integer.parseInt(lastCode.replace("CAT", ""));
        return String.format("CAT%03d", num + 1);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to generate next category code", e);
    }
    return "CAT001";
  }

  private void appendWhereClause(
      StringBuilder sql, List<Object> params, String search, String status, String type) {
    List<String> conditions = new ArrayList<>();
    if (search != null && !search.trim().isEmpty()) {
      conditions.add("(c.category_code LIKE ? OR c.category_name LIKE ? OR c.description LIKE ?)");
      String pattern = "%" + search.trim() + "%";
      params.add(pattern);
      params.add(pattern);
      params.add(pattern);
    }
    if (status != null && !status.trim().isEmpty()) {
      conditions.add("c.status = ?");
      params.add(status);
    }
    if (type != null && !type.trim().isEmpty()) {
      conditions.add("c.category_type = ?");
      params.add(type);
    }
    if (!conditions.isEmpty()) {
      sql.append(" WHERE ").append(join(conditions, " AND "));
    }
  }

  private CategoryData mapRow(ResultSet rs) throws SQLException {
    Timestamp createdAt = rs.getTimestamp("created_at");
    Timestamp updatedAt = rs.getTimestamp("updated_at");
    return new CategoryData(
        rs.getLong("category_id"),
        rs.getString("category_code"),
        rs.getString("category_name"),
        rs.getString("description"),
        rs.getString("category_type"),
        rs.getInt("product_count"),
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
