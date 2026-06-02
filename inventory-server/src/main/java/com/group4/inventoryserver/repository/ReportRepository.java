package com.group4.inventoryserver.repository;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.dto.report.CategoryValueBreakdown;
import com.group4.inventoryserver.dto.report.InventoryValueProduct;
import com.group4.inventoryserver.dto.report.LowStockItem;
import com.group4.inventoryserver.dto.report.ReportOptionsData;
import com.group4.inventoryserver.dto.report.TopSellingProduct;
import com.group4.inventoryserver.dto.report.TrendPoint;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportRepository {

  // --- Report Options ---

  public List<ReportOptionsData.CategoryOption> getActiveCategories() {
    String sql =
        "SELECT category_id, category_name FROM categories WHERE status = 'Active' "
            + "ORDER BY category_name";
    List<ReportOptionsData.CategoryOption> results = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        results.add(
            new ReportOptionsData.CategoryOption(
                rs.getLong("category_id"), rs.getString("category_name")));
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to load categories for report options", e);
    }
    return results;
  }

  public List<ReportOptionsData.SupplierOption> getActiveSuppliers() {
    String sql =
        "SELECT supplier_id, supplier_name FROM suppliers WHERE status = 'Active' "
            + "ORDER BY supplier_name";
    List<ReportOptionsData.SupplierOption> results = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        results.add(
            new ReportOptionsData.SupplierOption(
                rs.getLong("supplier_id"), rs.getString("supplier_name")));
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to load suppliers for report options", e);
    }
    return results;
  }

  // --- Sales Summary ---

  public double getSalesTotalAmount(String dateFrom, String dateTo, Long categoryId) {
    StringBuilder sql = new StringBuilder("SELECT COALESCE(SUM(s.total_amount), 0) FROM sales s");
    List<Object> params = new ArrayList<>();
    appendSalesJoinAndWhere(sql, params, dateFrom, dateTo, categoryId);
    return queryDouble(sql.toString(), params);
  }

  public int getSalesOrderCount(String dateFrom, String dateTo, Long categoryId) {
    StringBuilder sql = new StringBuilder("SELECT COUNT(DISTINCT s.sale_id) FROM sales s");
    List<Object> params = new ArrayList<>();
    appendSalesJoinAndWhere(sql, params, dateFrom, dateTo, categoryId);
    return (int) queryLong(sql.toString(), params);
  }

  public String getSalesBestDay(String dateFrom, String dateTo, Long categoryId) {
    StringBuilder sql =
        new StringBuilder(
            "SELECT DATE(s.sale_date) AS d, SUM(s.total_amount) AS total FROM sales s");
    List<Object> params = new ArrayList<>();
    appendSalesJoinAndWhere(sql, params, dateFrom, dateTo, categoryId);
    sql.append(" GROUP BY d ORDER BY total DESC LIMIT 1");
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql.toString())) {
      setParams(ps, params);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return rs.getString("d");
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to get best sales day", e);
    }
    return null;
  }

  public List<TrendPoint> getSalesTrend(
      String dateFrom, String dateTo, Long categoryId, String groupBy) {
    String dateBucket = getDateBucket(groupBy);
    StringBuilder sql =
        new StringBuilder(
            "SELECT " + dateBucket + " AS label, SUM(s.total_amount) AS value FROM sales s");
    List<Object> params = new ArrayList<>();
    appendSalesJoinAndWhere(sql, params, dateFrom, dateTo, categoryId);
    sql.append(" GROUP BY label ORDER BY label");
    return queryTrendPoints(sql.toString(), params);
  }

  public List<TopSellingProduct> getTopSellingProducts(String dateFrom, String dateTo, int limit) {
    String sql =
        "SELECT p.product_id, p.product_code, p.product_name, "
            + "SUM(si.quantity) AS qty_sold, SUM(si.line_total) AS sales_amt "
            + "FROM sale_items si "
            + "JOIN sales s ON si.sale_id = s.sale_id "
            + "JOIN products p ON si.product_id = p.product_id "
            + "WHERE s.status = 'Paid' AND s.sale_date BETWEEN ? AND ? "
            + "GROUP BY p.product_id, p.product_code, p.product_name "
            + "ORDER BY qty_sold DESC LIMIT ?";
    List<TopSellingProduct> results = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, dateFrom + " 00:00:00");
      ps.setString(2, dateTo + " 23:59:59");
      ps.setInt(3, limit);
      try (ResultSet rs = ps.executeQuery()) {
        int rank = 1;
        while (rs.next()) {
          results.add(
              new TopSellingProduct(
                  rank++,
                  rs.getLong("product_id"),
                  rs.getString("product_code"),
                  rs.getString("product_name"),
                  rs.getInt("qty_sold"),
                  rs.getDouble("sales_amt")));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to get top selling products", e);
    }
    return results;
  }

  // --- Inventory Value ---

  public double getTotalInventoryValue(Long categoryId) {
    StringBuilder sql =
        new StringBuilder(
            "SELECT COALESCE(SUM(p.unit_price * p.quantity), 0) FROM products p "
                + "WHERE p.status != 'Inactive'");
    List<Object> params = new ArrayList<>();
    if (categoryId != null) {
      sql.append(" AND p.category_id = ?");
      params.add(categoryId);
    }
    return queryDouble(sql.toString(), params);
  }

  public List<CategoryValueBreakdown> getInventoryValueByCategory(Long categoryId) {
    StringBuilder sql =
        new StringBuilder(
            "SELECT c.category_id, c.category_name, "
                + "COALESCE(SUM(p.unit_price * p.quantity), 0) AS inv_value "
                + "FROM categories c "
                + "LEFT JOIN products p ON c.category_id = p.category_id AND p.status != 'Inactive' "
                + "WHERE c.status = 'Active'");
    List<Object> params = new ArrayList<>();
    if (categoryId != null) {
      sql.append(" AND c.category_id = ?");
      params.add(categoryId);
    }
    sql.append(" GROUP BY c.category_id, c.category_name ORDER BY inv_value DESC");
    List<CategoryValueBreakdown> results = new ArrayList<>();
    double total = getTotalInventoryValue(categoryId);
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql.toString())) {
      setParams(ps, params);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          double value = rs.getDouble("inv_value");
          double pct = total > 0 ? (value / total) * 100.0 : 0;
          results.add(
              new CategoryValueBreakdown(
                  rs.getLong("category_id"),
                  rs.getString("category_name"),
                  value,
                  Math.round(pct * 100.0) / 100.0));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to get inventory value by category", e);
    }
    return results;
  }

  public List<InventoryValueProduct> getInventoryValueProducts(Long categoryId) {
    StringBuilder sql =
        new StringBuilder(
            "SELECT p.product_id, p.product_code, p.product_name, c.category_name, "
                + "p.quantity, p.unit_price, (p.unit_price * p.quantity) AS total_value "
                + "FROM products p "
                + "JOIN categories c ON p.category_id = c.category_id "
                + "WHERE p.status != 'Inactive'");
    List<Object> params = new ArrayList<>();
    if (categoryId != null) {
      sql.append(" AND p.category_id = ?");
      params.add(categoryId);
    }
    sql.append(" ORDER BY total_value DESC");
    List<InventoryValueProduct> results = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql.toString())) {
      setParams(ps, params);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          results.add(
              new InventoryValueProduct(
                  rs.getLong("product_id"),
                  rs.getString("product_code"),
                  rs.getString("product_name"),
                  rs.getString("category_name"),
                  rs.getInt("quantity"),
                  rs.getDouble("unit_price"),
                  rs.getDouble("total_value")));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to get inventory value products", e);
    }
    return results;
  }

  // --- Purchase Cost ---

  public double getPurchaseCostTotal(
      String dateFrom, String dateTo, Long supplierId, Long categoryId) {
    StringBuilder sql =
        new StringBuilder("SELECT COALESCE(SUM(po.total_amount), 0) FROM purchase_orders po");
    List<Object> params = new ArrayList<>();
    appendPurchaseJoinAndWhere(sql, params, dateFrom, dateTo, supplierId, categoryId);
    return queryDouble(sql.toString(), params);
  }

  public int getPurchaseOrderCount(
      String dateFrom, String dateTo, Long supplierId, Long categoryId) {
    StringBuilder sql =
        new StringBuilder("SELECT COUNT(DISTINCT po.purchase_order_id) FROM purchase_orders po");
    List<Object> params = new ArrayList<>();
    appendPurchaseJoinAndWhere(sql, params, dateFrom, dateTo, supplierId, categoryId);
    return (int) queryLong(sql.toString(), params);
  }

  public List<TrendPoint> getPurchaseCostTrend(
      String dateFrom, String dateTo, Long supplierId, Long categoryId) {
    StringBuilder sql =
        new StringBuilder(
            "SELECT DATE(po.order_date) AS label, SUM(po.total_amount) AS value "
                + "FROM purchase_orders po");
    List<Object> params = new ArrayList<>();
    appendPurchaseJoinAndWhere(sql, params, dateFrom, dateTo, supplierId, categoryId);
    sql.append(" GROUP BY label ORDER BY label");
    return queryTrendPoints(sql.toString(), params);
  }

  // --- Low Stock ---

  public int getLowStockCount(Long categoryId) {
    StringBuilder sql =
        new StringBuilder(
            "SELECT COUNT(*) FROM products p WHERE p.quantity <= p.reorder_level "
                + "AND p.status != 'Inactive'");
    List<Object> params = new ArrayList<>();
    if (categoryId != null) {
      sql.append(" AND p.category_id = ?");
      params.add(categoryId);
    }
    return (int) queryLong(sql.toString(), params);
  }

  public List<LowStockItem> getLowStockProducts(Long categoryId) {
    StringBuilder sql =
        new StringBuilder(
            "SELECT p.product_id, p.product_code, p.product_name, c.category_name, "
                + "p.quantity, p.reorder_level "
                + "FROM products p "
                + "JOIN categories c ON p.category_id = c.category_id "
                + "WHERE p.quantity <= p.reorder_level AND p.status != 'Inactive'");
    List<Object> params = new ArrayList<>();
    if (categoryId != null) {
      sql.append(" AND p.category_id = ?");
      params.add(categoryId);
    }
    sql.append(" ORDER BY (p.reorder_level - p.quantity) DESC");
    List<LowStockItem> results = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql.toString())) {
      setParams(ps, params);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          results.add(
              new LowStockItem(
                  rs.getLong("product_id"),
                  rs.getString("product_code"),
                  rs.getString("product_name"),
                  rs.getString("category_name"),
                  rs.getInt("quantity"),
                  rs.getInt("reorder_level")));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to get low stock products", e);
    }
    return results;
  }

  // --- Helpers ---

  private void appendSalesJoinAndWhere(
      StringBuilder sql, List<Object> params, String dateFrom, String dateTo, Long categoryId) {
    if (categoryId != null) {
      sql.append(
          " JOIN sale_items si ON s.sale_id = si.sale_id"
              + " JOIN products p ON si.product_id = p.product_id");
    }
    sql.append(" WHERE s.status = 'Paid'");
    sql.append(" AND s.sale_date BETWEEN ? AND ?");
    params.add(dateFrom + " 00:00:00");
    params.add(dateTo + " 23:59:59");
    if (categoryId != null) {
      sql.append(" AND p.category_id = ?");
      params.add(categoryId);
    }
  }

  private void appendPurchaseJoinAndWhere(
      StringBuilder sql,
      List<Object> params,
      String dateFrom,
      String dateTo,
      Long supplierId,
      Long categoryId) {
    if (categoryId != null) {
      sql.append(
          " JOIN purchase_order_items poi ON po.purchase_order_id = poi.purchase_order_id"
              + " JOIN products p ON poi.product_id = p.product_id");
    }
    sql.append(" WHERE po.status IN ('Delivered', 'Received')");
    sql.append(" AND po.order_date BETWEEN ? AND ?");
    params.add(dateFrom);
    params.add(dateTo);
    if (supplierId != null) {
      sql.append(" AND po.supplier_id = ?");
      params.add(supplierId);
    }
    if (categoryId != null) {
      sql.append(" AND p.category_id = ?");
      params.add(categoryId);
    }
  }

  private String getDateBucket(String groupBy) {
    if ("WEEK".equalsIgnoreCase(groupBy)) {
      return "DATE_FORMAT(s.sale_date, '%x-W%v')";
    } else if ("MONTH".equalsIgnoreCase(groupBy)) {
      return "DATE_FORMAT(s.sale_date, '%Y-%m')";
    }
    return "DATE(s.sale_date)";
  }

  private List<TrendPoint> queryTrendPoints(String sql, List<Object> params) {
    List<TrendPoint> results = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      setParams(ps, params);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          results.add(new TrendPoint(rs.getString("label"), rs.getDouble("value")));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to query trend points", e);
    }
    return results;
  }

  private double queryDouble(String sql, List<Object> params) {
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      setParams(ps, params);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return rs.getDouble(1);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to query double", e);
    }
    return 0;
  }

  private long queryLong(String sql, List<Object> params) {
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      setParams(ps, params);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return rs.getLong(1);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to query long", e);
    }
    return 0;
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
