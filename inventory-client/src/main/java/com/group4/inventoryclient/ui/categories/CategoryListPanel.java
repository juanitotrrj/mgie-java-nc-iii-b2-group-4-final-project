package com.group4.inventoryclient.ui.categories;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.group4.inventoryclient.api.ApiClient;
import com.group4.inventoryclient.api.CategoryApiClient;
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
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class CategoryListPanel extends JPanel {

  private final CategoryApiClient categoryApi;
  private final ApiClient apiClient;
  private final Frame parentFrame;
  private final SearchFilterBar searchBar;
  private final PaginatedTable table;
  private final JButton addBtn;
  private final JButton editBtn;
  private final JButton deleteBtn;
  private final JButton refreshBtn;

  public CategoryListPanel(CategoryApiClient categoryApi, ApiClient apiClient, Frame parentFrame) {
    super(new BorderLayout(10, 10));
    this.categoryApi = categoryApi;
    this.apiClient = apiClient;
    this.parentFrame = parentFrame;
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    searchBar = new SearchFilterBar();
    searchBar.setSearchListener(query -> loadData());
    add(searchBar, BorderLayout.NORTH);

    table =
        new PaginatedTable(new String[] {"ID", "Name", "Description", "Product Count", "Status"});
    table.setPageChangeListener(page -> loadData());
    add(table, BorderLayout.CENTER);

    JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
    addBtn = new JButton("Add");
    editBtn = new JButton("Edit");
    deleteBtn = new JButton("Delete");
    refreshBtn = new JButton("Refresh");
    ExportButton exportBtn =
        new ExportButton(apiClient, "/categories/export?format=csv", parentFrame);

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
                int page = table.getCurrentPage();

                ApiClient.ApiResponse response = categoryApi.list(page, search);
                if (!response.isSuccess()) {
                  SwingUtil.showError(
                      this, "Failed to load categories: " + response.getErrorMessage());
                  return;
                }

                JsonArray data = response.getDataAsArray();
                JsonObject meta = response.getMeta();
                int totalPages = PaginationUtil.totalPages(meta);

                Object[][] rows = new Object[data.size()][5];
                int i = 0;
                for (JsonElement el : data) {
                  JsonObject category = el.getAsJsonObject();
                  rows[i][0] = JsonFieldUtil.getLong(category, "categoryId", "id");
                  rows[i][1] = JsonFieldUtil.getString(category, "", "categoryName", "name");
                  rows[i][2] = JsonFieldUtil.getString(category, "", "description");
                  rows[i][3] = String.valueOf(JsonFieldUtil.getInt(category, "productCount"));
                  rows[i][4] = JsonFieldUtil.getString(category, "", "status");
                  i++;
                }

                SwingUtilities.invokeLater(() -> table.setData(rows, page, totalPages));
              } catch (Exception e) {
                SwingUtil.showError(this, "Error loading categories: " + e.getMessage());
              }
            })
        .start();
  }

  private void handleAdd() {
    CategoryFormDialog dialog = new CategoryFormDialog(parentFrame, null);
    dialog.showAndWait();
    if (dialog.isConfirmed()) {
      new Thread(
              () -> {
                try {
                  ApiClient.ApiResponse response = categoryApi.create(dialog.getFormData());
                  if (response.isSuccess()) {
                    SwingUtil.showInfo(this, "Category created successfully");
                    SwingUtilities.invokeLater(this::loadData);
                  } else {
                    SwingUtil.showError(
                        this, "Failed to create category: " + response.getErrorMessage());
                  }
                } catch (Exception e) {
                  SwingUtil.showError(this, "Error creating category: " + e.getMessage());
                }
              })
          .start();
    }
  }

  private void handleEdit() {
    int selectedRow = table.resolveSelectedRowIndex();
    table.clearCapturedRowIndex();
    if (selectedRow < 0) {
      SwingUtil.showError(this, "Please select a category to edit");
      return;
    }

    long id;
    try {
      id = table.getLongValue(selectedRow, 0);
    } catch (NumberFormatException e) {
      SwingUtil.showError(this, "Invalid category selected");
      return;
    }

    new Thread(
            () -> {
              try {
                ApiClient.ApiResponse response = categoryApi.get(id);
                if (!response.isSuccess()) {
                  SwingUtil.showError(
                      this, "Failed to load category: " + response.getErrorMessage());
                  return;
                }

                JsonObject category = response.getDataAsObject();
                SwingUtilities.invokeLater(
                    () -> {
                      CategoryFormDialog dialog = new CategoryFormDialog(parentFrame, category);
                      dialog.showAndWait();
                      if (dialog.isConfirmed()) {
                        new Thread(
                                () -> {
                                  try {
                                    ApiClient.ApiResponse updateResponse =
                                        categoryApi.update(id, dialog.getFormData());
                                    if (updateResponse.isSuccess()) {
                                      SwingUtil.showInfo(this, "Category updated successfully");
                                      SwingUtilities.invokeLater(this::loadData);
                                    } else {
                                      SwingUtil.showError(
                                          this,
                                          "Failed to update category: "
                                              + updateResponse.getErrorMessage());
                                    }
                                  } catch (Exception e) {
                                    SwingUtil.showError(
                                        this, "Error updating category: " + e.getMessage());
                                  }
                                })
                            .start();
                      }
                    });
              } catch (Exception e) {
                SwingUtil.showError(this, "Error loading category: " + e.getMessage());
              }
            })
        .start();
  }

  private void handleDelete() {
    int selectedRow = table.resolveSelectedRowIndex();
    table.clearCapturedRowIndex();
    if (selectedRow < 0) {
      SwingUtil.showError(this, "Please select a category to delete");
      return;
    }

    long id;
    String name;
    try {
      id = table.getLongValue(selectedRow, 0);
      name = table.getStringValue(selectedRow, 1);
    } catch (NumberFormatException e) {
      SwingUtil.showError(this, "Invalid category selected");
      return;
    }

    int confirm =
        JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete category: " + name + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);

    if (confirm == JOptionPane.YES_OPTION) {
      new Thread(
              () -> {
                try {
                  ApiClient.ApiResponse response = categoryApi.delete(id);
                  if (response.isSuccess()) {
                    SwingUtil.showInfo(this, "Category deleted successfully");
                    SwingUtilities.invokeLater(this::loadData);
                  } else {
                    SwingUtil.showError(
                        this, "Failed to delete category: " + response.getErrorMessage());
                  }
                } catch (Exception e) {
                  SwingUtil.showError(this, "Error deleting category: " + e.getMessage());
                }
              })
          .start();
    }
  }
}
