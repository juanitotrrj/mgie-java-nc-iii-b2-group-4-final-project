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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class AdminDashboard extends JPanel {

  private final DashboardApiClient dashboardApi;
  private final MetricCardPanel totalProductsCard;
  private final MetricCardPanel totalUsersCard;
  private final MetricCardPanel revenueTodayCard;
  private final MetricCardPanel pendingIcrsCard;
  private final DefaultTableModel auditTableModel;
  private final JLabel systemStatusLabel;

  public AdminDashboard(DashboardApiClient dashboardApi) {
    super(new BorderLayout(10, 10));
    this.dashboardApi = dashboardApi;
    setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    JPanel metricsPanel = new JPanel(new GridLayout(1, 4, 10, 10));
    totalProductsCard =
        new MetricCardPanel(
            "Total Products", "0", "All products in inventory", new Color(59, 130, 246));
    totalUsersCard =
        new MetricCardPanel("Total Users", "0", "Active users", new Color(16, 185, 129));
    revenueTodayCard =
        new MetricCardPanel("Revenue Today", "$0.00", "Sales today", new Color(245, 158, 11));
    pendingIcrsCard =
        new MetricCardPanel("Pending ICRs", "0", "Awaiting approval", new Color(239, 68, 68));

    metricsPanel.add(totalProductsCard);
    metricsPanel.add(totalUsersCard);
    metricsPanel.add(revenueTodayCard);
    metricsPanel.add(pendingIcrsCard);
    add(metricsPanel, BorderLayout.NORTH);

    JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
    auditTableModel =
        new DefaultTableModel(new String[] {"Timestamp", "User", "Action", "Resource"}, 0) {
          @Override
          public boolean isCellEditable(int row, int column) {
            return false;
          }
        };
    JTable auditTable = new JTable(auditTableModel);
    JScrollPane auditScrollPane = new JScrollPane(auditTable);
    auditScrollPane.setBorder(BorderFactory.createTitledBorder("Recent Audit Logs"));
    centerPanel.add(auditScrollPane, BorderLayout.CENTER);

    systemStatusLabel = new JLabel("System Status: Loading...");
    systemStatusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
    centerPanel.add(systemStatusLabel, BorderLayout.SOUTH);

    add(centerPanel, BorderLayout.CENTER);
    loadDashboardData();
  }

  private void loadDashboardData() {
    new Thread(
            () -> {
              try {
                JsonObject data = dashboardApi.getAdminDashboard();
                SwingUtilities.invokeLater(() -> updateDashboard(data));
              } catch (Exception e) {
                SwingUtil.showError(this, "Failed to load dashboard: " + e.getMessage());
              }
            })
        .start();
  }

  private void updateDashboard(JsonObject data) {
    if (data.has("totalProducts")) {
      totalProductsCard.updateValue(data.get("totalProducts").getAsString());
    }
    if (data.has("totalUsers")) {
      totalUsersCard.updateValue(data.get("totalUsers").getAsString());
    }
    if (data.has("revenueToday")) {
      revenueTodayCard.updateValue("$" + data.get("revenueToday").getAsString());
    }
    if (data.has("pendingIcrs")) {
      pendingIcrsCard.updateValue(data.get("pendingIcrs").getAsString());
    }

    if (data.has("auditLogs")) {
      JsonArray logs = data.get("auditLogs").getAsJsonArray();
      auditTableModel.setRowCount(0);
      int count = 0;
      for (JsonElement el : logs) {
        if (count >= 5) break;
        JsonObject log = el.getAsJsonObject();
        auditTableModel.addRow(
            new Object[] {
              getOrDefault(log, "timestamp", "N/A"),
              getOrDefault(log, "user", "N/A"),
              getOrDefault(log, "action", "N/A"),
              getOrDefault(log, "resource", "N/A")
            });
        count++;
      }
    }

    if (data.has("systemStatus")) {
      systemStatusLabel.setText("System Status: " + data.get("systemStatus").getAsString());
    } else {
      systemStatusLabel.setText("System Status: OK");
    }
  }

  private String getOrDefault(JsonObject obj, String key, String defaultValue) {
    return obj.has(key) ? obj.get(key).getAsString() : defaultValue;
  }
}
