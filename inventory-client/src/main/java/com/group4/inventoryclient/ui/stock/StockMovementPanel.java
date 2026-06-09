package com.group4.inventoryclient.ui.stock;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.group4.inventoryclient.api.ApiClient;
import com.group4.inventoryclient.api.StockMovementApiClient;
import com.group4.inventoryclient.ui.components.PaginatedTable;
import com.group4.inventoryclient.ui.components.SearchFilterBar;
import com.group4.inventoryclient.util.JsonFieldUtil;
import com.group4.inventoryclient.util.PaginationUtil;
import com.group4.inventoryclient.util.SwingUtil;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class StockMovementPanel extends JPanel {

  private final StockMovementApiClient stockMovementApi;
  private final SearchFilterBar searchBar = new SearchFilterBar();
  private final PaginatedTable table =
      new PaginatedTable(new String[] {"ID", "Product", "Type", "Quantity", "Reference", "Date"});

  public StockMovementPanel(ApiClient apiClient) {
    super(new BorderLayout(5, 5));
    this.stockMovementApi = new StockMovementApiClient(apiClient);

    searchBar.addFilter(
        "Movement Type", new String[] {"All", "Stock In", "Stock Out", "Adjustment"});
    searchBar.setSearchListener(q -> loadData(1));
    add(searchBar, BorderLayout.NORTH);

    add(table, BorderLayout.CENTER);

    table.setPageChangeListener(this::loadData);

    loadData(1);
  }

  private void loadData(int page) {
    new Thread(
            () -> {
              try {
                String search = searchBar.getSearchText();
                String movementType = searchBar.getFilterValue(0);
                ApiClient.ApiResponse response =
                    stockMovementApi.list(page, search, movementType, null, null);
                if (response.isSuccess()) {
                  JsonArray data = response.getDataAsArray();
                  JsonObject meta = response.getMeta();
                  int totalPages = PaginationUtil.totalPages(meta);

                  Object[][] rows = new Object[data.size()][6];
                  for (int i = 0; i < data.size(); i++) {
                    JsonObject item = data.get(i).getAsJsonObject();
                    rows[i][0] = JsonFieldUtil.getLong(item, "stockMovementId", "id");
                    rows[i][1] = JsonFieldUtil.getString(item, "N/A", "productName");
                    if ("N/A".equals(rows[i][1])
                        && item.has("product")
                        && item.get("product").isJsonObject()) {
                      rows[i][1] =
                          JsonFieldUtil.getString(item.getAsJsonObject("product"), "N/A", "name");
                    }
                    rows[i][2] = JsonFieldUtil.getString(item, "N/A", "movementType");
                    rows[i][3] = JsonFieldUtil.getInt(item, "quantityChange", "quantity");
                    String referenceNo = JsonFieldUtil.getString(item, "", "referenceNo");
                    String referenceType = JsonFieldUtil.getString(item, "", "referenceType");
                    if (!referenceNo.isEmpty()) {
                      rows[i][4] = referenceType + " " + referenceNo;
                    } else if (!referenceType.isEmpty()) {
                      long referenceId = JsonFieldUtil.getLong(item, "referenceId");
                      rows[i][4] = referenceType + " #" + referenceId;
                    } else {
                      rows[i][4] = "N/A";
                    }
                    rows[i][5] = JsonFieldUtil.getString(item, "N/A", "createdAt", "movementDate");
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
}
