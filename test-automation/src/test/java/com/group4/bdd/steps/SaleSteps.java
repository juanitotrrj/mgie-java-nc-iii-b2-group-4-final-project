package com.group4.bdd.steps;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.group4.bdd.SharedTestContext;
import com.group4.bdd.TestContext;
import com.group4.bdd.steps.common.HttpSupport;
import com.group4.inventoryserver.testing.E2eHttpClient;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class SaleSteps {

  private final TestContext context = SharedTestContext.get();

  @Given("a cashier user exists and is logged in")
  public void cashierLoggedIn() throws Exception {
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.login(context, "admin", "Administrator", "Admin@123");

    String username = "cashier" + System.currentTimeMillis();
    JsonObject body = new JsonObject();
    body.addProperty("fullName", "BDD Cashier");
    body.addProperty("username", username);
    body.addProperty("email", username + "@bdd.test");
    body.addProperty("role", "Cashier");
    body.addProperty("password", "Cashier@1234");
    body.addProperty("status", "Active");
    context.getHttpClient().post("/users", body.toString());

    context.setHttpClient(
        HttpSupport.authenticatedClient(context, username, "Cashier", "Cashier@1234"));
    context.setVar("cashierUsername", username);
  }

  @When("I list sales")
  public void listSales() throws Exception {
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().get("/sales?page=1&pageSize=10"));
  }

  @When("I create a POS sale for stored product with quantity {int}")
  public void createSale(int quantity) throws Exception {
    HttpSupport.startServerIfNeeded(context);
    long productId = context.getLongVar("productId");

    JsonObject item = new JsonObject();
    item.addProperty("productId", productId);
    item.addProperty("quantity", quantity);
    JsonArray items = new JsonArray();
    items.add(item);

    JsonObject body = new JsonObject();
    body.addProperty("customerName", "BDD Walk-in");
    body.addProperty("paymentMethod", "Cash");
    body.addProperty("amountReceived", 5000.0);
    body.addProperty("status", "Completed");
    body.add("items", items);

    HttpSupport.record(context, context.getHttpClient().post("/sales", body.toString()));
    if (context.getLastStatus() == 201) {
      context.setVar("saleId", HttpSupport.dataObject(context).get("saleId").getAsLong());
      context.setVar("saleQuantity", quantity);
    }
  }

  @When("I get receipt for stored sale")
  public void getReceipt() throws Exception {
    long saleId = context.getLongVar("saleId");
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().get("/sales/" + saleId + "/receipt"));
  }

  @When("I cancel the stored sale with reason {string}")
  public void cancelSale(String reason) throws Exception {
    long saleId = context.getLongVar("saleId");
    JsonObject body = new JsonObject();
    body.addProperty("reason", reason);
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(
        context,
        context.getHttpClient().post("/sales/" + saleId + "/cancel", body.toString()));
  }
}
