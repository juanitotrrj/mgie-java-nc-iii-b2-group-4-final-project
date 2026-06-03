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

public class CashierDashboard extends JPanel {

  private final DashboardApiClient dashboardApi;
  private final MetricCardPanel salesTodayCard;
  private final MetricCardPanel transactionsTodayCard;
  private final MetricCardPanel avgSaleCard;
  private final DefaultTableModel salesTableModel;

  public CashierDashboard(DashboardApiClient dashboardApi) {
    super(new BorderLayout(10, 10));
    this.dashboardApi = dashboardApi;
    setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    JPanel metricsPanel = new JPanel(new GridLayout(1, 3, 10, 10));
    salesTodayCard =
        new MetricCardPanel("Sales Today", "$0.00", "Total sales today", new Color(16, 185, 129));
    transactionsTodayCard =
        new MetricCardPanel("Transactions Today", "0", "Completed today", new Color(59, 130, 246));
    avgSaleCard =
        new MetricCardPanel(
            "Average Sale Value", "$0.00", "Per transaction", new Color(245, 158, 11));

    metricsPanel.add(salesTodayCard);
    metricsPanel.add(transactionsTodayCard);
    metricsPanel.add(avgSaleCard);
    add(metricsPanel, BorderLayout.NORTH);

    salesTableModel =
        new DefaultTableModel(new String[] {"Time", "Transaction#", "Items", "Amount"}, 0) {
          @Override
          public boolean isCellEditable(int row, int column) {
            return false;
          }
        };
    JTable salesTable = new JTable(salesTableModel);
    JScrollPane salesScrollPane = new JScrollPane(salesTable);
    salesScrollPane.setBorder(BorderFactory.createTitledBorder("Recent Sales"));
    add(salesScrollPane, BorderLayout.CENTER);

    loadDashboardData();
  }

  private void loadDashboardData() {
    new Thread(
            () -> {
              try {
                JsonObject data = dashboardApi.getCashierDashboard();
                SwingUtilities.invokeLater(() -> updateDashboard(data));
              } catch (Exception e) {
                SwingUtil.showError(this, "Failed to load dashboard: " + e.getMessage());
              }
            })
        .start();
  }

  private void updateDashboard(JsonObject data) {
    if (data.has("salesToday")) {
      salesTodayCard.updateValue("$" + data.get("salesToday").getAsString());
    }
    if (data.has("transactionsToday")) {
      transactionsTodayCard.updateValue(data.get("transactionsToday").getAsString());
    }
    if (data.has("averageSaleValue")) {
      avgSaleCard.updateValue("$" + data.get("averageSaleValue").getAsString());
    }

    if (data.has("recentSales")) {
      JsonArray sales = data.get("recentSales").getAsJsonArray();
      salesTableModel.setRowCount(0);
      int count = 0;
      for (JsonElement el : sales) {
        if (count >= 5) break;
        JsonObject sale = el.getAsJsonObject();
        salesTableModel.addRow(
            new Object[] {
              getOrDefault(sale, "time", "N/A"),
              getOrDefault(sale, "transactionNumber", "N/A"),
              getOrDefault(sale, "items", "N/A"),
              "$" + getOrDefault(sale, "amount", "0.00")
            });
        count++;
      }
    }
  }

  private String getOrDefault(JsonObject obj, String key, String defaultValue) {
    return obj.has(key) ? obj.get(key).getAsString() : defaultValue;
  }
}
