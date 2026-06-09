package com.group4.inventoryserver.handler;

import com.group4.inventoryserver.config.EnvConfig;
import com.group4.inventoryserver.dto.guest.WelcomeData;
import com.group4.inventoryserver.server.RequestContext;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class WelcomeHandler extends BaseHandler {

  private static final List<String> GUEST_CAPABILITIES =
      Arrays.asList(
          "View general system information",
          "Contact support",
          "Test server connection",
          "Proceed to login");

  private static final List<String> ACTIONS =
      Arrays.asList("Test Server Connection", "About System", "Proceed to Login");

  @Override
  protected void handleGet(RequestContext ctx) throws IOException {
    String systemName = EnvConfig.appName().replace(" - Group 4 API", "");
    WelcomeData data =
        new WelcomeData(
            systemName,
            "Welcome to the " + systemName,
            "Browse general system information, contact support, "
                + "test server connection, or proceed to login.",
            GUEST_CAPABILITIES,
            ACTIONS);
    sendSuccess(ctx, data);
  }
}
