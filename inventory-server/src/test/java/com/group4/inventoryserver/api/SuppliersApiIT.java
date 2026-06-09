package com.group4.inventoryserver.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.gson.JsonObject;
import com.group4.inventoryserver.testing.E2eHttpClient;
import org.junit.Test;

public class SuppliersApiIT extends ApiITBase {

  @Test
  public void list_suppliers_returns_200() throws Exception {
    assertThat(adminClient.get("/suppliers?page=1&pageSize=10").status, is(200));
  }

  @Test
  public void create_get_update_delete_and_export_supplier() throws Exception {
    JsonObject createBody = new JsonObject();
    String name = "API Supplier " + System.currentTimeMillis();
    createBody.addProperty("supplierName", name);
    createBody.addProperty("contactPerson", "IT Contact");
    createBody.addProperty("phone", "555-0199");
    createBody.addProperty("email", "supplier-it@test.local");
    createBody.addProperty("address", "Test Address");
    createBody.addProperty("type", "Local");
    createBody.addProperty("preferred", false);
    createBody.addProperty("status", "Active");

    E2eHttpClient.HttpResult created = adminClient.post("/suppliers", createBody.toString());
    assertThat(created.status, is(201));
    long supplierId = created.jsonRoot().getAsJsonObject("data").get("supplierId").getAsLong();

    assertThat(adminClient.get("/suppliers/" + supplierId).status, is(200));

    JsonObject updateBody = new JsonObject();
    updateBody.addProperty("supplierName", name + " Updated");
    assertThat(adminClient.put("/suppliers/" + supplierId, updateBody.toString()).status, is(200));

    E2eHttpClient.HttpResult exported = adminClient.get("/suppliers/export?format=csv");
    assertThat(exported.status, is(200));
    assertThat(exported.body.length() > 0, is(true));

    assertThat(adminClient.delete("/suppliers/" + supplierId).status, is(200));
  }
}
