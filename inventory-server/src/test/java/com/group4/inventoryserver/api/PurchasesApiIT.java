package com.group4.inventoryserver.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.group4.inventoryserver.testing.E2eHttpClient;
import java.time.LocalDate;
import org.junit.Test;

public class PurchasesApiIT extends ApiITBase {

  @Test
  public void list_purchases_returns_200() throws Exception {
    assertThat(adminClient.get("/purchases?page=1&pageSize=10").status, is(200));
  }

  @Test
  public void create_and_receive_purchase_order() throws Exception {
    long supplierId =
        adminClient
            .get("/suppliers?page=1&pageSize=1")
            .jsonRoot()
            .getAsJsonArray("data")
            .get(0)
            .getAsJsonObject()
            .get("supplierId")
            .getAsLong();
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
    item.addProperty("quantity", 3);
    item.addProperty("unitCost", 100.0);

    JsonArray items = new JsonArray();
    items.add(item);

    JsonObject createBody = new JsonObject();
    createBody.addProperty("supplierId", supplierId);
    createBody.addProperty("orderDate", LocalDate.now().toString());
    createBody.addProperty("expectedDeliveryDate", LocalDate.now().plusDays(7).toString());
    createBody.addProperty("notes", "API IT purchase");
    createBody.add("items", items);

    E2eHttpClient.HttpResult created = adminClient.post("/purchases", createBody.toString());
    assertThat(created.status, is(201));
    long purchaseId = created.jsonRoot().getAsJsonObject("data").get("purchaseId").getAsLong();

    JsonObject receiveBody = new JsonObject();
    receiveBody.addProperty("receivedDate", LocalDate.now().toString());
    assertThat(
        adminClient.post("/purchases/" + purchaseId + "/receive", receiveBody.toString()).status,
        is(200));
  }
}
