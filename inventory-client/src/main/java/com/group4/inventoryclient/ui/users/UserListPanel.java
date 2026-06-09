package com.group4.inventoryclient.ui.users;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.group4.inventoryclient.api.ApiClient;
import com.group4.inventoryclient.api.UserApiClient;
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

public class UserListPanel extends JPanel {

  private final UserApiClient userApiClient;
  private final ApiClient apiClient;
  private final SearchFilterBar searchBar;
  private final PaginatedTable table;
  private final JButton addBtn;
  private final JButton editBtn;
  private final JButton deactivateBtn;
  private final JButton resetPasswordBtn;
  private final JButton refreshBtn;

  public UserListPanel(ApiClient apiClient) {
    this(apiClient, null);
  }

  public UserListPanel(ApiClient apiClient, Frame parentFrame) {
    super(new BorderLayout(10, 10));
    this.apiClient = apiClient;
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
    Frame dialogOwner = parentFrame != null ? parentFrame : resolveOwnerFrame();
    ExportButton exportBtn = new ExportButton(apiClient, "/users/export?format=csv", dialogOwner);

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
    for (JButton btn :
        new JButton[] {addBtn, editBtn, deactivateBtn, resetPasswordBtn, refreshBtn}) {
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
    deactivateBtn.addMouseListener(captureSelectedRow);
    resetPasswordBtn.addMouseListener(captureSelectedRow);

    loadUsers();
  }

  private Frame resolveOwnerFrame() {
    java.awt.Window window = SwingUtilities.getWindowAncestor(this);
    return window instanceof Frame ? (Frame) window : null;
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
                        int totalPages = PaginationUtil.totalPages(meta);

                        Object[][] rows = new Object[users.size()][6];
                        for (int i = 0; i < users.size(); i++) {
                          JsonObject user = users.get(i).getAsJsonObject();
                          rows[i][0] = JsonFieldUtil.getLong(user, "userId", "id");
                          rows[i][1] = JsonFieldUtil.getString(user, "", "username");
                          rows[i][2] = JsonFieldUtil.getString(user, "", "fullName", "full_name");
                          rows[i][3] = JsonFieldUtil.getString(user, "", "email");
                          rows[i][4] = JsonFieldUtil.getString(user, "", "role");
                          rows[i][5] = JsonFieldUtil.getString(user, "", "status");
                        }
                        table.setData(rows, page, totalPages);
                      } else {
                        SwingUtil.showError(resolveOwnerFrame(), response.getErrorMessage());
                      }
                    });
              } catch (Exception ex) {
                SwingUtilities.invokeLater(
                    () -> SwingUtil.showError(resolveOwnerFrame(), ex.getMessage()));
              }
            })
        .start();
  }

  private void addUser() {
    Frame owner = resolveOwnerFrame();
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
                          SwingUtil.showInfo(owner, "User created successfully");
                          loadUsers();
                        } else {
                          SwingUtil.showError(owner, response.getErrorMessage());
                        }
                      });
                } catch (Exception ex) {
                  SwingUtilities.invokeLater(() -> SwingUtil.showError(owner, ex.getMessage()));
                }
              })
          .start();
    }
  }

  private void editUser() {
    int selectedRow = table.resolveSelectedRowIndex();
    table.clearCapturedRowIndex();
    Frame owner = resolveOwnerFrame();
    if (selectedRow < 0) {
      SwingUtil.showError(owner, "Please select a user to edit");
      return;
    }

    long userId;
    try {
      userId = table.getLongValue(selectedRow, 0);
    } catch (NumberFormatException e) {
      SwingUtil.showError(owner, "Invalid user selected");
      return;
    }
    if (userId <= 0) {
      SwingUtil.showError(owner, "Invalid user selected");
      return;
    }

    new Thread(
            () -> {
              try {
                ApiClient.ApiResponse response = userApiClient.get(userId);
                SwingUtilities.invokeLater(
                    () -> {
                      if (response.isSuccess()) {
                        JsonObject user = response.getDataAsObject();
                        UserFormDialog dialog = new UserFormDialog(owner, user);
                        dialog.show();

                        if (dialog.isConfirmed()) {
                          Map<String, Object> formData = dialog.getFormData();
                          formData.remove("username");
                          formData.remove("password");
                          new Thread(
                                  () -> {
                                    try {
                                      ApiClient.ApiResponse updateResponse =
                                          userApiClient.update(userId, formData);
                                      SwingUtilities.invokeLater(
                                          () -> {
                                            if (updateResponse.isSuccess()) {
                                              SwingUtil.showInfo(
                                                  owner, "User updated successfully");
                                              loadUsers();
                                            } else {
                                              SwingUtil.showError(
                                                  owner, updateResponse.getErrorMessage());
                                            }
                                          });
                                    } catch (Exception ex) {
                                      SwingUtilities.invokeLater(
                                          () -> SwingUtil.showError(owner, ex.getMessage()));
                                    }
                                  })
                              .start();
                        }
                      } else {
                        SwingUtil.showError(owner, response.getErrorMessage());
                      }
                    });
              } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> SwingUtil.showError(owner, ex.getMessage()));
              }
            })
        .start();
  }

  private void deactivateUser() {
    int selectedRow = table.resolveSelectedRowIndex();
    table.clearCapturedRowIndex();
    Frame owner = resolveOwnerFrame();
    if (selectedRow < 0) {
      SwingUtil.showError(owner, "Please select a user to deactivate");
      return;
    }

    long userId;
    String username;
    try {
      userId = table.getLongValue(selectedRow, 0);
      username = table.getStringValue(selectedRow, 1);
    } catch (NumberFormatException e) {
      SwingUtil.showError(owner, "Invalid user selected");
      return;
    }
    if (userId <= 0) {
      SwingUtil.showError(owner, "Invalid user selected");
      return;
    }

    int confirm =
        JOptionPane.showConfirmDialog(
            owner,
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
                          SwingUtil.showInfo(owner, "User deactivated successfully");
                          loadUsers();
                        } else {
                          SwingUtil.showError(owner, response.getErrorMessage());
                        }
                      });
                } catch (Exception ex) {
                  SwingUtilities.invokeLater(() -> SwingUtil.showError(owner, ex.getMessage()));
                }
              })
          .start();
    }
  }

  private void resetPassword() {
    int selectedRow = table.resolveSelectedRowIndex();
    table.clearCapturedRowIndex();
    Frame owner = resolveOwnerFrame();
    if (selectedRow < 0) {
      SwingUtil.showError(owner, "Please select a user to reset password");
      return;
    }

    long userId;
    try {
      userId = table.getLongValue(selectedRow, 0);
    } catch (NumberFormatException e) {
      SwingUtil.showError(owner, "Invalid user selected");
      return;
    }
    if (userId <= 0) {
      SwingUtil.showError(owner, "Invalid user selected");
      return;
    }

    ResetPasswordDialog dialog = new ResetPasswordDialog(owner);
    dialog.show();

    if (dialog.isConfirmed()) {
      Map<String, Object> body = new HashMap<>();
      body.put("newPassword", dialog.getNewPassword());

      new Thread(
              () -> {
                try {
                  ApiClient.ApiResponse response = userApiClient.resetPassword(userId, body);
                  SwingUtilities.invokeLater(
                      () -> {
                        if (response.isSuccess()) {
                          SwingUtil.showInfo(owner, "Password reset successfully");
                        } else {
                          SwingUtil.showError(owner, response.getErrorMessage());
                        }
                      });
                } catch (Exception ex) {
                  SwingUtilities.invokeLater(() -> SwingUtil.showError(owner, ex.getMessage()));
                }
              })
          .start();
    }
  }
}
