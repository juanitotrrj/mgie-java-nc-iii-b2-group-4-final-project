package com.group4.bdd.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.gson.JsonObject;
import com.group4.bdd.SharedTestContext;
import com.group4.bdd.TestContext;
import com.group4.bdd.steps.common.HttpSupport;
import com.group4.inventoryserver.testing.E2eHttpClient;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class AuthSteps {

  private final TestContext context = SharedTestContext.get();

  @Given("the system is initialized")
  public void theSystemIsInitialized() {
    context.setFixture("initialized");
  }

  @Given("the system is in INFRA_READY_APP_SETUP_PENDING state")
  public void infraReadyState() {
    context.setFixture("infra_ready");
  }

  @Given("I am logged in as administrator")
  public void loggedInAsAdmin() throws Exception {
    login("admin", "Administrator", "Admin@123");
  }

  @Given("an inactive user {string} exists with password {string}")
  public void inactiveUserExists(String username, String password) throws Exception {
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.login(context, "admin", "Administrator", "Admin@123");

    String email = username + "@bdd.test";
    String json =
        "{"
            + "\"fullName\":\"Inactive BDD User\","
            + "\"username\":\""
            + username
            + "\","
            + "\"email\":\""
            + email
            + "\","
            + "\"role\":\"Cashier\","
            + "\"password\":\""
            + password
            + "\","
            + "\"status\":\"Active\""
            + "}";
    E2eHttpClient.HttpResult created =
        context.getHttpClient().post("/users", json);
    assertThat(created.status, is(201));
    long userId = created.jsonRoot().getAsJsonObject("data").get("userId").getAsLong();

    JsonObject deactivate = new JsonObject();
    deactivate.addProperty("status", "Inactive");
    E2eHttpClient.HttpResult updated =
        context.getHttpClient().put("/users/" + userId, deactivate.toString());
    assertThat(updated.status, is(200));
    context.getHttpClient().setBearerToken(null);
    context.setVar("inactiveUsername", username);
    context.setVar("inactivePassword", password);
  }

  @When("I login as {string} with password {string}")
  public void loginDefaultRole(String username, String password) throws Exception {
    login(username, defaultRole(username), password);
  }

  @When("I login as {string} with role {string} and password {string}")
  public void loginWithRole(String username, String role, String password) throws Exception {
    login(username, role, password);
  }

  @When("I login as stored username with role {string} and password {string}")
  public void loginStoredUser(String role, String password) throws Exception {
    String username = context.getVar("username");
    login(username, role, password);
  }

  @When("I logout via the API")
  public void logout() throws Exception {
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().post("/auth/logout", "{}"));
    context.getHttpClient().setBearerToken(null);
  }

  @When("I request the current session profile")
  public void requestSessionProfile() throws Exception {
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().get("/auth/me"));
  }

  @Then("the login response contains a token")
  public void assertToken() {
    JsonObject data = HttpSupport.dataObject(context);
    assertThat(data.has("token"), is(true));
  }

  @Then("the session profile username is {string}")
  public void assertProfileUsername(String username) {
    JsonObject data = HttpSupport.dataObject(context);
    assertThat(data.get("username").getAsString(), is(username));
  }

  private void login(String username, String role, String password) throws Exception {
    HttpSupport.login(context, username, role, password);
  }

  private static String defaultRole(String username) {
    if ("admin".equals(username)) {
      return "Administrator";
    }
    return "Administrator";
  }
}
