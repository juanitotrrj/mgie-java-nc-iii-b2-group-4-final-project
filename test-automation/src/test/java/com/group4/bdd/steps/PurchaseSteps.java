package com.group4.bdd.steps;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.group4.bdd.SharedTestContext;
import com.group4.bdd.TestContext;
import com.group4.bdd.steps.common.HttpSupport;
import io.cucumber.java.en.When;
import java.time.LocalDate;

public class PurchaseSteps {

  private final TestContext context = SharedTestContext.get();

  @When("I list purchase orders")
  public void listPurchases() throws Exception {
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().get("/purchases?page=1&pageSize=10"));
  }

  @When("I create a purchase order for stored product with quantity {int}")
  public void createPurchase(int quantity) throws Exception {
    HttpSupport.startServerIfNeeded(context);
    long supplierId = context.getLongVar("supplierId");
    long productId = context.getLongVar("productId");

    JsonObject item = new JsonObject();
    item.addProperty("productId", productId);
    item.addProperty("quantity", quantity);
    item.addProperty("unitCost", 100.0);
    JsonArray items = new JsonArray();
    items.add(item);

    JsonObject body = new JsonObject();
    body.addProperty("supplierId", supplierId);
    body.addProperty("orderDate", LocalDate.now().toString());
    body.addProperty("expectedDeliveryDate", LocalDate.now().plusDays(7).toString());
    body.addProperty("notes", "BDD purchase");
    body.add("items", items);

    HttpSupport.record(context, context.getHttpClient().post("/purchases", body.toString()));
    if (context.getLastStatus() == 201) {
      context.setVar(
          "purchaseId",
          HttpSupport.dataObject(context).get("purchaseId").getAsLong());
      context.setVar("purchaseQuantity", quantity);
    }
  }

  @When("I get the stored purchase order by id")
  public void getPurchase() throws Exception {
    long purchaseId = context.getLongVar("purchaseId");
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().get("/purchases/" + purchaseId));
  }

  @When("I receive the stored purchase order")
  public void receivePurchase() throws Exception {
    long purchaseId = context.getLongVar("purchaseId");
    JsonObject body = new JsonObject();
    body.addProperty("receivedDate", LocalDate.now().toString());
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(
        context,
        context.getHttpClient().post("/purchases/" + purchaseId + "/receive", body.toString()));
  }

  @When("I cancel the stored purchase order with reason {string}")
  public void cancelPurchase(String reason) throws Exception {
    long purchaseId = context.getLongVar("purchaseId");
    JsonObject body = new JsonObject();
    body.addProperty("reason", reason);
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(
        context,
        context.getHttpClient().post("/purchases/" + purchaseId + "/cancel", body.toString()));
  }
}
