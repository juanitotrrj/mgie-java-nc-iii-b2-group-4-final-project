package com.group4.inventoryclient.ui.products;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.group4.inventoryclient.api.ApiClient;
import com.group4.inventoryclient.api.CategoryApiClient;
import com.group4.inventoryclient.api.ProductApiClient;
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
import java.util.ArrayList;
import java.util.List;
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
    javax.swing.JComboBox<String> statusFilter =
        searchBar.addFilter("Status", new String[] {"All", "Active", "Inactive"});
    statusFilter.setSelectedItem("Active");
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
        new ExportButton(apiClient, "/products/export?format=csv", parentFrame);

    addBtn.addActionListener(e -> handleAdd());
    editBtn.addActionListener(e -> handleEdit());
    deleteBtn.addActionListener(e -> handleDelete());
    refreshBtn.addActionListener(e -> loadData());
    for (JButton btn : new JButton[] {addBtn, editBtn, deleteBtn, refreshBtn}) {
      btn.setFocusable(false);
    }
    MouseAdapter captureSelectedRow =
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent e) {
            table.captureSelectedRowIndex();
          }
        };
    editBtn.addMouseListener(captureSelectedRow);
    deleteBtn.addMouseListener(captureSelectedRow);

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
                int totalPages = PaginationUtil.totalPages(meta);

                Object[][] rows = new Object[data.size()][7];
                int i = 0;
                for (JsonElement el : data) {
                  JsonObject product = el.getAsJsonObject();
                  rows[i][0] = JsonFieldUtil.getLong(product, "productId", "id");
                  rows[i][1] = JsonFieldUtil.getString(product, "", "productName", "name");
                  rows[i][2] = JsonFieldUtil.getString(product, "", "productCode", "sku");
                  rows[i][3] = JsonFieldUtil.getString(product, "", "categoryName", "category");
                  rows[i][4] = String.valueOf(JsonFieldUtil.getInt(product, "quantity"));
                  rows[i][5] = String.format("%.2f", JsonFieldUtil.getDouble(product, "unitPrice"));
                  rows[i][6] = JsonFieldUtil.getString(product, "", "status");
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
    openProductForm(null, null);
  }

  private void handleEdit() {
    int selectedRow = table.resolveSelectedRowIndex();
    table.clearCapturedRowIndex();
    if (selectedRow < 0) {
      SwingUtil.showError(parentFrame, "Please select a product to edit");
      return;
    }

    long id;
    try {
      id = table.getLongValue(selectedRow, 0);
    } catch (NumberFormatException e) {
      SwingUtil.showError(parentFrame, "Invalid product selected");
      return;
    }

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
                SwingUtilities.invokeLater(() -> openProductForm(product, id));
              } catch (Exception e) {
                SwingUtil.showError(this, "Error loading product: " + e.getMessage());
              }
            })
        .start();
  }

  private void handleDelete() {
    int selectedRow = table.resolveSelectedRowIndex();
    table.clearCapturedRowIndex();
    if (selectedRow < 0) {
      SwingUtil.showError(parentFrame, "Please select a product to delete");
      return;
    }

    long id;
    String name;
    try {
      id = table.getLongValue(selectedRow, 0);
      name = table.getStringValue(selectedRow, 1);
    } catch (NumberFormatException e) {
      SwingUtil.showError(parentFrame, "Invalid product selected");
      return;
    }

    int confirm =
        JOptionPane.showConfirmDialog(
            parentFrame,
            "Are you sure you want to deactivate product: " + name + "?",
            "Confirm Deactivate",
            JOptionPane.YES_NO_OPTION);

    if (confirm != JOptionPane.YES_OPTION) {
      return;
    }

    new Thread(
            () -> {
              try {
                ApiClient.ApiResponse response = productApi.delete(id);
                if (response.isSuccess()) {
                  SwingUtil.showInfo(parentFrame, "Product deactivated successfully");
                  SwingUtilities.invokeLater(this::loadData);
                } else {
                  SwingUtil.showError(
                      parentFrame, "Failed to delete product: " + response.getErrorMessage());
                }
              } catch (Exception e) {
                SwingUtil.showError(parentFrame, "Error deleting product: " + e.getMessage());
              }
            })
        .start();
  }

  private void openProductForm(JsonObject product, Long productId) {
    new Thread(
            () -> {
              try {
                List<CategoryOption> categories = loadCategoryOptions();
                if (categories.isEmpty()) {
                  SwingUtilities.invokeLater(
                      () ->
                          SwingUtil.showError(
                              this, "No active categories found. Create a category first."));
                  return;
                }
                SwingUtilities.invokeLater(
                    () -> {
                      ProductFormDialog dialog =
                          new ProductFormDialog(parentFrame, product, categories);
                      dialog.showAndWait();
                      if (!dialog.isConfirmed()) {
                        return;
                      }
                      new Thread(
                              () -> {
                                try {
                                  ApiClient.ApiResponse saveResponse;
                                  if (productId == null) {
                                    saveResponse = productApi.create(dialog.getFormData());
                                  } else {
                                    saveResponse =
                                        productApi.update(productId, dialog.getFormData());
                                  }
                                  if (saveResponse.isSuccess()) {
                                    SwingUtil.showInfo(
                                        this,
                                        productId == null
                                            ? "Product created successfully"
                                            : "Product updated successfully");
                                    SwingUtilities.invokeLater(this::loadData);
                                  } else {
                                    SwingUtil.showError(
                                        this,
                                        "Failed to save product: "
                                            + saveResponse.getErrorMessage());
                                  }
                                } catch (Exception e) {
                                  SwingUtil.showError(
                                      this, "Error saving product: " + e.getMessage());
                                }
                              })
                          .start();
                    });
              } catch (Exception e) {
                SwingUtilities.invokeLater(
                    () ->
                        SwingUtil.showError(this, "Failed to load categories: " + e.getMessage()));
              }
            })
        .start();
  }

  private List<CategoryOption> loadCategoryOptions() throws java.io.IOException {
    CategoryApiClient categoryApi = new CategoryApiClient(apiClient);
    ApiClient.ApiResponse response = categoryApi.list(1, "", 100, "Active");
    if (!response.isSuccess()) {
      throw new java.io.IOException(response.getErrorMessage());
    }
    JsonArray data = response.getDataAsArray();
    List<CategoryOption> options = new ArrayList<>();
    for (JsonElement el : data) {
      JsonObject category = el.getAsJsonObject();
      long id = JsonFieldUtil.getLong(category, "categoryId", "id");
      String name = JsonFieldUtil.getString(category, "", "categoryName", "name");
      if (id > 0 && !name.isEmpty()) {
        options.add(new CategoryOption(id, name));
      }
    }
    return options;
  }
}
