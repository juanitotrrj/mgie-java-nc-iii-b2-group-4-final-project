package com.group4.inventoryserver.repository;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.dto.purchase.PurchaseData;
import com.group4.inventoryserver.dto.purchase.PurchaseItemData;
import com.group4.inventoryserver.util.DateUtil;
import java.sql.Connection;
import java.sql.Date;
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

public class PurchaseRepository {

  private static final Map<String, String> SORT_COLUMN_MAP = new HashMap<>();

  static {
    SORT_COLUMN_MAP.put("purchaseId", "po.purchase_order_id");
    SORT_COLUMN_MAP.put("poNumber", "po.po_number");
    SORT_COLUMN_MAP.put("supplierName", "s.supplier_name");
    SORT_COLUMN_MAP.put("orderDate", "po.order_date");
    SORT_COLUMN_MAP.put("totalAmount", "po.total_amount");
    SORT_COLUMN_MAP.put("status", "po.status");
    SORT_COLUMN_MAP.put("createdAt", "po.created_at");
  }

  private static final String LIST_SELECT =
      "SELECT po.purchase_order_id, po.po_number, po.supplier_id, s.supplier_name, "
          + "po.order_date, po.expected_delivery_date, po.received_date, "
          + "po.total_amount, po.status, po.notes, po.cancel_reason, "
          + "po.created_at, po.updated_at, "
          + "(SELECT COUNT(*) FROM purchase_order_items i WHERE i.purchase_order_id = po.purchase_order_id) AS item_count "
          + "FROM purchase_orders po "
          + "JOIN suppliers s ON po.supplier_id = s.supplier_id";

  public List<PurchaseData> findAll(
      int offset,
      int limit,
      String sortBy,
      String sortDir,
      String search,
      Long supplierId,
      String status,
      String dateFrom,
      String dateTo) {
    StringBuilder sql = new StringBuilder(LIST_SELECT);
    List<Object> params = new ArrayList<>();
    appendWhereClause(sql, params, search, supplierId, status, dateFrom, dateTo);

    String column = SORT_COLUMN_MAP.getOrDefault(sortBy, "po.created_at");
    String direction = "desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
    sql.append(" ORDER BY ").append(column).append(' ').append(direction);
    sql.append(" LIMIT ? OFFSET ?");
    params.add(limit);
    params.add(offset);

    List<PurchaseData> results = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql.toString())) {
      setParams(ps, params);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          results.add(mapRow(rs));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to list purchase orders", e);
    }
    return results;
  }

  public long count(String search, Long supplierId, String status, String dateFrom, String dateTo) {
    StringBuilder sql =
        new StringBuilder(
            "SELECT COUNT(*) FROM purchase_orders po JOIN suppliers s ON po.supplier_id = s.supplier_id");
    List<Object> params = new ArrayList<>();
    appendWhereClause(sql, params, search, supplierId, status, dateFrom, dateTo);

    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql.toString())) {
      setParams(ps, params);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return rs.getLong(1);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to count purchase orders", e);
    }
    return 0;
  }

  public PurchaseData findById(long purchaseOrderId) {
    String sql = LIST_SELECT + " WHERE po.purchase_order_id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, purchaseOrderId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return mapRow(rs);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to find purchase order by id", e);
    }
    return null;
  }

  public List<PurchaseItemData> findItemsByPurchaseId(long purchaseOrderId) {
    String sql =
        "SELECT i.purchase_order_item_id, i.product_id, p.product_code, p.product_name, "
            + "i.quantity, i.unit_cost, i.line_total "
            + "FROM purchase_order_items i "
            + "JOIN products p ON i.product_id = p.product_id "
            + "WHERE i.purchase_order_id = ? ORDER BY i.purchase_order_item_id";
    List<PurchaseItemData> results = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, purchaseOrderId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          results.add(
              new PurchaseItemData(
                  rs.getLong("purchase_order_item_id"),
                  rs.getLong("product_id"),
                  rs.getString("product_code"),
                  rs.getString("product_name"),
                  rs.getInt("quantity"),
                  rs.getDouble("unit_cost"),
                  rs.getDouble("line_total")));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to find purchase order items", e);
    }
    return results;
  }

  public long insert(
      String poNumber,
      long supplierId,
      String orderDate,
      String expectedDeliveryDate,
      String notes,
      double totalAmount,
      Long createdBy) {
    String sql =
        "INSERT INTO purchase_orders (po_number, supplier_id, order_date, expected_delivery_date, "
            + "notes, total_amount, created_by, updated_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, poNumber);
      ps.setLong(2, supplierId);
      ps.setDate(3, Date.valueOf(orderDate));
      if (expectedDeliveryDate != null) {
        ps.setDate(4, Date.valueOf(expectedDeliveryDate));
      } else {
        ps.setNull(4, Types.DATE);
      }
      setNullableString(ps, 5, notes);
      ps.setDouble(6, totalAmount);
      if (createdBy != null) {
        ps.setLong(7, createdBy);
        ps.setLong(8, createdBy);
      } else {
        ps.setNull(7, Types.BIGINT);
        ps.setNull(8, Types.BIGINT);
      }
      ps.executeUpdate();
      try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next()) return keys.getLong(1);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to insert purchase order", e);
    }
    return -1;
  }

  public void insertItem(
      long purchaseOrderId, long productId, int quantity, double unitCost, double lineTotal) {
    String sql =
        "INSERT INTO purchase_order_items (purchase_order_id, product_id, quantity, unit_cost, "
            + "line_total) VALUES (?, ?, ?, ?, ?)";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, purchaseOrderId);
      ps.setLong(2, productId);
      ps.setInt(3, quantity);
      ps.setDouble(4, unitCost);
      ps.setDouble(5, lineTotal);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to insert purchase order item", e);
    }
  }

  public void deleteItemsByPurchaseId(long purchaseOrderId) {
    String sql = "DELETE FROM purchase_order_items WHERE purchase_order_id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, purchaseOrderId);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to delete purchase order items", e);
    }
  }

  public void updateHeader(
      long purchaseOrderId,
      long supplierId,
      String orderDate,
      String expectedDeliveryDate,
      String notes,
      double totalAmount,
      Long updatedBy) {
    String sql =
        "UPDATE purchase_orders SET supplier_id = ?, order_date = ?, expected_delivery_date = ?, "
            + "notes = ?, total_amount = ?, updated_by = ? WHERE purchase_order_id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, supplierId);
      ps.setDate(2, Date.valueOf(orderDate));
      if (expectedDeliveryDate != null) {
        ps.setDate(3, Date.valueOf(expectedDeliveryDate));
      } else {
        ps.setNull(3, Types.DATE);
      }
      setNullableString(ps, 4, notes);
      ps.setDouble(5, totalAmount);
      if (updatedBy != null) {
        ps.setLong(6, updatedBy);
      } else {
        ps.setNull(6, Types.BIGINT);
      }
      ps.setLong(7, purchaseOrderId);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to update purchase order header", e);
    }
  }

  public void receive(Connection conn, long purchaseOrderId, String receivedDate, Long receivedBy)
      throws SQLException {
    String sql =
        "UPDATE purchase_orders SET status = 'Received', received_date = ?, received_by = ?, "
            + "updated_by = ? WHERE purchase_order_id = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      if (receivedDate != null) {
        ps.setDate(1, Date.valueOf(receivedDate));
      } else {
        ps.setDate(1, Date.valueOf(LocalDate.now()));
      }
      if (receivedBy != null) {
        ps.setLong(2, receivedBy);
        ps.setLong(3, receivedBy);
      } else {
        ps.setNull(2, Types.BIGINT);
        ps.setNull(3, Types.BIGINT);
      }
      ps.setLong(4, purchaseOrderId);
      ps.executeUpdate();
    }
  }

  public void cancel(long purchaseOrderId, String reason, Long cancelledBy) {
    String sql =
        "UPDATE purchase_orders SET status = 'Cancelled', cancel_reason = ?, cancelled_by = ?, "
            + "updated_by = ? WHERE purchase_order_id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, reason);
      if (cancelledBy != null) {
        ps.setLong(2, cancelledBy);
        ps.setLong(3, cancelledBy);
      } else {
        ps.setNull(2, Types.BIGINT);
        ps.setNull(3, Types.BIGINT);
      }
      ps.setLong(4, purchaseOrderId);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to cancel purchase order", e);
    }
  }

  public String generateNextPoNumber() {
    String year = String.valueOf(LocalDate.now().getYear());
    String prefix = "PO-" + year + "-";
    String sql =
        "SELECT po_number FROM purchase_orders WHERE po_number LIKE ? "
            + "ORDER BY purchase_order_id DESC LIMIT 1";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, prefix + "%");
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          String last = rs.getString("po_number");
          String numPart = last.substring(prefix.length());
          int num = Integer.parseInt(numPart);
          return prefix + String.format("%05d", num + 1);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to generate next PO number", e);
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

  public List<PurchaseItemData> findItemsByPurchaseId(Connection conn, long purchaseOrderId)
      throws SQLException {
    String sql =
        "SELECT i.purchase_order_item_id, i.product_id, p.product_code, p.product_name, "
            + "i.quantity, i.unit_cost, i.line_total "
            + "FROM purchase_order_items i "
            + "JOIN products p ON i.product_id = p.product_id "
            + "WHERE i.purchase_order_id = ? ORDER BY i.purchase_order_item_id";
    List<PurchaseItemData> results = new ArrayList<>();
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, purchaseOrderId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          results.add(
              new PurchaseItemData(
                  rs.getLong("purchase_order_item_id"),
                  rs.getLong("product_id"),
                  rs.getString("product_code"),
                  rs.getString("product_name"),
                  rs.getInt("quantity"),
                  rs.getDouble("unit_cost"),
                  rs.getDouble("line_total")));
        }
      }
    }
    return results;
  }

  public boolean productExists(long productId) {
    String sql = "SELECT 1 FROM products WHERE product_id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setLong(1, productId);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to check product existence", e);
    }
  }

  public boolean supplierExistsAndActive(long supplierId) {
    String sql = "SELECT 1 FROM suppliers WHERE supplier_id = ? AND status = 'Active'";
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

  private void appendWhereClause(
      StringBuilder sql,
      List<Object> params,
      String search,
      Long supplierId,
      String status,
      String dateFrom,
      String dateTo) {
    List<String> conditions = new ArrayList<>();
    if (search != null && !search.trim().isEmpty()) {
      conditions.add("(po.po_number LIKE ? OR s.supplier_name LIKE ?)");
      String pattern = "%" + search.trim() + "%";
      params.add(pattern);
      params.add(pattern);
    }
    if (supplierId != null) {
      conditions.add("po.supplier_id = ?");
      params.add(supplierId);
    }
    if (status != null && !status.trim().isEmpty()) {
      conditions.add("po.status = ?");
      params.add(status);
    }
    if (dateFrom != null && !dateFrom.trim().isEmpty()) {
      conditions.add("po.order_date >= ?");
      params.add(dateFrom);
    }
    if (dateTo != null && !dateTo.trim().isEmpty()) {
      conditions.add("po.order_date <= ?");
      params.add(dateTo);
    }
    if (!conditions.isEmpty()) {
      sql.append(" WHERE ").append(join(conditions, " AND "));
    }
  }

  private PurchaseData mapRow(ResultSet rs) throws SQLException {
    Timestamp createdAt = rs.getTimestamp("created_at");
    Timestamp updatedAt = rs.getTimestamp("updated_at");
    Date orderDate = rs.getDate("order_date");
    Date expectedDate = rs.getDate("expected_delivery_date");
    Date receivedDate = rs.getDate("received_date");
    return new PurchaseData(
        rs.getLong("purchase_order_id"),
        rs.getString("po_number"),
        rs.getLong("supplier_id"),
        rs.getString("supplier_name"),
        orderDate != null ? orderDate.toString() : null,
        expectedDate != null ? expectedDate.toString() : null,
        receivedDate != null ? receivedDate.toString() : null,
        rs.getInt("item_count"),
        rs.getDouble("total_amount"),
        rs.getString("status"),
        rs.getString("notes"),
        rs.getString("cancel_reason"),
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
