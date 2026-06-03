package com.group4.inventoryclient.ui.stock;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.group4.inventoryclient.api.ApiClient;
import com.group4.inventoryclient.api.StockMovementApiClient;
import com.group4.inventoryclient.ui.components.PaginatedTable;
import com.group4.inventoryclient.ui.components.SearchFilterBar;
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
                  int totalPages = meta != null ? meta.get("lastPage").getAsInt() : 1;

                  Object[][] rows = new Object[data.size()][6];
                  for (int i = 0; i < data.size(); i++) {
                    JsonObject item = data.get(i).getAsJsonObject();
                    rows[i][0] = item.get("id").getAsLong();
                    rows[i][1] =
                        item.has("product") && !item.get("product").isJsonNull()
                            ? item.getAsJsonObject("product").get("name").getAsString()
                            : "N/A";
                    rows[i][2] =
                        item.has("movementType") && !item.get("movementType").isJsonNull()
                            ? item.get("movementType").getAsString()
                            : "N/A";
                    rows[i][3] =
                        item.has("quantity") && !item.get("quantity").isJsonNull()
                            ? item.get("quantity").getAsInt()
                            : 0;
                    rows[i][4] =
                        item.has("referenceType") && !item.get("referenceType").isJsonNull()
                            ? item.get("referenceType").getAsString()
                                + " #"
                                + (item.has("referenceId") && !item.get("referenceId").isJsonNull()
                                    ? item.get("referenceId").getAsLong()
                                    : "N/A")
                            : "N/A";
                    rows[i][5] =
                        item.has("movementDate") && !item.get("movementDate").isJsonNull()
                            ? item.get("movementDate").getAsString()
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
}
