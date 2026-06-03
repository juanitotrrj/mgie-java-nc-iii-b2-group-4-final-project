package com.group4.inventoryclient.ui.products;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.group4.inventoryclient.api.ApiClient;
import com.group4.inventoryclient.api.ProductApiClient;
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

public class ProductListPanel extends JPanel {

  private final ProductApiClient productApi;
  private final ApiClient apiClient;
  private final Frame parentFrame;
  private final SearchFilterBar searchBar;
  private final PaginatedTable table;
  private final JButton addBtn;
  private final JButton editBtn;
  private final JButton deleteBtn;
  private final JButton refreshBtn;

  public ProductListPanel(ProductApiClient productApi, ApiClient apiClient, Frame parentFrame) {
    super(new BorderLayout(10, 10));
    this.productApi = productApi;
    this.apiClient = apiClient;
    this.parentFrame = parentFrame;
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    searchBar = new SearchFilterBar();
    searchBar.addFilter("Status", new String[] {"All", "Active", "Inactive"});
    searchBar.addFilter("Category", new String[] {"All"});
    searchBar.setSearchListener(query -> loadData());
    add(searchBar, BorderLayout.NORTH);

    table =
        new PaginatedTable(
            new String[] {"ID", "Name", "SKU", "Category", "Qty", "Price", "Status"});
    table.setPageChangeListener(page -> loadData());
    add(table, BorderLayout.CENTER);

    JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
    addBtn = new JButton("Add");
    editBtn = new JButton("Edit");
    deleteBtn = new JButton("Delete");
    refreshBtn = new JButton("Refresh");
    ExportButton exportBtn =
        new ExportButton(apiClient, "/exports/resources?type=products&format=csv", parentFrame);

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
                String category = searchBar.getFilterValue(1);
                int page = table.getCurrentPage();

                ApiClient.ApiResponse response = productApi.list(page, search, status, category);
                if (!response.isSuccess()) {
                  SwingUtil.showError(
                      this, "Failed to load products: " + response.getErrorMessage());
                  return;
                }

                JsonArray data = response.getDataAsArray();
                JsonObject meta = response.getMeta();
                int totalPages =
                    meta != null && meta.has("totalPages") ? meta.get("totalPages").getAsInt() : 1;

                Object[][] rows = new Object[data.size()][7];
                int i = 0;
                for (JsonElement el : data) {
                  JsonObject product = el.getAsJsonObject();
                  rows[i][0] = getOrDefault(product, "id", "");
                  rows[i][1] = getOrDefault(product, "name", "");
                  rows[i][2] = getOrDefault(product, "sku", "");
                  rows[i][3] = getOrDefault(product, "category", "");
                  rows[i][4] = getOrDefault(product, "quantity", "0");
                  rows[i][5] = getOrDefault(product, "unitPrice", "0.00");
                  rows[i][6] = getOrDefault(product, "status", "");
                  i++;
                }

                SwingUtilities.invokeLater(() -> table.setData(rows, page, totalPages));
              } catch (Exception e) {
                SwingUtil.showError(this, "Error loading products: " + e.getMessage());
              }
            })
        .start();
  }

  private void handleAdd() {
    ProductFormDialog dialog = new ProductFormDialog(parentFrame, null);
    dialog.showAndWait();
    if (dialog.isConfirmed()) {
      new Thread(
              () -> {
                try {
                  ApiClient.ApiResponse response = productApi.create(dialog.getFormData());
                  if (response.isSuccess()) {
                    SwingUtil.showInfo(this, "Product created successfully");
                    SwingUtilities.invokeLater(this::loadData);
                  } else {
                    SwingUtil.showError(
                        this, "Failed to create product: " + response.getErrorMessage());
                  }
                } catch (Exception e) {
                  SwingUtil.showError(this, "Error creating product: " + e.getMessage());
                }
              })
          .start();
    }
  }

  private void handleEdit() {
    int selectedRow = table.getSelectedRow();
    if (selectedRow < 0) {
      SwingUtil.showError(this, "Please select a product to edit");
      return;
    }

    Object idObj = table.getTable().getValueAt(selectedRow, 0);
    long id = Long.parseLong(idObj.toString());

    new Thread(
            () -> {
              try {
                ApiClient.ApiResponse response = productApi.get(id);
                if (!response.isSuccess()) {
                  SwingUtil.showError(
                      this, "Failed to load product: " + response.getErrorMessage());
                  return;
                }

                JsonObject product = response.getDataAsObject();
                SwingUtilities.invokeLater(
                    () -> {
                      ProductFormDialog dialog = new ProductFormDialog(parentFrame, product);
                      dialog.showAndWait();
                      if (dialog.isConfirmed()) {
                        new Thread(
                                () -> {
                                  try {
                                    ApiClient.ApiResponse updateResponse =
                                        productApi.update(id, dialog.getFormData());
                                    if (updateResponse.isSuccess()) {
                                      SwingUtil.showInfo(this, "Product updated successfully");
                                      SwingUtilities.invokeLater(this::loadData);
                                    } else {
                                      SwingUtil.showError(
                                          this,
                                          "Failed to update product: "
                                              + updateResponse.getErrorMessage());
                                    }
                                  } catch (Exception e) {
                                    SwingUtil.showError(
                                        this, "Error updating product: " + e.getMessage());
                                  }
                                })
                            .start();
                      }
                    });
              } catch (Exception e) {
                SwingUtil.showError(this, "Error loading product: " + e.getMessage());
              }
            })
        .start();
  }

  private void handleDelete() {
    int selectedRow = table.getSelectedRow();
    if (selectedRow < 0) {
      SwingUtil.showError(this, "Please select a product to delete");
      return;
    }

    Object idObj = table.getTable().getValueAt(selectedRow, 0);
    String name = table.getTable().getValueAt(selectedRow, 1).toString();

    int confirm =
        JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete product: " + name + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);

    if (confirm == JOptionPane.YES_OPTION) {
      long id = Long.parseLong(idObj.toString());
      new Thread(
              () -> {
                try {
                  ApiClient.ApiResponse response = productApi.delete(id);
                  if (response.isSuccess()) {
                    SwingUtil.showInfo(this, "Product deleted successfully");
                    SwingUtilities.invokeLater(this::loadData);
                  } else {
                    SwingUtil.showError(
                        this, "Failed to delete product: " + response.getErrorMessage());
                  }
                } catch (Exception e) {
                  SwingUtil.showError(this, "Error deleting product: " + e.getMessage());
                }
              })
          .start();
    }
  }

  private String getOrDefault(JsonObject obj, String key, String defaultValue) {
    return obj.has(key) ? obj.get(key).getAsString() : defaultValue;
  }
}
