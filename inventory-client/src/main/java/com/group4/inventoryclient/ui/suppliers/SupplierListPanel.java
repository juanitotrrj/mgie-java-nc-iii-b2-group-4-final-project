package com.group4.inventoryclient.ui.suppliers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.group4.inventoryclient.api.ApiClient;
import com.group4.inventoryclient.api.SupplierApiClient;
import com.group4.inventoryclient.ui.components.ExportButton;
import com.group4.inventoryclient.ui.components.PaginatedTable;
import com.group4.inventoryclient.ui.components.SearchFilterBar;
import com.group4.inventoryclient.util.SwingUtil;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class SupplierListPanel extends JPanel {

  private final SupplierApiClient supplierApi;
  private final ApiClient apiClient;
  private final Frame parentFrame;
  private final SearchFilterBar searchBar;
  private final PaginatedTable table;
  private final JButton addBtn;
  private final JButton editBtn;
  private final JButton deleteBtn;
  private final JButton refreshBtn;

  public SupplierListPanel(SupplierApiClient supplierApi, ApiClient apiClient, Frame parentFrame) {
    super(new BorderLayout(10, 10));
    this.supplierApi = supplierApi;
    this.apiClient = apiClient;
    this.parentFrame = parentFrame;
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    searchBar = new SearchFilterBar();
    searchBar.addFilter("Status", new String[] {"All", "Active", "Inactive"});
    searchBar.setSearchListener(query -> loadData());
    add(searchBar, BorderLayout.NORTH);

    table =
        new PaginatedTable(
            new String[] {"ID", "Name", "Contact Person", "Phone", "Email", "Status"});
    table.setPageChangeListener(page -> loadData());
    add(table, BorderLayout.CENTER);

    JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
    addBtn = new JButton("Add");
    editBtn = new JButton("Edit");
    deleteBtn = new JButton("Delete");
    refreshBtn = new JButton("Refresh");
    ExportButton exportBtn =
        new ExportButton(apiClient, "/suppliers/export?format=csv", parentFrame);

    addBtn.addActionListener(e -> handleAdd());
    editBtn.addActionListener(e -> handleEdit());
    deleteBtn.addActionListener(e -> handleDelete());
    refreshBtn.addActionListener(e -> loadData());

    toolbarPanel.add(addBtn);
    toolbarPanel.add(editBtn);
    toolbarPanel.add(deleteBtn);
    toolbarPanel.add(refreshBtn);
    toolbarPanel.add(exportBtn);
    add(toolbarPanel, BorderLayout.SOUTH);

    loadData();
  }

  private void loadData() {
    new Thread(
            () -> {
              try {
                String search = searchBar.getSearchText();
                String status = searchBar.getFilterValue(0);
                int page = table.getCurrentPage();

                ApiClient.ApiResponse response = supplierApi.list(page, search, status);
                if (!response.isSuccess()) {
                  SwingUtil.showError(
                      this, "Failed to load suppliers: " + response.getErrorMessage());
                  return;
                }

                JsonArray data = response.getDataAsArray();
                JsonObject meta = response.getMeta();
                int totalPages =
                    meta != null && meta.has("totalPages") ? meta.get("totalPages").getAsInt() : 1;

                Object[][] rows = new Object[data.size()][6];
                int i = 0;
                for (JsonElement el : data) {
                  JsonObject supplier = el.getAsJsonObject();
                  rows[i][0] = getOrDefault(supplier, "id", "");
                  rows[i][1] = getOrDefault(supplier, "name", "");
                  rows[i][2] = getOrDefault(supplier, "contactPerson", "");
                  rows[i][3] = getOrDefault(supplier, "phone", "");
                  rows[i][4] = getOrDefault(supplier, "email", "");
                  rows[i][5] = getOrDefault(supplier, "status", "");
                  i++;
                }

                SwingUtilities.invokeLater(() -> table.setData(rows, page, totalPages));
              } catch (Exception e) {
                SwingUtil.showError(this, "Error loading suppliers: " + e.getMessage());
              }
            })
        .start();
  }

  private void handleAdd() {
    SupplierFormDialog dialog = new SupplierFormDialog(parentFrame, null);
    dialog.showAndWait();
    if (dialog.isConfirmed()) {
      new Thread(
              () -> {
                try {
                  ApiClient.ApiResponse response = supplierApi.create(dialog.getFormData());
                  if (response.isSuccess()) {
                    SwingUtil.showInfo(this, "Supplier created successfully");
                    SwingUtilities.invokeLater(this::loadData);
                  } else {
                    SwingUtil.showError(
                        this, "Failed to create supplier: " + response.getErrorMessage());
                  }
                } catch (Exception e) {
                  SwingUtil.showError(this, "Error creating supplier: " + e.getMessage());
                }
              })
          .start();
    }
  }

  private void handleEdit() {
    int selectedRow = table.getSelectedRow();
    if (selectedRow < 0) {
      SwingUtil.showError(this, "Please select a supplier to edit");
      return;
    }

    Object idObj = table.getTable().getValueAt(selectedRow, 0);
    long id = Long.parseLong(idObj.toString());

    new Thread(
            () -> {
              try {
                ApiClient.ApiResponse response = supplierApi.get(id);
                if (!response.isSuccess()) {
                  SwingUtil.showError(
                      this, "Failed to load supplier: " + response.getErrorMessage());
                  return;
                }

                JsonObject supplier = response.getDataAsObject();
                SwingUtilities.invokeLater(
                    () -> {
                      SupplierFormDialog dialog = new SupplierFormDialog(parentFrame, supplier);
                      dialog.showAndWait();
                      if (dialog.isConfirmed()) {
                        new Thread(
                                () -> {
                                  try {
                                    ApiClient.ApiResponse updateResponse =
                                        supplierApi.update(id, dialog.getFormData());
                                    if (updateResponse.isSuccess()) {
                                      SwingUtil.showInfo(this, "Supplier updated successfully");
                                      SwingUtilities.invokeLater(this::loadData);
                                    } else {
                                      SwingUtil.showError(
                                          this,
                                          "Failed to update supplier: "
                                              + updateResponse.getErrorMessage());
                                    }
                                  } catch (Exception e) {
                                    SwingUtil.showError(
                                        this, "Error updating supplier: " + e.getMessage());
                                  }
                                })
                            .start();
                      }
                    });
              } catch (Exception e) {
                SwingUtil.showError(this, "Error loading supplier: " + e.getMessage());
              }
            })
        .start();
  }

  private void handleDelete() {
    int selectedRow = table.getSelectedRow();
    if (selectedRow < 0) {
      SwingUtil.showError(this, "Please select a supplier to delete");
      return;
    }

    Object idObj = table.getTable().getValueAt(selectedRow, 0);
    String name = table.getTable().getValueAt(selectedRow, 1).toString();

    int confirm =
        JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete supplier: " + name + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);

    if (confirm == JOptionPane.YES_OPTION) {
      long id = Long.parseLong(idObj.toString());
      new Thread(
              () -> {
                try {
                  ApiClient.ApiResponse response = supplierApi.delete(id);
                  if (response.isSuccess()) {
                    SwingUtil.showInfo(this, "Supplier deleted successfully");
                    SwingUtilities.invokeLater(this::loadData);
                  } else {
                    SwingUtil.showError(
                        this, "Failed to delete supplier: " + response.getErrorMessage());
                  }
                } catch (Exception e) {
                  SwingUtil.showError(this, "Error deleting supplier: " + e.getMessage());
                }
              })
          .start();
    }
  }

  private String getOrDefault(JsonObject obj, String key, String defaultValue) {
    return obj.has(key) ? obj.get(key).getAsString() : defaultValue;
  }
}
