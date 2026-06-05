package com.group4.bdd.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.group4.bdd.SharedTestContext;
import com.group4.bdd.TestContext;
import com.group4.bdd.steps.common.HttpSupport;
import com.group4.inventoryserver.testing.E2eHttpClient;
import com.group4.inventoryserver.testing.TestSetupBootstrap;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class SetupSteps {

  private final TestContext context = SharedTestContext.get();

  @When("I PUT business settings without setup token")
  public void putBusinessSettingsNoToken() throws Exception {
    HttpSupport.startServerIfNeeded(context);
    JsonObject body = new JsonObject();
    body.addProperty("companyName", "Unauthorized Co");
    body.addProperty("currencyCode", "PHP");
    body.addProperty("currencySymbol", "₱");
    body.addProperty("currencyName", "Philippine Peso");
    body.addProperty("taxRate", 12.0);
    body.addProperty("taxType", "VAT");
    body.addProperty("priceDecimalPlaces", 2);
    body.addProperty("timezone", "UTC");
    String baseUrl =
        System.getProperty("e2e.server.url", context.getServerLauncher().getBaseUrl());
    E2eHttpClient noTokenClient = new E2eHttpClient(baseUrl);
    HttpSupport.record(
        context, noTokenClient.put("/setup/business-settings", body.toString()));
  }

  @When("I seed default roles via setup API")
  public void seedRoles() throws Exception {
    HttpSupport.startServerIfNeeded(context);
    JsonObject body = new JsonObject();
    JsonArray roles = new JsonArray();
    roles.add("Administrator");
    roles.add("Manager");
    roles.add("Inventory Clerk");
    roles.add("Cashier");
    body.add("roles", roles);
    HttpSupport.record(context, context.getHttpClient().post("/setup/roles/seed", body.toString()));
  }

  @When("I seed default permissions via setup API")
  public void seedPermissions() throws Exception {
    HttpSupport.startServerIfNeeded(context);
    JsonObject body = new JsonObject();
    JsonObject rolePermissions = new JsonObject();
    JsonArray adminPerms = new JsonArray();
    adminPerms.add("AUTH_LOGIN");
    adminPerms.add("DASHBOARD_VIEW");
    adminPerms.add("PRODUCT_READ");
    adminPerms.add("SALE_WRITE");
    rolePermissions.add("Administrator", adminPerms);
    body.add("rolePermissions", rolePermissions);
    HttpSupport.record(
        context, context.getHttpClient().post("/setup/permissions/seed", body.toString()));
  }

  @When("I create setup admin user via setup API")
  public void createSetupUsers() throws Exception {
    HttpSupport.startServerIfNeeded(context);
    JsonObject user = new JsonObject();
    user.addProperty("username", "setupadmin");
    user.addProperty("password", "Setup@1234");
    user.addProperty("email", "setupadmin@bdd.test");
    user.addProperty("fullName", "Setup Admin");
    user.addProperty("role", "Administrator");
    JsonArray users = new JsonArray();
    users.add(user);
    JsonObject body = new JsonObject();
    body.add("users", users);
    HttpSupport.record(context, context.getHttpClient().post("/setup/users", body.toString()));
  }

  @When("I finish setup via setup API")
  public void finishSetup() throws Exception {
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().post("/setup/finish", "{}"));
  }

  @When("I complete the setup wizard via API")
  public void completeWizard() throws Exception {
    String token = TestSetupBootstrap.mintSetupToken();
    context.setSetupToken(token);
    HttpSupport.startServerIfNeeded(context);
    context.getHttpClient().setSetupToken(token);

    JsonObject settings = new JsonObject();
    settings.addProperty("companyName", "Wizard Test Co");
    settings.addProperty("currencyCode", "PHP");
    settings.addProperty("currencySymbol", "₱");
    settings.addProperty("currencyName", "Philippine Peso");
    settings.addProperty("taxRate", 12.0);
    settings.addProperty("taxType", "VAT");
    settings.addProperty("priceDecimalPlaces", 2);
    settings.addProperty("timezone", "UTC");
    HttpSupport.record(
        context, context.getHttpClient().put("/setup/business-settings", settings.toString()));

    JsonObject rolesBody = new JsonObject();
    JsonArray roles = new JsonArray();
    roles.add("Administrator");
    roles.add("Cashier");
    rolesBody.add("roles", roles);
    HttpSupport.record(
        context, context.getHttpClient().post("/setup/roles/seed", rolesBody.toString()));

    JsonObject permsBody = new JsonObject();
    JsonObject rolePermissions = new JsonObject();
    JsonArray adminPerms = new JsonArray();
    adminPerms.add("AUTH_LOGIN");
    adminPerms.add("DASHBOARD_VIEW");
    adminPerms.add("PRODUCT_READ");
    adminPerms.add("SALE_WRITE");
    adminPerms.add("SALE_READ");
    rolePermissions.add("Administrator", adminPerms);
    JsonArray cashierPerms = new JsonArray();
    cashierPerms.add("AUTH_LOGIN");
    cashierPerms.add("DASHBOARD_VIEW");
    cashierPerms.add("PRODUCT_READ");
    cashierPerms.add("SALE_WRITE");
    rolePermissions.add("Cashier", cashierPerms);
    permsBody.add("rolePermissions", rolePermissions);
    HttpSupport.record(
        context, context.getHttpClient().post("/setup/permissions/seed", permsBody.toString()));

    JsonObject user = new JsonObject();
    user.addProperty("username", "wizardadmin");
    user.addProperty("password", "Wizard@1234");
    user.addProperty("email", "wizardadmin@bdd.test");
    user.addProperty("fullName", "Wizard Admin");
    user.addProperty("role", "Administrator");
    JsonObject usersBody = new JsonObject();
    JsonArray users = new JsonArray();
    users.add(user);
    usersBody.add("users", users);
    HttpSupport.record(context, context.getHttpClient().post("/setup/users", usersBody.toString()));
    HttpSupport.record(context, context.getHttpClient().post("/setup/finish", "{}"));
    context.setVar("wizardAdminUser", "wizardadmin");
    context.setVar("wizardAdminPassword", "Wizard@1234");
  }

  @Then("the setup state is {string}")
  public void assertSetupState(String expected) {
    JsonObject data = HttpSupport.dataObject(context);
    assertThat(data.get("state").getAsString(), is(expected));
  }

  @And("I mint and apply a setup token")
  public void mintAndApplyToken() {
    String token = TestSetupBootstrap.mintSetupToken();
    context.setSetupToken(token);
    if (context.getHttpClient() != null) {
      context.getHttpClient().setSetupToken(token);
    }
  }
}
