package com.group4.inventoryserver.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.gson.JsonObject;
import com.group4.inventoryserver.testing.E2eHttpClient;
import org.junit.Test;

public class ProductsApiIT extends ApiITBase {

  @Test
  public void list_products_returns_200() throws Exception {
    assertThat(adminClient.get("/products?page=1&pageSize=10").status, is(200));
  }

  @Test
  public void export_products_returns_csv() throws Exception {
    E2eHttpClient.HttpResult result = adminClient.get("/products/export?format=csv");
    assertThat(result.status, is(200));
    assertThat(result.body.contains("Product Code") || result.body.length() > 0, is(true));
  }

  @Test
  public void create_get_update_and_delete_product() throws Exception {
    JsonObject list = adminClient.get("/categories?page=1&pageSize=1").jsonRoot();
    long categoryId =
        list.getAsJsonArray("data").get(0).getAsJsonObject().get("categoryId").getAsLong();

    JsonObject createBody = new JsonObject();
    String code = "APIP" + System.currentTimeMillis();
    createBody.addProperty("productCode", code);
    createBody.addProperty("productName", "API Test Product");
    createBody.addProperty("categoryId", categoryId);
    createBody.addProperty("quantity", 10);
    createBody.addProperty("reorderLevel", 2);
    createBody.addProperty("unitPrice", 150.0);
    createBody.addProperty("status", "In Stock");

    E2eHttpClient.HttpResult created = adminClient.post("/products", createBody.toString());
    assertThat(created.status, is(201));
    long productId = created.jsonRoot().getAsJsonObject("data").get("productId").getAsLong();

    E2eHttpClient.HttpResult fetched = adminClient.get("/products/" + productId);
    assertThat(fetched.status, is(200));
    assertThat(
        fetched.jsonRoot().getAsJsonObject("data").get("productCode").getAsString(), is(code));

    JsonObject updateBody = new JsonObject();
    updateBody.addProperty("productName", "API Test Product Updated");
    assertThat(adminClient.put("/products/" + productId, updateBody.toString()).status, is(200));

    assertThat(adminClient.delete("/products/" + productId).status, is(200));
  }
}
