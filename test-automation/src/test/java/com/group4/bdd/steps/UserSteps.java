package com.group4.bdd.steps;

import com.google.gson.JsonObject;
import com.group4.bdd.SharedTestContext;
import com.group4.bdd.TestContext;
import com.group4.bdd.steps.common.HttpSupport;
import io.cucumber.java.en.When;

public class UserSteps {

  private final TestContext context = SharedTestContext.get();

  @When("I list users")
  public void listUsers() throws Exception {
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().get("/users?page=1&pageSize=10"));
  }

  @When("I create a test cashier user")
  public void createUser() throws Exception {
    HttpSupport.startServerIfNeeded(context);
    String username = "bdduser" + System.currentTimeMillis();
    JsonObject body = new JsonObject();
    body.addProperty("fullName", "BDD Test User");
    body.addProperty("username", username);
    body.addProperty("email", username + "@bdd.test");
    body.addProperty("role", "Cashier");
    body.addProperty("password", "Test@1234");
    body.addProperty("status", "Active");
    HttpSupport.record(context, context.getHttpClient().post("/users", body.toString()));
    if (context.getLastStatus() == 201) {
      context.setVar("userId", HttpSupport.dataObject(context).get("userId").getAsLong());
      context.setVar("username", username);
      context.setVar("userPassword", "Test@1234");
    }
  }

  @When("I get the stored user by id")
  public void getUser() throws Exception {
    long userId = context.getLongVar("userId");
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().get("/users/" + userId));
  }

  @When("I update the stored user full name to {string}")
  public void updateUser(String fullName) throws Exception {
    long userId = context.getLongVar("userId");
    JsonObject body = new JsonObject();
    body.addProperty("fullName", fullName);
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().put("/users/" + userId, body.toString()));
  }

  @When("I deactivate the stored user")
  public void deactivateUser() throws Exception {
    long userId = context.getLongVar("userId");
    JsonObject body = new JsonObject();
    body.addProperty("status", "Inactive");
    HttpSupport.startServerIfNeeded(context);
    HttpSupport.record(context, context.getHttpClient().put("/users/" + userId, body.toString()));
  }
}
