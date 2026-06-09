package com.group4.inventoryclient.ui.users;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.group4.inventoryclient.api.ApiClient;
import com.group4.inventoryclient.api.UserApiClient;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class UserListPanel extends JPanel {

  private final UserApiClient userApiClient;
  private final SearchFilterBar searchBar;
  private final PaginatedTable table;
  private final JButton addBtn;
  private final JButton editBtn;
  private final JButton deactivateBtn;
  private final JButton resetPasswordBtn;
  private final JButton refreshBtn;

  public UserListPanel(ApiClient apiClient) {
    super(new BorderLayout(10, 10));
    this.userApiClient = new UserApiClient(apiClient);

    searchBar = new SearchFilterBar();
    searchBar.addFilter(
        "Role", new String[] {"All", "Administrator", "Manager", "Inventory Clerk", "Cashier"});
    searchBar.addFilter("Status", new String[] {"All", "Active", "Inactive"});
    searchBar.setSearchListener(query -> loadUsers());

    table =
        new PaginatedTable(new String[] {"ID", "Username", "Full Name", "Email", "Role", "Status"});
    table.setPageChangeListener(page -> loadUsers());

    addBtn = new JButton("Add");
    editBtn = new JButton("Edit");
    deactivateBtn = new JButton("Deactivate");
    resetPasswordBtn = new JButton("Reset Password");
    refreshBtn = new JButton("Refresh");
    ExportButton exportBtn = new ExportButton(apiClient, "/users/export?format=csv", this);

    JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
    toolbar.add(addBtn);
    toolbar.add(editBtn);
    toolbar.add(deactivateBtn);
    toolbar.add(resetPasswordBtn);
    toolbar.add(refreshBtn);
    toolbar.add(exportBtn);

    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.add(searchBar, BorderLayout.NORTH);
    topPanel.add(toolbar, BorderLayout.SOUTH);

    add(topPanel, BorderLayout.NORTH);
    add(table, BorderLayout.CENTER);

    addBtn.addActionListener(e -> addUser());
    editBtn.addActionListener(e -> editUser());
    deactivateBtn.addActionListener(e -> deactivateUser());
    resetPasswordBtn.addActionListener(e -> resetPassword());
    refreshBtn.addActionListener(e -> loadUsers());

    loadUsers();
  }

  private void loadUsers() {
    int page = table.getCurrentPage();
    String search = searchBar.getSearchText();
    String role = searchBar.getFilterValue(0);
    String status = searchBar.getFilterValue(1);

    new Thread(
            () -> {
              try {
                ApiClient.ApiResponse response = userApiClient.list(page, search, role, status);
                SwingUtilities.invokeLater(
                    () -> {
                      if (response.isSuccess()) {
                        JsonArray users = response.getDataAsArray();
                        JsonObject meta = response.getMeta();
                        int totalPages =
                            meta != null && meta.has("total_pages")
                                ? meta.get("total_pages").getAsInt()
                                : 1;

                        Object[][] rows = new Object[users.size()][6];
                        for (int i = 0; i < users.size(); i++) {
                          JsonObject user = users.get(i).getAsJsonObject();
                          rows[i][0] = user.has("id") ? user.get("id").getAsLong() : 0;
                          rows[i][1] =
                              user.has("username") ? user.get("username").getAsString() : "";
                          rows[i][2] =
                              user.has("full_name") ? user.get("full_name").getAsString() : "";
                          rows[i][3] = user.has("email") ? user.get("email").getAsString() : "";
                          rows[i][4] = user.has("role") ? user.get("role").getAsString() : "";
                          rows[i][5] = user.has("status") ? user.get("status").getAsString() : "";
                        }
                        table.setData(rows, page, totalPages);
                      } else {
                        SwingUtil.showError(this, response.getErrorMessage());
                      }
                    });
              } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> SwingUtil.showError(this, ex.getMessage()));
              }
            })
        .start();
  }

  private void addUser() {
    Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
    UserFormDialog dialog = new UserFormDialog(owner, null);
    dialog.show();

    if (dialog.isConfirmed()) {
      Map<String, Object> formData = dialog.getFormData();
      new Thread(
              () -> {
                try {
                  ApiClient.ApiResponse response = userApiClient.create(formData);
                  SwingUtilities.invokeLater(
                      () -> {
                        if (response.isSuccess()) {
                          SwingUtil.showInfo(this, "User created successfully");
                          loadUsers();
                        } else {
                          SwingUtil.showError(this, response.getErrorMessage());
                        }
                      });
                } catch (Exception ex) {
                  SwingUtilities.invokeLater(() -> SwingUtil.showError(this, ex.getMessage()));
                }
              })
          .start();
    }
  }

  private void editUser() {
    int selectedRow = table.getSelectedRow();
    if (selectedRow < 0) {
      SwingUtil.showError(this, "Please select a user to edit");
      return;
    }

    long userId = (Long) table.getTableModel().getValueAt(selectedRow, 0);

    new Thread(
            () -> {
              try {
                ApiClient.ApiResponse response = userApiClient.get(userId);
                SwingUtilities.invokeLater(
                    () -> {
                      if (response.isSuccess()) {
                        JsonObject user = response.getDataAsObject();
                        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
                        UserFormDialog dialog = new UserFormDialog(owner, user);
                        dialog.show();

                        if (dialog.isConfirmed()) {
                          Map<String, Object> formData = dialog.getFormData();
                          new Thread(
                                  () -> {
                                    try {
                                      ApiClient.ApiResponse updateResponse =
                                          userApiClient.update(userId, formData);
                                      SwingUtilities.invokeLater(
                                          () -> {
                                            if (updateResponse.isSuccess()) {
                                              SwingUtil.showInfo(this, "User updated successfully");
                                              loadUsers();
                                            } else {
                                              SwingUtil.showError(
                                                  this, updateResponse.getErrorMessage());
                                            }
                                          });
                                    } catch (Exception ex) {
                                      SwingUtilities.invokeLater(
                                          () -> SwingUtil.showError(this, ex.getMessage()));
                                    }
                                  })
                              .start();
                        }
                      } else {
                        SwingUtil.showError(this, response.getErrorMessage());
                      }
                    });
              } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> SwingUtil.showError(this, ex.getMessage()));
              }
            })
        .start();
  }

  private void deactivateUser() {
    int selectedRow = table.getSelectedRow();
    if (selectedRow < 0) {
      SwingUtil.showError(this, "Please select a user to deactivate");
      return;
    }

    long userId = (Long) table.getTableModel().getValueAt(selectedRow, 0);
    String username = (String) table.getTableModel().getValueAt(selectedRow, 1);

    int confirm =
        JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to deactivate user: " + username + "?",
            "Confirm Deactivation",
            JOptionPane.YES_NO_OPTION);

    if (confirm == JOptionPane.YES_OPTION) {
      new Thread(
              () -> {
                try {
                  ApiClient.ApiResponse response = userApiClient.deactivate(userId);
                  SwingUtilities.invokeLater(
                      () -> {
                        if (response.isSuccess()) {
                          SwingUtil.showInfo(this, "User deactivated successfully");
                          loadUsers();
                        } else {
                          SwingUtil.showError(this, response.getErrorMessage());
                        }
                      });
                } catch (Exception ex) {
                  SwingUtilities.invokeLater(() -> SwingUtil.showError(this, ex.getMessage()));
                }
              })
          .start();
    }
  }

  private void resetPassword() {
    int selectedRow = table.getSelectedRow();
    if (selectedRow < 0) {
      SwingUtil.showError(this, "Please select a user to reset password");
      return;
    }

    long userId = (Long) table.getTableModel().getValueAt(selectedRow, 0);

    Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
    ResetPasswordDialog dialog = new ResetPasswordDialog(owner);
    dialog.show();

    if (dialog.isConfirmed()) {
      String newPassword = dialog.getNewPassword();
      Map<String, Object> body = new HashMap<>();
      body.put("password", newPassword);

      new Thread(
              () -> {
                try {
                  ApiClient.ApiResponse response = userApiClient.resetPassword(userId, body);
                  SwingUtilities.invokeLater(
                      () -> {
                        if (response.isSuccess()) {
                          SwingUtil.showInfo(this, "Password reset successfully");
                        } else {
                          SwingUtil.showError(this, response.getErrorMessage());
                        }
                      });
                } catch (Exception ex) {
                  SwingUtilities.invokeLater(() -> SwingUtil.showError(this, ex.getMessage()));
                }
              })
          .start();
    }
  }
}
