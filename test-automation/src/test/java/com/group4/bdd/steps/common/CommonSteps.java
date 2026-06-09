package com.group4.bdd.steps.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.group4.bdd.SharedTestContext;
import com.group4.bdd.TestContext;
import com.group4.inventoryserver.testing.E2eHttpClient;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CommonSteps {

  private final TestContext context = SharedTestContext.get();

  @Given("the API server is running")
  public void apiServerRunning() throws Exception {
    HttpSupport.startServerIfNeeded(context);
  }

  @When("I GET {string}")
  public void getPath(String path) throws Exception {
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().get(path));
  }

  @When("I POST {string} with empty body")
  public void postEmpty(String path) throws Exception {
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().post(path, "{}"));
  }

  @When("I POST {string} with JSON:")
  public void postJson(String path, String json) throws Exception {
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().post(path, json.trim()));
  }

  @When("I PUT {string} with JSON:")
  public void putJson(String path, String json) throws Exception {
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().put(path, json.trim()));
  }

  @When("I DELETE {string}")
  public void deletePath(String path) throws Exception {
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().delete(path));
  }

  @When("I clear the auth token")
  public void clearAuthToken() throws Exception {
    HttpSupport.startServerIfNeeded(context);
    context.getHttpClient().setBearerToken(null);
  }

  @When("I set an invalid bearer token")
  public void setInvalidToken() throws Exception {
    HttpSupport.startServerIfNeeded(context);
    context.getHttpClient().setBearerToken("invalid-bdd-token");
  }

  @Then("the response status is {int}")
  public void assertStatus(int expected) {
    assertThat(context.getLastStatus(), is(expected));
  }

  @Then("the response body has data field {string}")
  public void assertDataField(String field) {
    JsonObject data = HttpSupport.dataObject(context);
    assertThat(data.has(field), is(true));
  }

  @Then("the response data array is not empty")
  public void assertDataArrayNotEmpty() {
    JsonObject root = HttpSupport.parseBody(context);
    assertThat(root.has("data"), is(true));
    JsonArray data = root.getAsJsonArray("data");
    assertThat(data.size(), greaterThan(0));
  }

  @Then("the response body contains text {string}")
  public void assertBodyContains(String text) {
    assertThat(context.getLastBody(), is(notNullValue()));
    assertThat(context.getLastBody().contains(text), is(true));
  }

  @Then("the stored {string} is preserved")
  public void assertStoredVar(String key) {
    assertThat(context.getVar(key), is(notNullValue()));
  }

  @And("I store response data field {string} as {string}")
  public void storeDataField(String field, String varName) {
    JsonObject data = HttpSupport.dataObject(context);
    if (data.has(field)) {
      if (data.get(field).isJsonPrimitive()) {
        context.setVar(varName, data.get(field).getAsString());
      } else {
        context.setVar(varName, data.get(field));
      }
    }
  }

  @And("I store response data numeric field {string} as {string}")
  public void storeNumericField(String field, String varName) {
    JsonObject data = HttpSupport.dataObject(context);
    assertThat(data.has(field), is(true));
    context.setVar(varName, data.get(field).getAsLong());
  }
}
