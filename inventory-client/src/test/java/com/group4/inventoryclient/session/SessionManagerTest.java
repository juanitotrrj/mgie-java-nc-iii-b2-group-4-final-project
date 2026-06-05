package com.group4.inventoryclient.session;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;

public class SessionManagerTest {

  private SessionManager session;

  @Before
  public void setUp() {
    session = SessionManager.getInstance();
    session.logout();
  }

  @Test
  public void login_populates_permissions_role_and_email() {
    JsonObject user = new JsonObject();
    user.addProperty("userId", 1);
    user.addProperty("username", "admin");
    user.addProperty("fullName", "Admin");
    user.addProperty("email", "admin@example.com");
    user.addProperty("role", "Administrator");
    JsonArray perms = new JsonArray();
    perms.add("PRODUCT_READ");
    perms.add("USER_MANAGE");
    user.add("permissions", perms);

    JsonObject data = new JsonObject();
    data.addProperty("token", "test-jwt");
    data.addProperty("expiresInSeconds", 3600);
    data.add("user", user);

    session.login(data);
    assertThat(session.isLoggedIn(), is(true));
    assertThat(session.hasPermission("PRODUCT_READ"), is(true));
    assertThat(session.hasPermission("USER_MANAGE"), is(true));
    assertThat(session.getRole(), is("Administrator"));
    assertThat(session.getEmail(), is("admin@example.com"));
    assertThat(session.getUsername(), is("admin"));
    assertThat(session.getFullName(), is("Admin"));
    assertThat(session.getUserId(), is(1L));
    assertThat(session.getToken(), is("test-jwt"));
    assertThat(session.getPermissions().size(), is(2));
  }

  @Test
  public void v004_permission_codes_are_recognized() {
    JsonObject user = new JsonObject();
    JsonArray perms = new JsonArray();
    perms.add("INVENTORY_CHANGE_REQUEST_REVIEW");
    perms.add("STOCK_MOVEMENT_READ");
    perms.add("USER_MANAGE");
    perms.add("AUDIT_LOG_READ");
    user.add("permissions", perms);
    JsonObject data = new JsonObject();
    data.addProperty("token", "t");
    data.addProperty("expiresInSeconds", 3600);
    data.add("user", user);
    session.login(data);
    assertThat(session.hasPermission("USER_MANAGE"), is(true));
    assertThat(session.hasPermission("AUDIT_LOG_READ"), is(true));
  }

  @Test
  public void isExpired_trueWhenNotLoggedIn() {
    assertThat(session.isExpired(), is(true));
  }

  @Test
  public void isExpired_trueAfterTtlElapses() throws Exception {
    JsonObject data = new JsonObject();
    data.addProperty("token", "t");
    data.addProperty("expiresInSeconds", 1);
    session.login(data);

    java.lang.reflect.Field tsField = SessionManager.class.getDeclaredField("loginTimestamp");
    tsField.setAccessible(true);
    tsField.set(session, System.currentTimeMillis() - 5000);

    assertThat(session.isExpired(), is(true));
  }

  @Test
  public void isExpired_falseWithinTtl() {
    JsonObject data = new JsonObject();
    data.addProperty("token", "t");
    data.addProperty("expiresInSeconds", 3600);
    session.login(data);
    assertThat(session.isExpired(), is(false));
  }

  @Test
  public void logout_clears_session() {
    JsonObject data = new JsonObject();
    data.addProperty("token", "x");
    data.addProperty("expiresInSeconds", 3600);
    session.login(data);
    session.logout();
    assertThat(session.isLoggedIn(), is(false));
    assertThat(session.hasPermission("PRODUCT_READ"), is(false));
  }
}
