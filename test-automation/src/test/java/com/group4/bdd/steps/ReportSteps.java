package com.group4.bdd.steps;

import com.group4.bdd.SharedTestContext;
import com.group4.bdd.TestContext;
import com.group4.bdd.steps.common.HttpSupport;
import com.group4.inventoryserver.testing.E2eHttpClient;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class ReportSteps {

  private final TestContext context = SharedTestContext.get();

  @When("I get report options")
  public void reportOptions() throws Exception {
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().get("/reports/options"));
  }

  @When("I generate the sales summary report")
  public void salesSummary() throws Exception {
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().get("/reports/sales-summary"));
  }

  @When("I generate the inventory value report")
  public void inventoryValue() throws Exception {
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().get("/reports/inventory-value"));
  }

  @When("I generate the low stock report")
  public void lowStock() throws Exception {
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().get("/reports/low-stock"));
  }

  @When("I list audit logs")
  public void listAuditLogs() throws Exception {
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().get("/audit-logs?page=1&pageSize=10"));
  }

  @When("I list stock movements")
  public void listStockMovements() throws Exception {
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().get("/stock-movements?page=1&pageSize=20"));
  }

  @Given("I am logged in as {word} role user")
  public void loginAsRoleUser(String roleTag) throws Exception {
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.login(context, "admin", "Administrator", "Admin@123");

    String roleName = mapRole(roleTag);
    String username = roleTag + System.currentTimeMillis();
    com.google.gson.JsonObject body = new com.google.gson.JsonObject();
    body.addProperty("fullName", "BDD " + roleName);
    body.addProperty("username", username);
    body.addProperty("email", username + "@bdd.test");
    body.addProperty("role", roleName);
    body.addProperty("password", "Role@1234");
    body.addProperty("status", "Active");
    context.getHttpClient().post("/users", body.toString());

    context.setHttpClient(
        HttpSupport.authenticatedClient(context, username, roleName, "Role@1234"));
    context.setVar("roleUsername", username);
  }

  @When("I open the {word} dashboard")
  public void openDashboard(String roleTag) throws Exception {
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().get("/dashboard/" + roleTag));
  }

  private static String mapRole(String roleTag) {
    switch (roleTag) {
      case "admin":
        return "Administrator";
      case "manager":
        return "Manager";
      case "clerk":
        return "Inventory Clerk";
      case "cashier":
        return "Cashier";
      default:
        return "Administrator";
    }
  }
}
