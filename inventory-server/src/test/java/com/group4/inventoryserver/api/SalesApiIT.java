package com.group4.inventoryserver.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.group4.inventoryserver.testing.E2eHttpClient;
import org.junit.Test;

public class SalesApiIT extends ApiITBase {

  @Test
  public void list_sales_returns_200() throws Exception {
    assertThat(adminClient.get("/sales?page=1&pageSize=10").status, is(200));
  }

  @Test
  public void create_sale_and_get_receipt() throws Exception {
    long productId =
        adminClient
            .get("/products?page=1&pageSize=1")
            .jsonRoot()
            .getAsJsonArray("data")
            .get(0)
            .getAsJsonObject()
            .get("productId")
            .getAsLong();

    JsonObject item = new JsonObject();
    item.addProperty("productId", productId);
    item.addProperty("quantity", 1);

    JsonArray items = new JsonArray();
    items.add(item);

    JsonObject createBody = new JsonObject();
    createBody.addProperty("customerName", "API Walk-in");
    createBody.addProperty("paymentMethod", "Cash");
    createBody.addProperty("amountReceived", 1000.0);
    createBody.addProperty("status", "Completed");
    createBody.add("items", items);

    E2eHttpClient.HttpResult created = adminClient.post("/sales", createBody.toString());
    assertThat(created.status, is(201));
    long saleId = created.jsonRoot().getAsJsonObject("data").get("saleId").getAsLong();

    E2eHttpClient.HttpResult receipt = adminClient.get("/sales/" + saleId + "/receipt");
    assertThat(receipt.status, is(200));
    assertThat(receipt.jsonRoot().getAsJsonObject("data").has("invoiceNo"), is(true));
  }
}
