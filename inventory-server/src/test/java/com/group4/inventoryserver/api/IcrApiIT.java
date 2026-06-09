package com.group4.inventoryserver.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.group4.inventoryserver.testing.E2eHttpClient;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;

public class IcrApiIT extends ApiITBase {

  @Test
  public void list_inventory_change_requests_returns_200() throws Exception {
    assertThat(adminClient.get("/inventory-change-requests?page=1&pageSize=10").status, is(200));
  }

  @Test
  public void create_inventory_change_request_returns_201() throws Exception {
    long productId =
        adminClient
            .get("/products?page=1&pageSize=1")
            .jsonRoot()
            .getAsJsonArray("data")
            .get(0)
            .getAsJsonObject()
            .get("productId")
            .getAsLong();

    Map<String, String> fields = new LinkedHashMap<>();
    fields.put("productId", String.valueOf(productId));
    fields.put("requestType", "Adjustment");
    fields.put("requestedQuantity", "15");
    fields.put("reason", "API integration test adjustment");

    E2eHttpClient.HttpResult result =
        adminClient.postMultipart("/inventory-change-requests", fields);
    assertThat(result.status, is(201));
    assertThat(result.jsonRoot().getAsJsonObject("data").has("requestNo"), is(true));
  }
}
