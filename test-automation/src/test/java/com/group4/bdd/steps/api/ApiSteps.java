package com.group4.bdd.steps.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.google.gson.JsonObject;
import com.group4.bdd.SharedTestContext;
import com.group4.bdd.TestContext;
import com.group4.bdd.steps.common.HttpSupport;
import com.group4.inventoryserver.testing.E2eHttpClient;
import com.group4.inventoryserver.testing.TestSetupBootstrap;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class ApiSteps {

  private final TestContext context = SharedTestContext.get();

  @Given("the API server is running with INFRA_READY fixture")
  public void apiServerInfraReady() throws Exception {
    context.setFixture("infra_ready");
    HttpSupport.startServerIfNeeded(context);
  }

  @And("I have a minted setup token")
  public void mintToken() throws Exception {
    String token = TestSetupBootstrap.mintSetupToken();
    context.setSetupToken(token);
    HttpSupport.startServerIfNeeded(context);
    context.getHttpClient().setSetupToken(token);
  }

  @When("I PUT business settings via setup API")
  public void putBusinessSettings() throws Exception {
    HttpSupport.startServerIfNeeded(context);
    JsonObject body = new JsonObject();
    body.addProperty("companyName", "BDD Test Company");
    body.addProperty("currencyCode", "PHP");
    body.addProperty("currencySymbol", "₱");
    body.addProperty("currencyName", "Philippine Peso");
    body.addProperty("taxRate", 12.0);
    body.addProperty("taxType", "VAT");
    body.addProperty("priceDecimalPlaces", 2);
    body.addProperty("timezone", "UTC");
    E2eHttpClient.HttpResult result =
        context.getHttpClient().put("/setup/business-settings", body.toString());
    HttpSupport.record(context, result);
  }

  @When("I POST a guest inquiry")
  public void postGuestInquiry() throws Exception {
    HttpSupport.startServerIfNeeded(context);
    JsonObject body = new JsonObject();
    body.addProperty("name", "BDD Guest");
    body.addProperty("email", "bdd@example.com");
    body.addProperty("subject", "Hello");
    body.addProperty("message", "From Cucumber");
    E2eHttpClient.HttpResult result =
        context.getHttpClient().post("/public/inquiries", body.toString());
    HttpSupport.record(context, result);
  }

  @When("I POST root credentials to /setup/root-login")
  public void postRootLogin() {
    throw new UnsupportedOperationException("root-login deferred to separate plan");
  }

  @And("I receive a setup session token")
  public void receiveSetupToken() {
    assertThat(context.getSetupToken(), is(notNullValue()));
  }
}
