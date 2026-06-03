package com.group4.inventoryclient.ui.audit;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.group4.inventoryclient.api.ApiClient;
import com.group4.inventoryclient.api.AuditApiClient;
import com.group4.inventoryclient.ui.components.PaginatedTable;
import com.group4.inventoryclient.ui.components.SearchFilterBar;
import com.group4.inventoryclient.util.SwingUtil;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class AuditLogPanel extends JPanel {

  private final AuditApiClient auditApiClient;
  private final SearchFilterBar searchBar;
  private final PaginatedTable table;

  public AuditLogPanel(ApiClient apiClient) {
    super(new BorderLayout(10, 10));
    this.auditApiClient = new AuditApiClient(apiClient);

    searchBar = new SearchFilterBar();
    searchBar.addFilter(
        "Action", new String[] {"All", "CREATE", "UPDATE", "DELETE", "LOGIN", "LOGOUT"});
    searchBar.addFilter(
        "Module",
        new String[] {
          "All", "Products", "Categories", "Suppliers", "Purchases", "Sales", "Users", "Settings"
        });
    searchBar.setSearchListener(query -> loadAuditLogs());

    table =
        new PaginatedTable(
            new String[] {"ID", "User", "Action", "Module", "Description", "Timestamp"});
    table.setPageChangeListener(page -> loadAuditLogs());

    add(searchBar, BorderLayout.NORTH);
    add(table, BorderLayout.CENTER);

    loadAuditLogs();
  }

  private void loadAuditLogs() {
    int page = table.getCurrentPage();
    String user = searchBar.getSearchText();
    String action = searchBar.getFilterValue(0);
    String module = searchBar.getFilterValue(1);

    new Thread(
            () -> {
              try {
                ApiClient.ApiResponse response =
                    auditApiClient.list(page, user, action, module, null, null);
                SwingUtilities.invokeLater(
                    () -> {
                      if (response.isSuccess()) {
                        JsonArray logs = response.getDataAsArray();
                        JsonObject meta = response.getMeta();
                        int totalPages =
                            meta != null && meta.has("total_pages")
                                ? meta.get("total_pages").getAsInt()
                                : 1;

                        Object[][] rows = new Object[logs.size()][6];
                        for (int i = 0; i < logs.size(); i++) {
                          JsonObject log = logs.get(i).getAsJsonObject();
                          rows[i][0] = log.has("id") ? log.get("id").getAsLong() : 0;
                          rows[i][1] = log.has("user") ? log.get("user").getAsString() : "";
                          rows[i][2] = log.has("action") ? log.get("action").getAsString() : "";
                          rows[i][3] = log.has("module") ? log.get("module").getAsString() : "";
                          rows[i][4] =
                              log.has("description") ? log.get("description").getAsString() : "";
                          rows[i][5] =
                              log.has("timestamp") ? log.get("timestamp").getAsString() : "";
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
}
