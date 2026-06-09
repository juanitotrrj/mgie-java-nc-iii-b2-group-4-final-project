package com.group4.bdd.steps;

import com.google.gson.JsonObject;
import com.group4.bdd.SharedTestContext;
import com.group4.bdd.TestContext;
import com.group4.bdd.steps.common.HttpSupport;
import io.cucumber.java.en.When;

public class CategorySteps {

  private final TestContext context = SharedTestContext.get();

  @When("I list categories")
  public void listCategories() throws Exception {
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().get("/categories?page=1&pageSize=10"));
  }

  @When("I create a test category")
  public void createCategory() throws Exception {
    HttpSupport.startServerIfNeeded(context);
    String name = "BDD Category " + System.currentTimeMillis();
    JsonObject body = new JsonObject();
    body.addProperty("categoryName", name);
    body.addProperty("description", "BDD category");
    body.addProperty("type", "Product Group");
    body.addProperty("status", "Active");
    HttpSupport.record(context, context.getHttpClient().post("/categories", body.toString()));
    if (context.getLastStatus() == 201) {
      context.setVar(
          "categoryId",
          HttpSupport.dataObject(context).get("categoryId").getAsLong());
      context.setVar("categoryName", name);
    }
  }

  @When("I get the stored category by id")
  public void getCategory() throws Exception {
    long categoryId = context.getLongVar("categoryId");
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().get("/categories/" + categoryId));
  }

  @When("I update the stored category description")
  public void updateCategory() throws Exception {
    long categoryId = context.getLongVar("categoryId");
    JsonObject body = new JsonObject();
    body.addProperty("description", "Updated by BDD");
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(
        context, context.getHttpClient().put("/categories/" + categoryId, body.toString()));
  }

  @When("I delete the stored category")
  public void deleteCategory() throws Exception {
    long categoryId = context.getLongVar("categoryId");
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().delete("/categories/" + categoryId));
  }
}
