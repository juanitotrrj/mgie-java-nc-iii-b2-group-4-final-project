package com.group4.inventoryserver.repository;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.dto.sale.SaleData;
import com.group4.inventoryserver.dto.sale.SaleItemData;
import com.group4.inventoryserver.util.DateUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaleRepository {

  private static final Map<String, String> SORT_COLUMN_MAP = new HashMap<>();

  static {
    SORT_COLUMN_MAP.put("saleId", "s.sale_id");
    SORT_COLUMN_MAP.put("invoiceNo", "s.invoice_no");
    SORT_COLUMN_MAP.put("customerName", "s.customer_name");
    SORT_COLUMN_MAP.put("saleDate", "s.sale_date");
    SORT_COLUMN_MAP.put("totalAmount", "s.total_amount");
    SORT_COLUMN_MAP.put("status", "s.status");
    SORT_COLUMN_MAP.put("paymentMethod", "s.payment_method");
    SORT_COLUMN_MAP.put("createdAt", "s.created_at");
  }

  private static final String LIST_SELECT =
      "SELECT s.sale_id, s.invoice_no, s.customer_name, s.cashier_id, "
          + "u.full_name AS cashier_name, s.sale_date, s.payment_method, s.status, "
          + "s.subtotal, s.tax_amount, s.total_amount, s.amount_received, s.change_amount, "
          + "s.cancel_reason, s.created_at, s.updated_at, "
          + "(SELECT COUNT(*) FROM sale_items si WHERE si.sale_id = s.sale_id) AS item_count "
          + "FROM sales s "
          + "LEFT JOIN users u ON s.cashier_id = u.user_id";

  public List<SaleData> findAll(
      int offset,
      int limit,
      String sortBy,
      String sortDir,
      String search,
      String status,
      String paymentMethod,
      String dateFrom,
      String dateTo,
      Long cashierId) {
    StringBuilder sql = new StringBuilder(LIST_SELECT);
    List<Object> params = new ArrayList<>();
    appendWhereClause(sql, params, search, status, paymentMethod, dateFrom, dateTo, cashierId);

    String column = SORT_COLUMN_MAP.getOrDefault(sortBy, "s.created_at");
    String direction = "desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
    sql.append(" ORDER BY ").append(column).append(' ').append(direction);
    sql.append(" LIMIT ? OFFSET ?");
    params.add(limit);
    params.add(offset);

    List<SaleData> results = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql.toString())) {
      setParams(ps, params);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          results.add(mapRow(rs));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to list sales", e);
    }
    return results;
  }

  public long count(
      String search,
      String status,
      String paymentMethod,
      String dateFrom,
      String dateTo,
      Long cashierId) {
    StringBuilder sql =
        new StringBuilder(
            "SELECT COUNT(*) FROM sales s LEFT JOIN users u ON s.cashier_id = u.user_id");
    List<Object> params = new ArrayList<>();
    appendWhereClause(sql, params, search, status, paymentMethod, dateFrom, dateTo, cashierId);

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql.toString())) {
      setParams(ps, params);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return rs.getLong(1);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to count sales", e);
    }
    return 0;
  }

  public SaleData findById(long saleId) {
    String sql = LIST_SELECT + " WHERE s.sale_id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, saleId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return mapRow(rs);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to find sale by id", e);
    }
    return null;
  }

  public List<SaleItemData> findItemsBySaleId(long saleId) {
    String sql =
        "SELECT si.sale_item_id, si.product_id, p.product_code, p.product_name, "
            + "si.quantity, si.unit_price, si.line_total "
            + "FROM sale_items si "
            + "JOIN products p ON si.product_id = p.product_id "
            + "WHERE si.sale_id = ? ORDER BY si.sale_item_id";
    List<SaleItemData> results = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, saleId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          results.add(mapItemRow(rs));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to find sale items", e);
    }
    return results;
  }

  public List<SaleItemData> findItemsBySaleId(Connection conn, long saleId) throws SQLException {
    String sql =
        "SELECT si.sale_item_id, si.product_id, p.product_code, p.product_name, "
            + "si.quantity, si.unit_price, si.line_total "
            + "FROM sale_items si "
            + "JOIN products p ON si.product_id = p.product_id "
            + "WHERE si.sale_id = ? ORDER BY si.sale_item_id";
    List<SaleItemData> results = new ArrayList<>();
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, saleId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          results.add(mapItemRow(rs));
        }
      }
    }
    return results;
  }

  public long insert(
      Connection conn,
      String invoiceNo,
      String customerName,
      Long cashierId,
      String paymentMethod,
      String status,
      double subtotal,
      double taxAmount,
      double totalAmount,
      double amountReceived,
      double changeAmount,
      Long createdBy)
      throws SQLException {
    String sql =
        "INSERT INTO sales (invoice_no, customer_name, cashier_id, payment_method, status, "
            + "subtotal, tax_amount, total_amount, amount_received, change_amount, "
            + "created_by, updated_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, invoiceNo);
      ps.setString(2, customerName);
      setNullableLong(ps, 3, cashierId);
      ps.setString(4, paymentMethod);
      ps.setString(5, status);
      ps.setDouble(6, subtotal);
      ps.setDouble(7, taxAmount);
      ps.setDouble(8, totalAmount);
      ps.setDouble(9, amountReceived);
      ps.setDouble(10, changeAmount);
      setNullableLong(ps, 11, createdBy);
      setNullableLong(ps, 12, createdBy);
      ps.executeUpdate();
      try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next()) return keys.getLong(1);
      }
    }
    return -1;
  }

  public void insertItem(
      Connection conn,
      long saleId,
      long productId,
      int quantity,
      double unitPrice,
      double lineTotal)
      throws SQLException {
    String sql =
        "INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, line_total) "
            + "VALUES (?, ?, ?, ?, ?)";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, saleId);
      ps.setLong(2, productId);
      ps.setInt(3, quantity);
      ps.setDouble(4, unitPrice);
      ps.setDouble(5, lineTotal);
      ps.executeUpdate();
    }
  }

  public void insertItem(
      long saleId, long productId, int quantity, double unitPrice, double lineTotal) {
    String sql =
        "INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, line_total) "
            + "VALUES (?, ?, ?, ?, ?)";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, saleId);
      ps.setLong(2, productId);
      ps.setInt(3, quantity);
      ps.setDouble(4, unitPrice);
      ps.setDouble(5, lineTotal);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to insert sale item", e);
    }
  }

  public void deleteItemsBySaleId(long saleId) {
    String sql = "DELETE FROM sale_items WHERE sale_id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, saleId);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to delete sale items", e);
    }
  }

  public void updateHeader(
      long saleId,
      String customerName,
      String paymentMethod,
      double subtotal,
      double taxAmount,
      double totalAmount,
      double amountReceived,
      double changeAmount,
      Long updatedBy) {
    String sql =
        "UPDATE sales SET customer_name = ?, payment_method = ?, subtotal = ?, tax_amount = ?, "
            + "total_amount = ?, amount_received = ?, change_amount = ?, updated_by = ? "
            + "WHERE sale_id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, customerName);
      ps.setString(2, paymentMethod);
      ps.setDouble(3, subtotal);
      ps.setDouble(4, taxAmount);
      ps.setDouble(5, totalAmount);
      ps.setDouble(6, amountReceived);
      ps.setDouble(7, changeAmount);
      setNullableLong(ps, 8, updatedBy);
      ps.setLong(9, saleId);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to update sale header", e);
    }
  }

  public void cancel(Connection conn, long saleId, String reason, Long cancelledBy)
      throws SQLException {
    String sql =
        "UPDATE sales SET status = 'Cancelled', cancel_reason = ?, cancelled_by = ?, "
            + "updated_by = ? WHERE sale_id = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, reason);
      setNullableLong(ps, 2, cancelledBy);
      setNullableLong(ps, 3, cancelledBy);
      ps.setLong(4, saleId);
      ps.executeUpdate();
    }
  }

  public String generateNextInvoiceNo() {
    String year = String.valueOf(LocalDate.now().getYear());
    String prefix = "INV-" + year + "-";
    String sql =
        "SELECT invoice_no FROM sales WHERE invoice_no LIKE ? " + "ORDER BY sale_id DESC LIMIT 1";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, prefix + "%");
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          String last = rs.getString("invoice_no");
          String numPart = last.substring(prefix.length());
          int num = Integer.parseInt(numPart);
          return prefix + String.format("%05d", num + 1);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to generate next invoice number", e);
    }
    return prefix + "00001";
  }

  public int getProductQuantity(Connection conn, long productId) throws SQLException {
    String sql = "SELECT quantity FROM products WHERE product_id = ? FOR UPDATE";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, productId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return rs.getInt("quantity");
      }
    }
    return 0;
  }

  public void updateProductQuantity(
      Connection conn, long productId, int newQuantity, int reorderLevel) throws SQLException {
    String status;
    if (newQuantity <= 0) {
      status = "Out of Stock";
    } else if (newQuantity <= reorderLevel) {
      status = "Low Stock";
    } else {
      status = "In Stock";
    }
    String sql = "UPDATE products SET quantity = ?, status = ? WHERE product_id = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, newQuantity);
      ps.setString(2, status);
      ps.setLong(3, productId);
      ps.executeUpdate();
    }
  }

  public int getProductReorderLevel(Connection conn, long productId) throws SQLException {
    String sql = "SELECT reorder_level FROM products WHERE product_id = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, productId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return rs.getInt("reorder_level");
      }
    }
    return 0;
  }

  public double getProductUnitPrice(long productId) {
    String sql = "SELECT unit_price FROM products WHERE product_id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, productId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return rs.getDouble("unit_price");
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to get product unit price", e);
    }
    return 0;
  }

  public boolean productExistsAndActive(long productId) {
    String sql = "SELECT 1 FROM products WHERE product_id = ? AND status != 'Inactive'";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, productId);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to check product", e);
    }
  }

  private void appendWhereClause(
      StringBuilder sql,
      List<Object> params,
      String search,
      String status,
      String paymentMethod,
      String dateFrom,
      String dateTo,
      Long cashierId) {
    List<String> conditions = new ArrayList<>();
    if (search != null && !search.trim().isEmpty()) {
      conditions.add("(s.invoice_no LIKE ? OR s.customer_name LIKE ?)");
      String pattern = "%" + search.trim() + "%";
      params.add(pattern);
      params.add(pattern);
    }
    if (status != null && !status.trim().isEmpty()) {
      conditions.add("s.status = ?");
      params.add(status);
    }
    if (paymentMethod != null && !paymentMethod.trim().isEmpty()) {
      conditions.add("s.payment_method = ?");
      params.add(paymentMethod);
    }
    if (dateFrom != null && !dateFrom.trim().isEmpty()) {
      conditions.add("DATE(s.sale_date) >= ?");
      params.add(dateFrom);
    }
    if (dateTo != null && !dateTo.trim().isEmpty()) {
      conditions.add("DATE(s.sale_date) <= ?");
      params.add(dateTo);
    }
    if (cashierId != null) {
      conditions.add("s.cashier_id = ?");
      params.add(cashierId);
    }
    if (!conditions.isEmpty()) {
      sql.append(" WHERE ").append(join(conditions, " AND "));
    }
  }

  private SaleData mapRow(ResultSet rs) throws SQLException {
    Timestamp createdAt = rs.getTimestamp("created_at");
    Timestamp updatedAt = rs.getTimestamp("updated_at");
    Timestamp saleDate = rs.getTimestamp("sale_date");
    long cashierId = rs.getLong("cashier_id");
    return new SaleData(
        rs.getLong("sale_id"),
        rs.getString("invoice_no"),
        rs.getString("customer_name"),
        rs.wasNull() ? null : cashierId,
        rs.getString("cashier_name"),
        saleDate != null ? DateUtil.formatIso(saleDate) : null,
        rs.getString("payment_method"),
        rs.getString("status"),
        rs.getInt("item_count"),
        rs.getDouble("subtotal"),
        rs.getDouble("tax_amount"),
        rs.getDouble("total_amount"),
        rs.getDouble("amount_received"),
        rs.getDouble("change_amount"),
        rs.getString("cancel_reason"),
        createdAt != null ? DateUtil.formatIso(createdAt) : null,
        updatedAt != null ? DateUtil.formatIso(updatedAt) : null);
  }

  private SaleItemData mapItemRow(ResultSet rs) throws SQLException {
    return new SaleItemData(
        rs.getLong("sale_item_id"),
        rs.getLong("product_id"),
        rs.getString("product_code"),
        rs.getString("product_name"),
        rs.getInt("quantity"),
        rs.getDouble("unit_price"),
        rs.getDouble("line_total"));
  }

  private void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
    if (value != null) {
      ps.setLong(index, value);
    } else {
      ps.setNull(index, Types.BIGINT);
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
