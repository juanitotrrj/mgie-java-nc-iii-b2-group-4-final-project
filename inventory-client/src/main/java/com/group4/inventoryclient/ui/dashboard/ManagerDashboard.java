package com.group4.inventoryclient.ui.dashboard;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.group4.inventoryclient.api.DashboardApiClient;
import com.group4.inventoryclient.ui.components.MetricCardPanel;
import com.group4.inventoryclient.util.SwingUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class ManagerDashboard extends JPanel {

  private final DashboardApiClient dashboardApi;
  private final MetricCardPanel pendingIcrsCard;
  private final MetricCardPanel lowStockCard;
  private final MetricCardPanel weeklySalesCard;
  private final MetricCardPanel activeSuppliersCard;
  private final DefaultTableModel pendingIcrsTableModel;
  private final DefaultTableModel topProductsTableModel;

  public ManagerDashboard(DashboardApiClient dashboardApi) {
    super(new BorderLayout(10, 10));
    this.dashboardApi = dashboardApi;
    setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    JPanel metricsPanel = new JPanel(new GridLayout(1, 4, 10, 10));
    pendingIcrsCard =
        new MetricCardPanel("Pending ICRs", "0", "Awaiting approval", new Color(239, 68, 68));
    lowStockCard =
        new MetricCardPanel("Low Stock Items", "0", "Need restocking", new Color(245, 158, 11));
    weeklySalesCard =
        new MetricCardPanel("Weekly Sales", "$0.00", "Last 7 days", new Color(16, 185, 129));
    activeSuppliersCard =
        new MetricCardPanel("Active Suppliers", "0", "Currently active", new Color(59, 130, 246));

    metricsPanel.add(pendingIcrsCard);
    metricsPanel.add(lowStockCard);
    metricsPanel.add(weeklySalesCard);
    metricsPanel.add(activeSuppliersCard);
    add(metricsPanel, BorderLayout.NORTH);

    JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 10, 10));

    pendingIcrsTableModel =
        new DefaultTableModel(new String[] {"ICR#", "Product", "Qty", "Clerk"}, 0) {
          @Override
          public boolean isCellEditable(int row, int column) {
            return false;
          }
        };
    JTable pendingIcrsTable = new JTable(pendingIcrsTableModel);
    JScrollPane pendingScrollPane = new JScrollPane(pendingIcrsTable);
    pendingScrollPane.setBorder(BorderFactory.createTitledBorder("Pending ICRs Needing Approval"));
    tablesPanel.add(pendingScrollPane);

    topProductsTableModel =
        new DefaultTableModel(new String[] {"Product", "Sales", "Revenue"}, 0) {
          @Override
          public boolean isCellEditable(int row, int column) {
            return false;
          }
        };
    JTable topProductsTable = new JTable(topProductsTableModel);
    JScrollPane topScrollPane = new JScrollPane(topProductsTable);
    topScrollPane.setBorder(BorderFactory.createTitledBorder("Top Selling Products"));
    tablesPanel.add(topScrollPane);

    add(tablesPanel, BorderLayout.CENTER);
    loadDashboardData();
  }

  private void loadDashboardData() {
    new Thread(
            () -> {
              try {
                JsonObject data = dashboardApi.getManagerDashboard();
                SwingUtilities.invokeLater(() -> updateDashboard(data));
              } catch (Exception e) {
                SwingUtil.showError(this, "Failed to load dashboard: " + e.getMessage());
              }
            })
        .start();
  }

  private void updateDashboard(JsonObject data) {
    if (data.has("pendingIcrs")) {
      pendingIcrsCard.updateValue(data.get("pendingIcrs").getAsString());
    }
    if (data.has("lowStockItems")) {
      lowStockCard.updateValue(data.get("lowStockItems").getAsString());
    }
    if (data.has("weeklySales")) {
      weeklySalesCard.updateValue("$" + data.get("weeklySales").getAsString());
    }
    if (data.has("activeSuppliers")) {
      activeSuppliersCard.updateValue(data.get("activeSuppliers").getAsString());
    }

    if (data.has("pendingIcrsList")) {
      JsonArray icrs = data.get("pendingIcrsList").getAsJsonArray();
      pendingIcrsTableModel.setRowCount(0);
      int count = 0;
      for (JsonElement el : icrs) {
        if (count >= 5) break;
        JsonObject icr = el.getAsJsonObject();
        pendingIcrsTableModel.addRow(
            new Object[] {
              getOrDefault(icr, "icrNumber", "N/A"),
              getOrDefault(icr, "product", "N/A"),
              getOrDefault(icr, "quantity", "N/A"),
              getOrDefault(icr, "clerk", "N/A")
            });
        count++;
      }
    }

    if (data.has("topProducts")) {
      JsonArray products = data.get("topProducts").getAsJsonArray();
      topProductsTableModel.setRowCount(0);
      int count = 0;
      for (JsonElement el : products) {
        if (count >= 5) break;
        JsonObject prod = el.getAsJsonObject();
        topProductsTableModel.addRow(
            new Object[] {
              getOrDefault(prod, "name", "N/A"),
              getOrDefault(prod, "sales", "N/A"),
              "$" + getOrDefault(prod, "revenue", "0.00")
            });
        count++;
      }
    }
  }

  private String getOrDefault(JsonObject obj, String key, String defaultValue) {
    return obj.has(key) ? obj.get(key).getAsString() : defaultValue;
  }
}
