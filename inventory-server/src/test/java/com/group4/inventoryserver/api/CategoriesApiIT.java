package com.group4.inventoryserver.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.gson.JsonObject;
import com.group4.inventoryserver.testing.E2eHttpClient;
import org.junit.Test;

public class CategoriesApiIT extends ApiITBase {

  @Test
  public void list_categories_returns_200() throws Exception {
    assertThat(adminClient.get("/categories?page=1&pageSize=10").status, is(200));
  }

  @Test
  public void create_get_update_delete_and_export_category() throws Exception {
    JsonObject createBody = new JsonObject();
    String name = "API Category " + System.currentTimeMillis();
    createBody.addProperty("categoryName", name);
    createBody.addProperty("description", "API IT category");
    createBody.addProperty("type", "Product Group");
    createBody.addProperty("status", "Active");

    E2eHttpClient.HttpResult created = adminClient.post("/categories", createBody.toString());
    assertThat(created.status, is(201));
    long categoryId = created.jsonRoot().getAsJsonObject("data").get("categoryId").getAsLong();

    assertThat(adminClient.get("/categories/" + categoryId).status, is(200));

    JsonObject updateBody = new JsonObject();
    updateBody.addProperty("categoryName", name + " Updated");
    assertThat(adminClient.put("/categories/" + categoryId, updateBody.toString()).status, is(200));

    E2eHttpClient.HttpResult exported = adminClient.get("/categories/export?format=csv");
    assertThat(exported.status, is(200));
    assertThat(exported.body.length() > 0, is(true));

    assertThat(adminClient.delete("/categories/" + categoryId).status, is(200));
  }
}
