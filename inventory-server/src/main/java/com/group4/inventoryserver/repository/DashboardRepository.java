package com.group4.inventoryserver.repository;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.dto.report.TopSellingProduct;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardRepository {

  public int countActiveProducts() {
    return countSingle("SELECT COUNT(*) FROM products WHERE status != 'Inactive'");
  }

  public int countActiveUsers() {
    return countSingle("SELECT COUNT(*) FROM users WHERE status = 'Active'");
  }

  public double getTodaySalesTotal() {
    String sql =
        "SELECT COALESCE(SUM(total_amount), 0) FROM sales "
            + "WHERE DATE(created_at) = CURDATE() AND status != 'Cancelled'";
    return sumSingle(sql);
  }

  public int getTodaySalesCount() {
    String sql =
        "SELECT COUNT(*) FROM sales "
            + "WHERE DATE(created_at) = CURDATE() AND status != 'Cancelled'";
    return countSingle(sql);
  }

  public int countLowStockProducts() {
    String sql =
        "SELECT COUNT(*) FROM products "
            + "WHERE status != 'Inactive' AND quantity <= reorder_level AND reorder_level > 0";
    return countSingle(sql);
  }

  public int countPendingApprovals() {
    return countSingle("SELECT COUNT(*) FROM inventory_change_requests WHERE status = 'Pending'");
  }

  public double getMonthlyRevenue() {
    String sql =
        "SELECT COALESCE(SUM(total_amount), 0) FROM sales "
            + "WHERE YEAR(created_at) = YEAR(CURDATE()) AND MONTH(created_at) = MONTH(CURDATE()) "
            + "AND status != 'Cancelled'";
    return sumSingle(sql);
  }

  public int countMonthlyPurchaseOrders() {
    String sql =
        "SELECT COUNT(*) FROM purchase_orders "
            + "WHERE YEAR(created_at) = YEAR(CURDATE()) AND MONTH(created_at) = MONTH(CURDATE())";
    return countSingle(sql);
  }

  public int countPendingRequestsByUser(long userId) {
    String sql =
        "SELECT COUNT(*) FROM inventory_change_requests "
            + "WHERE requested_by = ? AND status = 'Pending'";
    return countWithParam(sql, userId);
  }

  public int countApprovedRequestsByUser(long userId) {
    String sql =
        "SELECT COUNT(*) FROM inventory_change_requests "
            + "WHERE requested_by = ? AND status = 'Approved'";
    return countWithParam(sql, userId);
  }

  public int countRejectedRequestsByUser(long userId) {
    String sql =
        "SELECT COUNT(*) FROM inventory_change_requests "
            + "WHERE requested_by = ? AND status = 'Rejected'";
    return countWithParam(sql, userId);
  }

  public double getAverageSaleToday() {
    String sql =
        "SELECT COALESCE(AVG(total_amount), 0) FROM sales "
            + "WHERE DATE(created_at) = CURDATE() AND status != 'Cancelled'";
    return sumSingle(sql);
  }

  public List<Map<String, Object>> getRecentAuditLogs(int limit) {
    String sql =
        "SELECT audit_log_id, user_id, username, action, module, reference_no, "
            + "details, created_at FROM audit_logs ORDER BY created_at DESC LIMIT ?";
    List<Map<String, Object>> results = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, limit);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          Map<String, Object> row = new LinkedHashMap<>();
          row.put("auditLogId", rs.getLong("audit_log_id"));
          row.put("userId", rs.getObject("user_id"));
          row.put("username", rs.getString("username"));
          row.put("action", rs.getString("action"));
          row.put("module", rs.getString("module"));
          row.put("referenceNo", rs.getString("reference_no"));
          row.put("details", rs.getString("details"));
          row.put("dateTime", rs.getString("created_at"));
          results.add(row);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to get recent audit logs", e);
    }
    return results;
  }

  public List<Map<String, Object>> getPendingIcrs(int limit) {
    String sql =
        "SELECT icr.inventory_change_request_id, icr.request_no, icr.request_type, "
            + "icr.reason, icr.created_at, u.username AS requested_by_name "
            + "FROM inventory_change_requests icr "
            + "LEFT JOIN users u ON icr.requested_by = u.user_id "
            + "WHERE icr.status = 'Pending' ORDER BY icr.created_at DESC LIMIT ?";
    List<Map<String, Object>> results = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, limit);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          Map<String, Object> row = new LinkedHashMap<>();
          row.put("requestId", rs.getLong("inventory_change_request_id"));
          row.put("requestNo", rs.getString("request_no"));
          row.put("changeType", rs.getString("request_type"));
          row.put("reason", rs.getString("reason"));
          row.put("requestedByName", rs.getString("requested_by_name"));
          row.put("createdAt", rs.getString("created_at"));
          results.add(row);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to get pending ICRs", e);
    }
    return results;
  }

  public List<Map<String, Object>> getRecentStockMovements(int limit) {
    String sql =
        "SELECT sm.stock_movement_id, sm.movement_type, sm.quantity_change, "
            + "sm.created_at, p.product_code, p.product_name "
            + "FROM stock_movements sm JOIN products p ON sm.product_id = p.product_id "
            + "ORDER BY sm.created_at DESC LIMIT ?";
    List<Map<String, Object>> results = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, limit);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          Map<String, Object> row = new LinkedHashMap<>();
          row.put("stockMovementId", rs.getLong("stock_movement_id"));
          row.put("productCode", rs.getString("product_code"));
          row.put("productName", rs.getString("product_name"));
          row.put("movementType", rs.getString("movement_type"));
          row.put("quantityChange", rs.getInt("quantity_change"));
          row.put("createdAt", rs.getString("created_at"));
          results.add(row);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to get recent stock movements", e);
    }
    return results;
  }

  public List<TopSellingProduct> getTopSellingProducts(int limit) {
    String sql =
        "SELECT p.product_id, p.product_code, p.product_name, "
            + "SUM(si.quantity) AS qty_sold, SUM(si.line_total) AS sales_amt "
            + "FROM sale_items si JOIN products p ON si.product_id = p.product_id "
            + "JOIN sales s ON si.sale_id = s.sale_id "
            + "WHERE s.status != 'Cancelled' "
            + "GROUP BY p.product_id, p.product_code, p.product_name "
            + "ORDER BY qty_sold DESC LIMIT ?";
    List<TopSellingProduct> results = new ArrayList<>();
    int rank = 0;
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, limit);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          rank++;
          results.add(
              new TopSellingProduct(
                  rank,
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

  public List<Map<String, Object>> getRecentSales(int limit) {
    String sql =
        "SELECT s.sale_id, s.invoice_no, s.total_amount, s.payment_method, "
            + "s.status, s.created_at, u.username AS cashier_name "
            + "FROM sales s LEFT JOIN users u ON s.created_by = u.user_id "
            + "ORDER BY s.created_at DESC LIMIT ?";
    List<Map<String, Object>> results = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, limit);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          Map<String, Object> row = new LinkedHashMap<>();
          row.put("saleId", rs.getLong("sale_id"));
          row.put("invoiceNo", rs.getString("invoice_no"));
          row.put("totalAmount", rs.getDouble("total_amount"));
          row.put("paymentMethod", rs.getString("payment_method"));
          row.put("status", rs.getString("status"));
          row.put("cashierName", rs.getString("cashier_name"));
          row.put("createdAt", rs.getString("created_at"));
          results.add(row);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to get recent sales", e);
    }
    return results;
  }

  private int countSingle(String sql) {
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
      return rs.next() ? rs.getInt(1) : 0;
    } catch (SQLException e) {
      throw new RuntimeException("Dashboard count query failed", e);
    }
  }

  private double sumSingle(String sql) {
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
      return rs.next() ? rs.getDouble(1) : 0.0;
    } catch (SQLException e) {
      throw new RuntimeException("Dashboard sum query failed", e);
    }
  }

  private int countWithParam(String sql, long param) {
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, param);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? rs.getInt(1) : 0;
      }
    } catch (SQLException e) {
      throw new RuntimeException("Dashboard count query failed", e);
    }
  }
}
