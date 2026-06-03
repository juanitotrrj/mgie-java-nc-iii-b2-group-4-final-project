package com.group4.inventoryclient.ui.icr;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.group4.inventoryclient.api.ApiClient;
import com.group4.inventoryclient.api.IcrApiClient;
import com.group4.inventoryclient.ui.components.PaginatedTable;
import com.group4.inventoryclient.ui.components.SearchFilterBar;
import com.group4.inventoryclient.util.SwingUtil;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class IcrListPanel extends JPanel {

  private final IcrApiClient icrApi;
  private final Frame parentFrame;
  private final SearchFilterBar searchBar = new SearchFilterBar();
  private final PaginatedTable table =
      new PaginatedTable(
          new String[] {"ID", "Product", "Type", "Quantity", "Requester", "Status", "Date"});

  private final JButton submitBtn = new JButton("Submit New");
  private final JButton approveBtn = new JButton("Approve");
  private final JButton rejectBtn = new JButton("Reject");
  private final JButton refreshBtn = new JButton("Refresh");

  public IcrListPanel(ApiClient apiClient, Frame parentFrame) {
    super(new BorderLayout(5, 5));
    this.icrApi = new IcrApiClient(apiClient);
    this.parentFrame = parentFrame;

    searchBar.addFilter("Status", new String[] {"All", "Pending", "Approved", "Rejected"});
    searchBar.setSearchListener(q -> loadData(1));
    add(searchBar, BorderLayout.NORTH);

    add(table, BorderLayout.CENTER);

    JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
    toolbar.add(submitBtn);
    toolbar.add(approveBtn);
    toolbar.add(rejectBtn);
    toolbar.add(refreshBtn);
    add(toolbar, BorderLayout.SOUTH);

    table.setPageChangeListener(this::loadData);

    submitBtn.addActionListener(e -> showSubmitDialog());
    approveBtn.addActionListener(e -> handleApprove());
    rejectBtn.addActionListener(e -> handleReject());
    refreshBtn.addActionListener(e -> loadData(table.getCurrentPage()));

    loadData(1);
  }

  private void loadData(int page) {
    new Thread(
            () -> {
              try {
                String search = searchBar.getSearchText();
                String status = searchBar.getFilterValue(0);
                ApiClient.ApiResponse response = icrApi.list(page, search, status);
                if (response.isSuccess()) {
                  JsonArray data = response.getDataAsArray();
                  JsonObject meta = response.getMeta();
                  int totalPages = meta != null ? meta.get("lastPage").getAsInt() : 1;

                  Object[][] rows = new Object[data.size()][7];
                  for (int i = 0; i < data.size(); i++) {
                    JsonObject item = data.get(i).getAsJsonObject();
                    rows[i][0] = item.get("id").getAsLong();
                    rows[i][1] =
                        item.has("product") && !item.get("product").isJsonNull()
                            ? item.getAsJsonObject("product").get("name").getAsString()
                            : "N/A";
                    rows[i][2] =
                        item.has("requestType") && !item.get("requestType").isJsonNull()
                            ? item.get("requestType").getAsString()
                            : "N/A";
                    rows[i][3] =
                        item.has("quantity") && !item.get("quantity").isJsonNull()
                            ? item.get("quantity").getAsInt()
                            : 0;
                    rows[i][4] =
                        item.has("requester") && !item.get("requester").isJsonNull()
                            ? item.getAsJsonObject("requester").get("name").getAsString()
                            : "N/A";
                    rows[i][5] =
                        item.has("status") && !item.get("status").isJsonNull()
                            ? item.get("status").getAsString()
                            : "N/A";
                    rows[i][6] =
                        item.has("requestDate") && !item.get("requestDate").isJsonNull()
                            ? item.get("requestDate").getAsString()
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

  private void showSubmitDialog() {
    IcrSubmitDialog dialog = new IcrSubmitDialog(parentFrame);
    dialog.showAndWait();
    if (dialog.isConfirmed()) {
      Map<String, Object> data = dialog.getFormData();
      new Thread(
              () -> {
                try {
                  ApiClient.ApiResponse response = icrApi.create(data);
                  if (response.isSuccess()) {
                    SwingUtilities.invokeLater(
                        () -> {
                          SwingUtil.showInfo(
                              this, "Inventory change request submitted successfully");
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

  private void handleApprove() {
    int row = table.getSelectedRow();
    if (row == -1) {
      SwingUtil.showError(this, "Please select a request");
      return;
    }
    long id = (Long) table.getTable().getValueAt(row, 0);
    int confirm =
        JOptionPane.showConfirmDialog(
            this, "Approve this inventory change request?", "Approve", JOptionPane.YES_NO_OPTION);
    if (confirm == JOptionPane.YES_OPTION) {
      new Thread(
              () -> {
                try {
                  Map<String, Object> body = new HashMap<>();
                  ApiClient.ApiResponse response = icrApi.approve(id, body);
                  if (response.isSuccess()) {
                    SwingUtilities.invokeLater(
                        () -> {
                          SwingUtil.showInfo(this, "Request approved successfully");
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

  private void handleReject() {
    int row = table.getSelectedRow();
    if (row == -1) {
      SwingUtil.showError(this, "Please select a request");
      return;
    }
    long id = (Long) table.getTable().getValueAt(row, 0);
    String reason =
        JOptionPane.showInputDialog(
            this, "Enter rejection reason:", "Reject Request", JOptionPane.PLAIN_MESSAGE);
    if (reason != null && !reason.trim().isEmpty()) {
      new Thread(
              () -> {
                try {
                  Map<String, Object> body = new HashMap<>();
                  body.put("reason", reason.trim());
                  ApiClient.ApiResponse response = icrApi.reject(id, body);
                  if (response.isSuccess()) {
                    SwingUtilities.invokeLater(
                        () -> {
                          SwingUtil.showInfo(this, "Request rejected successfully");
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
