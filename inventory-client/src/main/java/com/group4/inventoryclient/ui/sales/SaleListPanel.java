package com.group4.inventoryclient.ui.sales;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.group4.inventoryclient.api.ApiClient;
import com.group4.inventoryclient.api.SaleApiClient;
import com.group4.inventoryclient.ui.components.ExportButton;
import com.group4.inventoryclient.ui.components.PaginatedTable;
import com.group4.inventoryclient.ui.components.SearchFilterBar;
import com.group4.inventoryclient.util.SwingUtil;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class SaleListPanel extends JPanel {

  private final SaleApiClient saleApi;
  private final ApiClient apiClient;
  private final Frame parentFrame;
  private final SearchFilterBar searchBar = new SearchFilterBar();
  private final PaginatedTable table =
      new PaginatedTable(
          new String[] {"ID", "Customer", "Date", "Payment Method", "Total", "Status"});

  private final JButton newSaleBtn = new JButton("New Sale");
  private final JButton viewReceiptBtn = new JButton("View Receipt");
  private final JButton cancelBtn = new JButton("Cancel");
  private final JButton refreshBtn = new JButton("Refresh");

  public SaleListPanel(ApiClient apiClient, Frame parentFrame) {
    super(new BorderLayout(5, 5));
    this.apiClient = apiClient;
    this.saleApi = new SaleApiClient(apiClient);
    this.parentFrame = parentFrame;

    searchBar.addFilter("Status", new String[] {"All", "Completed", "Cancelled"});
    searchBar.setSearchListener(q -> loadData(1));
    add(searchBar, BorderLayout.NORTH);

    add(table, BorderLayout.CENTER);

    JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
    toolbar.add(newSaleBtn);
    toolbar.add(viewReceiptBtn);
    toolbar.add(cancelBtn);
    toolbar.add(refreshBtn);
    toolbar.add(new ExportButton(apiClient, "/exports/resources?type=sales&format=csv", this));
    add(toolbar, BorderLayout.SOUTH);

    table.setPageChangeListener(this::loadData);

    newSaleBtn.addActionListener(e -> showNewSaleDialog());
    viewReceiptBtn.addActionListener(e -> viewReceipt());
    cancelBtn.addActionListener(e -> handleCancel());
    refreshBtn.addActionListener(e -> loadData(table.getCurrentPage()));

    loadData(1);
  }

  private void loadData(int page) {
    new Thread(
            () -> {
              try {
                String search = searchBar.getSearchText();
                String status = searchBar.getFilterValue(0);
                ApiClient.ApiResponse response = saleApi.list(page, search, status);
                if (response.isSuccess()) {
                  JsonArray data = response.getDataAsArray();
                  JsonObject meta = response.getMeta();
                  int totalPages = meta != null ? meta.get("lastPage").getAsInt() : 1;

                  Object[][] rows = new Object[data.size()][6];
                  for (int i = 0; i < data.size(); i++) {
                    JsonObject item = data.get(i).getAsJsonObject();
                    rows[i][0] = item.get("id").getAsLong();
                    rows[i][1] =
                        item.has("customerName") && !item.get("customerName").isJsonNull()
                            ? item.get("customerName").getAsString()
                            : "Walk-in";
                    rows[i][2] =
                        item.has("saleDate") && !item.get("saleDate").isJsonNull()
                            ? item.get("saleDate").getAsString()
                            : "N/A";
                    rows[i][3] =
                        item.has("paymentMethod") && !item.get("paymentMethod").isJsonNull()
                            ? item.get("paymentMethod").getAsString()
                            : "N/A";
                    rows[i][4] =
                        item.has("totalAmount") && !item.get("totalAmount").isJsonNull()
                            ? String.format("%.2f", item.get("totalAmount").getAsDouble())
                            : "0.00";
                    rows[i][5] =
                        item.has("status") && !item.get("status").isJsonNull()
                            ? item.get("status").getAsString()
                            : "N/A";
                  }
                  SwingUtilities.invokeLater(() -> table.setData(rows, page, totalPages));
                } else {
                  SwingUtilities.invokeLater(
                      () -> SwingUtil.showError(this, response.getErrorMessage()));
                }
              } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> SwingUtil.showError(this, ex.getMessage()));
              }
            })
        .start();
  }

  private void showNewSaleDialog() {
    SaleFormDialog dialog = new SaleFormDialog(parentFrame);
    dialog.showAndWait();
    if (dialog.isConfirmed()) {
      Map<String, Object> data = dialog.getFormData();
      new Thread(
              () -> {
                try {
                  ApiClient.ApiResponse response = saleApi.create(data);
                  if (response.isSuccess()) {
                    SwingUtilities.invokeLater(
                        () -> {
                          SwingUtil.showInfo(this, "Sale completed successfully");
                          loadData(1);
                        });
                  } else {
                    SwingUtilities.invokeLater(
                        () -> SwingUtil.showError(this, response.getErrorMessage()));
                  }
                } catch (Exception ex) {
                  SwingUtilities.invokeLater(() -> SwingUtil.showError(this, ex.getMessage()));
                }
              })
          .start();
    }
  }

  private void viewReceipt() {
    int row = table.getSelectedRow();
    if (row == -1) {
      SwingUtil.showError(this, "Please select a sale");
      return;
    }
    long id = (Long) table.getTable().getValueAt(row, 0);
    new Thread(
            () -> {
              try {
                ApiClient.ApiResponse response = saleApi.get(id);
                if (response.isSuccess()) {
                  JsonObject sale = response.getDataAsObject();
                  StringBuilder receipt = new StringBuilder();
                  receipt.append("========== RECEIPT ==========\n\n");
                  receipt.append("Sale ID: ").append(sale.get("id").getAsLong()).append("\n");
                  receipt
                      .append("Customer: ")
                      .append(
                          sale.has("customerName") && !sale.get("customerName").isJsonNull()
                              ? sale.get("customerName").getAsString()
                              : "Walk-in")
                      .append("\n");
                  receipt
                      .append("Date: ")
                      .append(
                          sale.has("saleDate") && !sale.get("saleDate").isJsonNull()
                              ? sale.get("saleDate").getAsString()
                              : "N/A")
                      .append("\n");
                  receipt
                      .append("Payment: ")
                      .append(
                          sale.has("paymentMethod") && !sale.get("paymentMethod").isJsonNull()
                              ? sale.get("paymentMethod").getAsString()
                              : "N/A")
                      .append("\n\n");
                  receipt.append("Items:\n");
                  if (sale.has("items") && sale.get("items").isJsonArray()) {
                    JsonArray items = sale.getAsJsonArray("items");
                    for (int i = 0; i < items.size(); i++) {
                      JsonObject item = items.get(i).getAsJsonObject();
                      receipt
                          .append("  - ")
                          .append(
                              item.has("product") && !item.get("product").isJsonNull()
                                  ? item.getAsJsonObject("product").get("name").getAsString()
                                  : "Product")
                          .append(" x")
                          .append(item.get("quantity").getAsInt())
                          .append(" @ ")
                          .append(String.format("%.2f", item.get("unitPrice").getAsDouble()))
                          .append("\n");
                    }
                  }
                  receipt.append("\n");
                  receipt
                      .append("Total: ")
                      .append(String.format("%.2f", sale.get("totalAmount").getAsDouble()))
                      .append("\n");
                  receipt.append("==============================\n");

                  SwingUtilities.invokeLater(
                      () -> {
                        JDialog receiptDialog = new JDialog(parentFrame, "Receipt", true);
                        JTextArea textArea = new JTextArea(receipt.toString(), 20, 40);
                        textArea.setEditable(false);
                        receiptDialog.add(new JScrollPane(textArea));
                        receiptDialog.pack();
                        receiptDialog.setLocationRelativeTo(parentFrame);
                        receiptDialog.setVisible(true);
                      });
                } else {
                  SwingUtilities.invokeLater(
                      () -> SwingUtil.showError(this, response.getErrorMessage()));
                }
              } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> SwingUtil.showError(this, ex.getMessage()));
              }
            })
        .start();
  }

  private void handleCancel() {
    int row = table.getSelectedRow();
    if (row == -1) {
      SwingUtil.showError(this, "Please select a sale");
      return;
    }
    long id = (Long) table.getTable().getValueAt(row, 0);
    String reason =
        JOptionPane.showInputDialog(
            this, "Enter cancellation reason:", "Cancel Sale", JOptionPane.PLAIN_MESSAGE);
    if (reason != null && !reason.trim().isEmpty()) {
      new Thread(
              () -> {
                try {
                  Map<String, Object> body = new HashMap<>();
                  body.put("reason", reason.trim());
                  ApiClient.ApiResponse response = saleApi.cancel(id, body);
                  if (response.isSuccess()) {
                    SwingUtilities.invokeLater(
                        () -> {
                          SwingUtil.showInfo(this, "Sale cancelled successfully");
                          loadData(table.getCurrentPage());
                        });
                  } else {
                    SwingUtilities.invokeLater(
                        () -> SwingUtil.showError(this, response.getErrorMessage()));
                  }
                } catch (Exception ex) {
                  SwingUtilities.invokeLater(() -> SwingUtil.showError(this, ex.getMessage()));
                }
              })
          .start();
    }
  }
}
