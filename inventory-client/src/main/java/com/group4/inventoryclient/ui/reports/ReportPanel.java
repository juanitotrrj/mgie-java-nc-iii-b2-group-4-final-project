package com.group4.inventoryclient.ui.reports;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.group4.inventoryclient.api.ApiClient;
import com.group4.inventoryclient.api.ReportApiClient;
import com.group4.inventoryclient.ui.components.ExportButton;
import com.group4.inventoryclient.util.SwingUtil;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ReportPanel extends JPanel {

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private final ReportApiClient reportApiClient;
  private final ApiClient apiClient;
  private final JComboBox<String> typeCombo;
  private final JTextField dateFromField;
  private final JTextField dateToField;
  private final JTextField categoryField;
  private final JTextArea reportArea;
  private final JButton generateBtn;
  private ExportButton exportBtn;

  public ReportPanel(ApiClient apiClient) {
    super(new BorderLayout(10, 10));
    this.apiClient = apiClient;
    this.reportApiClient = new ReportApiClient(apiClient);

    typeCombo =
        new JComboBox<>(
            new String[] {
              "Sales Summary", "Inventory Value", "Purchase Cost", "Low Stock", "Top Selling"
            });
    dateFromField = new JTextField(10);
    dateToField = new JTextField(10);
    categoryField = new JTextField(10);
    generateBtn = new JButton("Generate");

    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
    topPanel.add(new JLabel("Report Type:"));
    topPanel.add(typeCombo);
    topPanel.add(new JLabel("Date From (YYYY-MM-DD):"));
    topPanel.add(dateFromField);
    topPanel.add(new JLabel("Date To (YYYY-MM-DD):"));
    topPanel.add(dateToField);
    topPanel.add(new JLabel("Category:"));
    topPanel.add(categoryField);
    topPanel.add(generateBtn);

    reportArea = new JTextArea(20, 60);
    reportArea.setEditable(false);
    reportArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));

    JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
    updateExportButton();
    bottomPanel.add(exportBtn);

    add(topPanel, BorderLayout.NORTH);
    add(new JScrollPane(reportArea), BorderLayout.CENTER);
    add(bottomPanel, BorderLayout.SOUTH);

    generateBtn.addActionListener(e -> generateReport());
  }

  private void generateReport() {
    String type = typeCombo.getSelectedItem().toString().replace(" ", "_").toUpperCase();
    String dateFrom = dateFromField.getText().trim();
    String dateTo = dateToField.getText().trim();
    String category = categoryField.getText().trim();

    generateBtn.setEnabled(false);
    reportArea.setText("Generating report...");

    new Thread(
            () -> {
              try {
                ApiClient.ApiResponse response =
                    reportApiClient.generate(type, dateFrom, dateTo, category);
                SwingUtilities.invokeLater(
                    () -> {
                      if (response.isSuccess()) {
                        String jsonData = GSON.toJson(response.getRawRoot());
                        reportArea.setText(jsonData);
                        updateExportButton();
                      } else {
                        reportArea.setText("Error: " + response.getErrorMessage());
                        SwingUtil.showError(this, response.getErrorMessage());
                      }
                      generateBtn.setEnabled(true);
                    });
              } catch (Exception ex) {
                SwingUtilities.invokeLater(
                    () -> {
                      reportArea.setText("Error: " + ex.getMessage());
                      SwingUtil.showError(this, ex.getMessage());
                      generateBtn.setEnabled(true);
                    });
              }
            })
        .start();
  }

  private void updateExportButton() {
    if (exportBtn != null) {
      remove(exportBtn.getParent());
    }
    String reportType = toReportTypeKey(typeCombo.getSelectedItem().toString());
    String dateFrom = dateFromField.getText().trim();
    String dateTo = dateToField.getText().trim();
    String exportPath = "/reports/export?reportType=" + reportType + "&format=csv";
    if (!dateFrom.isEmpty()) {
      exportPath += "&dateFrom=" + dateFrom;
    }
    if (!dateTo.isEmpty()) {
      exportPath += "&dateTo=" + dateTo;
    }
    exportBtn = new ExportButton(apiClient, exportPath, this);

    JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
    bottomPanel.add(exportBtn);
    add(bottomPanel, BorderLayout.SOUTH);
    revalidate();
  }

  private static String toReportTypeKey(String label) {
    switch (label) {
      case "Sales Summary":
        return "sales-summary";
      case "Inventory Value":
        return "inventory-value";
      case "Purchase Cost":
        return "purchase-cost";
      case "Low Stock":
        return "low-stock";
      case "Top Selling":
        return "top-selling-products";
      default:
        return label.toLowerCase().replace(' ', '-');
    }
  }
}
