package com.group4.inventoryserver.handler;

import com.group4.inventoryserver.dto.guest.AboutData;
import com.group4.inventoryserver.server.RequestContext;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AboutHandler extends BaseHandler {

  private static final List<String> CORE_MODULES =
      Arrays.asList(
          "Products",
          "Categories",
          "Suppliers",
          "Purchases",
          "Sales",
          "Reports",
          "Users",
          "Settings");

  private static final List<String> BENEFITS =
      Arrays.asList(
          "Improve data accuracy", "Reduce manual errors", "Support better decision-making");

  @Override
  protected void handleGet(RequestContext ctx) throws IOException {
    AboutData data =
        new AboutData(
            "A Java Swing desktop inventory system with a Java SE backend API.",
            "Help businesses manage products, categories, suppliers, "
                + "purchases, sales, reports, users, and settings.",
            CORE_MODULES,
            BENEFITS,
            "Guests can only view public information and test server connectivity.");
    sendSuccess(ctx, data);
  }
}
