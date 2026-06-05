package com.group4.bdd.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.gson.JsonObject;
import com.group4.bdd.SharedTestContext;
import com.group4.bdd.TestContext;
import com.group4.bdd.steps.common.HttpSupport;
import com.group4.inventoryserver.testing.E2eHttpClient;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.LinkedHashMap;
import java.util.Map;

public class IcrSteps {

  private final TestContext context = SharedTestContext.get();

  @Given("an inventory clerk is logged in")
  public void clerkLoggedIn() throws Exception {
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.login(context, "admin", "Administrator", "Admin@123");

    String username = "clerk" + System.currentTimeMillis();
    JsonObject body = new JsonObject();
    body.addProperty("fullName", "BDD Clerk");
    body.addProperty("username", username);
    body.addProperty("email", username + "@bdd.test");
    body.addProperty("role", "Inventory Clerk");
    body.addProperty("password", "Clerk@1234");
    body.addProperty("status", "Active");
    context.getHttpClient().post("/users", body.toString());

    context.setHttpClient(
        HttpSupport.authenticatedClient(context, username, "Inventory Clerk", "Clerk@1234"));
  }

  @Given("a manager is logged in")
  public void managerLoggedIn() throws Exception {
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.login(context, "admin", "Administrator", "Admin@123");

    String username = "manager" + System.currentTimeMillis();
    JsonObject body = new JsonObject();
    body.addProperty("fullName", "BDD Manager");
    body.addProperty("username", username);
    body.addProperty("email", username + "@bdd.test");
    body.addProperty("role", "Manager");
    body.addProperty("password", "Manager@1234");
    body.addProperty("status", "Active");
    context.getHttpClient().post("/users", body.toString());

    context.setHttpClient(
        HttpSupport.authenticatedClient(context, username, "Manager", "Manager@1234"));
  }

  @When("I list inventory change requests")
  public void listIcrs() throws Exception {
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(
        context,
        context.getHttpClient().get("/inventory-change-requests?page=1&pageSize=10"));
  }

  @When("I submit an inventory adjustment for stored product to quantity {int}")
  public void submitAdjustment(int requestedQty) throws Exception {
    HttpSupport.startServerIfNeeded(context);
    long productId = context.getLongVar("productId");
    Map<String, String> fields = new LinkedHashMap<>();
    fields.put("productId", String.valueOf(productId));
    fields.put("requestType", "Adjustment");
    fields.put("requestedQuantity", String.valueOf(requestedQty));
    fields.put("reason", "BDD stock adjustment");
    HttpSupport.postMultipart(context, "/inventory-change-requests", fields);
    if (context.getLastStatus() == 201) {
      context.setVar("icrId", HttpSupport.dataObject(context).get("requestId").getAsLong());
      context.setVar("icrRequestedQuantity", requestedQty);
    }
  }

  @When("I approve the stored inventory change request")
  public void approveIcr() throws Exception {
    long icrId = context.getLongVar("icrId");
    JsonObject body = new JsonObject();
    body.addProperty("notes", "Approved by BDD");
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(
        context,
        context
            .getHttpClient()
            .post("/inventory-change-requests/" + icrId + "/approve", body.toString()));
  }

  @When("I reject the stored inventory change request with reason {string}")
  public void rejectIcr(String reason) throws Exception {
    long icrId = context.getLongVar("icrId");
    JsonObject body = new JsonObject();
    body.addProperty("reason", reason);
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(
        context,
        context
            .getHttpClient()
            .post("/inventory-change-requests/" + icrId + "/reject", body.toString()));
  }

  @When("I get my inventory change request summary")
  public void mySummary() throws Exception {
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().get("/inventory-change-requests/my-summary"));
  }

  @Then("the product quantity equals {int}")
  public void assertProductQuantity(int expected) throws Exception {
    long productId = context.getLongVar("productId");
    HttpSupport.startServerIfNeeded(context);
    E2eHttpClient.HttpResult result = context.getHttpClient().get("/products/" + productId);
    int actual = result.jsonRoot().getAsJsonObject("data").get("quantity").getAsInt();
    assertThat(actual, is(expected));
    context.setVar("productQuantity", actual);
  }
}
