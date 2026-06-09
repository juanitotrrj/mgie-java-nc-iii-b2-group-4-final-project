package com.group4.inventoryclient.ui.purchases;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.group4.inventoryclient.api.ApiClient;
import com.group4.inventoryclient.api.PurchaseApiClient;
import com.group4.inventoryclient.ui.components.ExportButton;
import com.group4.inventoryclient.ui.components.PaginatedTable;
import com.group4.inventoryclient.ui.components.SearchFilterBar;
import com.group4.inventoryclient.util.JsonFieldUtil;
import com.group4.inventoryclient.util.PaginationUtil;
import com.group4.inventoryclient.util.SwingUtil;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class PurchaseListPanel extends JPanel {

  private final PurchaseApiClient purchaseApi;
  private final ApiClient apiClient;
  private final Frame parentFrame;
  private final SearchFilterBar searchBar = new SearchFilterBar();
  private final PaginatedTable table =
      new PaginatedTable(
          new String[] {"ID", "Supplier", "Order Date", "Expected Date", "Status", "Total"});

  private final JButton addBtn = new JButton("Add");
  private final JButton receiveBtn = new JButton("Receive");
  private final JButton cancelBtn = new JButton("Cancel");
  private final JButton refreshBtn = new JButton("Refresh");

  public PurchaseListPanel(ApiClient apiClient, Frame parentFrame) {
    super(new BorderLayout(5, 5));
    this.apiClient = apiClient;
    this.purchaseApi = new PurchaseApiClient(apiClient);
    this.parentFrame = parentFrame;

    searchBar.addFilter(
        "Status", new String[] {"All", "Pending", "Delivered", "Received", "Cancelled"});
    searchBar.setSearchListener(q -> loadData(1));
    add(searchBar, BorderLayout.NORTH);

    add(table, BorderLayout.CENTER);

    JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
    toolbar.add(addBtn);
    toolbar.add(receiveBtn);
    toolbar.add(cancelBtn);
    toolbar.add(refreshBtn);
    toolbar.add(new ExportButton(apiClient, "/purchases/export?format=csv", this));
    add(toolbar, BorderLayout.SOUTH);

    table.setPageChangeListener(this::loadData);

    addBtn.addActionListener(e -> showAddDialog());
    receiveBtn.addActionListener(e -> handleReceive());
    cancelBtn.addActionListener(e -> handleCancel());
    refreshBtn.addActionListener(e -> loadData(table.getCurrentPage()));
    for (JButton btn : new JButton[] {addBtn, receiveBtn, cancelBtn, refreshBtn}) {
      btn.setFocusable(false);
    }
    MouseAdapter captureSelectedRow =
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent e) {
            table.captureSelectedRowIndex();
          }
        };
    receiveBtn.addMouseListener(captureSelectedRow);
    cancelBtn.addMouseListener(captureSelectedRow);

    loadData(1);
  }

  private void loadData(int page) {
    new Thread(
            () -> {
              try {
                String search = searchBar.getSearchText();
                String status = searchBar.getFilterValue(0);
                ApiClient.ApiResponse response = purchaseApi.list(page, search, status);
                if (response.isSuccess()) {
                  JsonArray data = response.getDataAsArray();
                  JsonObject meta = response.getMeta();
                  int totalPages = PaginationUtil.totalPages(meta);

                  Object[][] rows = new Object[data.size()][6];
                  for (int i = 0; i < data.size(); i++) {
                    JsonObject item = data.get(i).getAsJsonObject();
                    rows[i][0] = JsonFieldUtil.getLong(item, "purchaseId", "id");
                    rows[i][1] = JsonFieldUtil.getString(item, "N/A", "supplierName");
                    if ("N/A".equals(rows[i][1])
                        && item.has("supplier")
                        && item.get("supplier").isJsonObject()) {
                      rows[i][1] =
                          JsonFieldUtil.getString(item.getAsJsonObject("supplier"), "N/A", "name");
                    }
                    rows[i][2] = JsonFieldUtil.getString(item, "N/A", "orderDate");
                    rows[i][3] = JsonFieldUtil.getString(item, "N/A", "expectedDeliveryDate");
                    rows[i][4] = JsonFieldUtil.getString(item, "N/A", "status");
                    rows[i][5] =
                        String.format("%.2f", JsonFieldUtil.getDouble(item, "totalAmount"));
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

  private void showAddDialog() {
    PurchaseFormDialog dialog = new PurchaseFormDialog(parentFrame);
    dialog.showAndWait();
    if (dialog.isConfirmed()) {
      Map<String, Object> data = dialog.getFormData();
      new Thread(
              () -> {
                try {
                  ApiClient.ApiResponse response = purchaseApi.create(data);
                  if (response.isSuccess()) {
                    SwingUtilities.invokeLater(
                        () -> {
                          SwingUtil.showInfo(this, "Purchase order created successfully");
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

  private void handleReceive() {
    int row = table.resolveSelectedRowIndex();
    table.clearCapturedRowIndex();
    if (row == -1) {
      SwingUtil.showError(this, "Please select a purchase order");
      return;
    }
    long id;
    try {
      id = table.getLongValue(row, 0);
    } catch (NumberFormatException e) {
      SwingUtil.showError(this, "Invalid purchase order selected");
      return;
    }
    int confirm =
        JOptionPane.showConfirmDialog(
            this,
            "Confirm receipt of this purchase order?",
            "Receive Purchase",
            JOptionPane.YES_NO_OPTION);
    if (confirm == JOptionPane.YES_OPTION) {
      new Thread(
              () -> {
                try {
                  Map<String, Object> body = new HashMap<>();
                  ApiClient.ApiResponse response = purchaseApi.receive(id, body);
                  if (response.isSuccess()) {
                    SwingUtilities.invokeLater(
                        () -> {
                          SwingUtil.showInfo(this, "Purchase order received successfully");
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

  private void handleCancel() {
    int row = table.resolveSelectedRowIndex();
    table.clearCapturedRowIndex();
    if (row == -1) {
      SwingUtil.showError(this, "Please select a purchase order");
      return;
    }
    long id;
    try {
      id = table.getLongValue(row, 0);
    } catch (NumberFormatException e) {
      SwingUtil.showError(this, "Invalid purchase order selected");
      return;
    }
    String reason =
        JOptionPane.showInputDialog(
            this, "Enter cancellation reason:", "Cancel Purchase", JOptionPane.PLAIN_MESSAGE);
    if (reason != null && !reason.trim().isEmpty()) {
      new Thread(
              () -> {
                try {
                  Map<String, Object> body = new HashMap<>();
                  body.put("reason", reason.trim());
                  ApiClient.ApiResponse response = purchaseApi.cancel(id, body);
                  if (response.isSuccess()) {
                    SwingUtilities.invokeLater(
                        () -> {
                          SwingUtil.showInfo(this, "Purchase order cancelled successfully");
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
