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

public class ClerkDashboard extends JPanel {

  private final DashboardApiClient dashboardApi;
  private final MetricCardPanel myPendingIcrsCard;
  private final MetricCardPanel productsManagedCard;
  private final MetricCardPanel recentActivityCard;
  private final DefaultTableModel activityTableModel;

  public ClerkDashboard(DashboardApiClient dashboardApi) {
    super(new BorderLayout(10, 10));
    this.dashboardApi = dashboardApi;
    setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    JPanel metricsPanel = new JPanel(new GridLayout(1, 3, 10, 10));
    myPendingIcrsCard =
        new MetricCardPanel("My Pending ICRs", "0", "Awaiting approval", new Color(239, 68, 68));
    productsManagedCard =
        new MetricCardPanel(
            "Products Managed", "0", "Under my responsibility", new Color(59, 130, 246));
    recentActivityCard =
        new MetricCardPanel("Recent Activity", "0", "Actions this week", new Color(16, 185, 129));

    metricsPanel.add(myPendingIcrsCard);
    metricsPanel.add(productsManagedCard);
    metricsPanel.add(recentActivityCard);
    add(metricsPanel, BorderLayout.NORTH);

    activityTableModel =
        new DefaultTableModel(new String[] {"Timestamp", "Action", "Product", "Status"}, 0) {
          @Override
          public boolean isCellEditable(int row, int column) {
            return false;
          }
        };
    JTable activityTable = new JTable(activityTableModel);
    JScrollPane activityScrollPane = new JScrollPane(activityTable);
    activityScrollPane.setBorder(BorderFactory.createTitledBorder("Recent Inventory Activity"));
    add(activityScrollPane, BorderLayout.CENTER);

    loadDashboardData();
  }

  private void loadDashboardData() {
    new Thread(
            () -> {
              try {
                JsonObject data = dashboardApi.getClerkDashboard();
                SwingUtilities.invokeLater(() -> updateDashboard(data));
              } catch (Exception e) {
                SwingUtil.showError(this, "Failed to load dashboard: " + e.getMessage());
              }
            })
        .start();
  }

  private void updateDashboard(JsonObject data) {
    if (data.has("myPendingIcrs")) {
      myPendingIcrsCard.updateValue(data.get("myPendingIcrs").getAsString());
    }
    if (data.has("productsManaged")) {
      productsManagedCard.updateValue(data.get("productsManaged").getAsString());
    }
    if (data.has("recentActivity")) {
      recentActivityCard.updateValue(data.get("recentActivity").getAsString());
    }

    if (data.has("activityList")) {
      JsonArray activities = data.get("activityList").getAsJsonArray();
      activityTableModel.setRowCount(0);
      for (JsonElement el : activities) {
        JsonObject activity = el.getAsJsonObject();
        activityTableModel.addRow(
            new Object[] {
              getOrDefault(activity, "timestamp", "N/A"),
              getOrDefault(activity, "action", "N/A"),
              getOrDefault(activity, "product", "N/A"),
              getOrDefault(activity, "status", "N/A")
            });
      }
    }
  }

  private String getOrDefault(JsonObject obj, String key, String defaultValue) {
    return obj.has(key) ? obj.get(key).getAsString() : defaultValue;
  }
}
