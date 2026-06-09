package com.group4.bdd.steps;

import com.google.gson.JsonObject;
import com.group4.bdd.SharedTestContext;
import com.group4.bdd.TestContext;
import com.group4.bdd.steps.common.HttpSupport;
import io.cucumber.java.en.When;

public class SupplierSteps {

  private final TestContext context = SharedTestContext.get();

  @When("I list suppliers")
  public void listSuppliers() throws Exception {
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().get("/suppliers?page=1&pageSize=10"));
  }

  @When("I create a test supplier")
  public void createSupplier() throws Exception {
    HttpSupport.startServerIfNeeded(context);
    String name = "BDD Supplier " + System.currentTimeMillis();
    JsonObject body = new JsonObject();
    body.addProperty("supplierName", name);
    body.addProperty("contactPerson", "BDD Contact");
    body.addProperty("phone", "555-0100");
    body.addProperty("email", "supplier-bdd@test.local");
    body.addProperty("address", "BDD Address");
    body.addProperty("type", "Local");
    body.addProperty("preferred", false);
    body.addProperty("status", "Active");
    HttpSupport.record(context, context.getHttpClient().post("/suppliers", body.toString()));
    if (context.getLastStatus() == 201) {
      context.setVar(
          "supplierId",
          HttpSupport.dataObject(context).get("supplierId").getAsLong());
    }
  }

  @When("I get the stored supplier by id")
  public void getSupplier() throws Exception {
    long supplierId = context.getLongVar("supplierId");
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().get("/suppliers/" + supplierId));
  }

  @When("I update the stored supplier phone")
  public void updateSupplier() throws Exception {
    long supplierId = context.getLongVar("supplierId");
    JsonObject body = new JsonObject();
    body.addProperty("phone", "555-9999");
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(
        context, context.getHttpClient().put("/suppliers/" + supplierId, body.toString()));
  }

  @When("I delete the stored supplier")
  public void deleteSupplier() throws Exception {
    long supplierId = context.getLongVar("supplierId");
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().delete("/suppliers/" + supplierId));
  }

  @When("I store the first supplier id")
  public void storeFirstSupplier() throws Exception {
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().get("/suppliers?page=1&pageSize=1"));
    context.setVar(
        "supplierId",
        HttpSupport.parseBody(context)
            .getAsJsonArray("data")
            .get(0)
            .getAsJsonObject()
            .get("supplierId")
            .getAsLong());
  }
}
