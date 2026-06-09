package com.group4.inventoryclient.ui;

import static org.assertj.swing.core.matcher.JButtonMatcher.withText;
import static org.assertj.swing.edt.GuiActionRunner.execute;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.group4.inventoryclient.api.ApiClient;
import com.group4.inventoryclient.api.CategoryApiClient;
import com.group4.inventoryclient.api.DashboardApiClient;
import com.group4.inventoryclient.api.ProductApiClient;
import com.group4.inventoryclient.api.SetupApiClient;
import com.group4.inventoryclient.api.SupplierApiClient;
import com.group4.inventoryclient.session.SessionManager;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import org.assertj.swing.core.Robot;
import org.assertj.swing.fixture.DialogFixture;

public final class UiTestSupport {

  public static final String FRAME_NAME = "uiTestFrame";

  private UiTestSupport() {}

  public static void enableSwingTesting() {
    System.setProperty("java.awt.headless", "false");
  }

  public static ApiClient testApiClient() {
    return new ApiClient("http://localhost:18080/api");
  }

  public static SetupApiClient testSetupApi() {
    return new SetupApiClient(testApiClient());
  }

  public static ProductApiClient testProductApi() {
    return new ProductApiClient(testApiClient());
  }

  public static CategoryApiClient testCategoryApi() {
    return new CategoryApiClient(testApiClient());
  }

  public static SupplierApiClient testSupplierApi() {
    return new SupplierApiClient(testApiClient());
  }

  public static DashboardApiClient testDashboardApi() {
    return new DashboardApiClient(testApiClient());
  }

  public static void resetSession() {
    SessionManager.getInstance().logout();
  }

  public static void loginAsAdmin() {
    resetSession();
    JsonObject login = new JsonObject();
    login.addProperty("token", "ui-test-token");
    login.addProperty("expiresInSeconds", 3600);

    JsonObject user = new JsonObject();
    user.addProperty("userId", 1L);
    user.addProperty("username", "admin");
    user.addProperty("fullName", "Test Admin");
    user.addProperty("email", "admin@test.local");
    user.addProperty("role", "Administrator");

    JsonArray permissions = new JsonArray();
    String[] allPermissions = {
      "PRODUCT_READ",
      "CATEGORY_READ",
      "SUPPLIER_READ",
      "PURCHASE_READ",
      "SALE_READ",
      "INVENTORY_CHANGE_REQUEST_CREATE",
      "INVENTORY_CHANGE_REQUEST_REVIEW",
      "STOCK_MOVEMENT_READ",
      "REPORT_READ",
      "USER_MANAGE",
      "SETTINGS_MANAGE",
      "AUDIT_LOG_READ"
    };
    for (String permission : allPermissions) {
      permissions.add(permission);
    }
    user.add("permissions", permissions);
    login.add("user", user);

    SessionManager.getInstance().login(login);
  }

  public static JFrame showFrame(FrameBuilder builder) {
    return execute(
        () -> {
          JFrame frame = new JFrame();
          frame.setName(FRAME_NAME);
          builder.build(frame);
          frame.pack();
          frame.setVisible(true);
          return frame;
        });
  }

  public static <D extends JDialog> D showDialog(DialogBuilder<D> builder) {
    return execute(
        () -> {
          JFrame owner = new JFrame();
          owner.pack();
          owner.setVisible(true);
          D dialog = builder.build(owner);
          dialog.setModal(false);
          dialog.pack();
          return dialog;
        });
  }

  public static void assertDialogHasButtons(JDialog dialog, String... buttonTexts) {
    execute(
        () -> {
          dialog.pack();
          Set<String> found = new HashSet<>();
          collectButtonTexts(dialog, found);
          for (String text : buttonTexts) {
            if (!found.contains(text)) {
              throw new AssertionError("Missing button: " + text);
            }
          }
        });
  }

  private static void collectButtonTexts(Container container, Set<String> found) {
    for (Component component : container.getComponents()) {
      if (component instanceof JButton) {
        found.add(((JButton) component).getText());
      }
      if (component instanceof Container) {
        collectButtonTexts((Container) component, found);
      }
    }
  }

  public static void dismissBlockingDialogs(Robot robot) {
    robot.waitForIdle();
    for (Window window : Window.getWindows()) {
      if (window instanceof JDialog && window.isShowing()) {
        DialogFixture dialog = new DialogFixture(robot, (JDialog) window);
        try {
          dialog.button(withText("OK")).click();
        } catch (RuntimeException ignored) {
          dialog.close();
        }
      }
    }
    robot.waitForIdle();
  }

  public interface FrameBuilder {
    void build(JFrame frame);
  }

  public interface DialogBuilder<D extends JDialog> {
    D build(JFrame owner);
  }
}
