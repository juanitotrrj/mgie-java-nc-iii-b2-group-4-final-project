package com.group4.bdd.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.group4.bdd.SharedTestContext;
import com.group4.bdd.TestContext;
import com.group4.bdd.steps.common.HttpSupport;
import com.group4.inventoryserver.testing.E2eHttpClient;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ProductSteps {

  private final TestContext context = SharedTestContext.get();

  @When("I list products with page {int} and page size {int}")
  public void listProducts(int page, int pageSize) throws Exception {
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(
        context, context.getHttpClient().get("/products?page=" + page + "&pageSize=" + pageSize));
  }

  @When("I search products with keyword {string}")
  public void searchProducts(String keyword) throws Exception {
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(
        context,
        context.getHttpClient().get("/products?page=1&pageSize=20&search=" + keyword));
  }

  @When("I filter products by status {string}")
  public void filterByStatus(String status) throws Exception {
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(
        context,
        context.getHttpClient().get("/products?page=1&pageSize=20&status=" + status));
  }

  @When("I filter products by category id {string}")
  public void filterByCategory(String categoryVar) throws Exception {
    long categoryId = context.getLongVar(categoryVar);
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(
        context,
        context
            .getHttpClient()
            .get("/products?page=1&pageSize=20&categoryId=" + categoryId));
  }

  @When("I export products as {string}")
  public void exportProducts(String format) throws Exception {
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().get("/products/export?format=" + format));
  }

  @When("I create a test product")
  public void createProduct() throws Exception {
    HttpSupport.startServerIfNeeded(context);
    long categoryId = firstCategoryId();
    String code = "BDD" + System.currentTimeMillis();
    JsonObject body = new JsonObject();
    body.addProperty("productCode", code);
    body.addProperty("productName", "BDD Test Product");
    body.addProperty("categoryId", categoryId);
    body.addProperty("quantity", 10);
    body.addProperty("reorderLevel", 2);
    body.addProperty("unitPrice", 150.0);
    body.addProperty("status", "In Stock");
    E2eHttpClient.HttpResult result = context.getHttpClient().post("/products", body.toString());
    HttpSupport.record(context, result);
    if (result.status == 201) {
      context.setVar("productId", result.jsonRoot().getAsJsonObject("data").get("productId").getAsLong());
      context.setVar("productCode", code);
      context.setVar("productQuantity", 10);
    }
  }

  @When("I get the stored product by id")
  public void getStoredProduct() throws Exception {
    long productId = context.getLongVar("productId");
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().get("/products/" + productId));
  }

  @When("I update the stored product name to {string}")
  public void updateStoredProduct(String name) throws Exception {
    long productId = context.getLongVar("productId");
    JsonObject body = new JsonObject();
    body.addProperty("productName", name);
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(
        context, context.getHttpClient().put("/products/" + productId, body.toString()));
  }

  @When("I delete the stored product")
  public void deleteStoredProduct() throws Exception {
    long productId = context.getLongVar("productId");
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().delete("/products/" + productId));
  }

  @When("I get product quantity for stored product")
  public void getProductQuantity() throws Exception {
    long productId = context.getLongVar("productId");
    HttpSupport.startServerIfNeeded(context);
    E2eHttpClient.HttpResult result = context.getHttpClient().get("/products/" + productId);
    HttpSupport.record(context, result);
    if (result.status == 200) {
      int qty = result.jsonRoot().getAsJsonObject("data").get("quantity").getAsInt();
      context.setVar("productQuantity", qty);
    }
  }

  @When("I store the first product id and quantity")
  public void storeFirstProduct() throws Exception {
    HttpSupport.startServerIfNeeded(context);
    E2eHttpClient.HttpResult result =
        context.getHttpClient().get("/products?page=1&pageSize=1");
    JsonObject product =
        result.jsonRoot().getAsJsonArray("data").get(0).getAsJsonObject();
    context.setVar("productId", product.get("productId").getAsLong());
    context.setVar("productQuantity", product.get("quantity").getAsInt());
    HttpSupport.record(context, result);
  }

  @Then("the stored product quantity is at least {int}")
  public void assertQuantityAtLeast(int min) {
    int qty = context.getIntVar("productQuantity");
    assertThat(qty >= min, is(true));
  }

  @Then("the stored product quantity increased by {int}")
  public void assertQuantityIncreased(int delta) {
    int before = context.getIntVar("quantityBefore");
    int after = context.getIntVar("productQuantity");
    assertThat(after, is(before + delta));
  }

  @Then("the stored product quantity decreased by {int}")
  public void assertQuantityDecreased(int delta) {
    int before = context.getIntVar("quantityBefore");
    int after = context.getIntVar("productQuantity");
    assertThat(after, is(before - delta));
  }

  @And("I remember the current product quantity")
  public void rememberQuantity() {
    context.setVar("quantityBefore", context.getIntVar("productQuantity"));
  }

  private long firstCategoryId() throws Exception {
    if (context.getVar("categoryId") != null) {
      return context.getLongVar("categoryId");
    }
    E2eHttpClient.HttpResult result =
        context.getHttpClient().get("/categories?page=1&pageSize=1");
    JsonArray data = result.jsonRoot().getAsJsonArray("data");
    return data.get(0).getAsJsonObject().get("categoryId").getAsLong();
  }
}
