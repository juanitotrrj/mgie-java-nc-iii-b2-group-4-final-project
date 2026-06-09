package com.group4.inventoryserver.handler;

import com.group4.inventoryserver.dto.guest.ContactData;
import com.group4.inventoryserver.server.RequestContext;
import java.io.IOException;

public class ContactHandler extends BaseHandler {

  @Override
  protected void handleGet(RequestContext ctx) throws IOException {
    ContactData data =
        new ContactData(
            "support@inventoryms.com",
            "+1 (555) 123-4567",
            "+1 (555) 987-6543",
            "456 Business Park Drive, Suite 200, Austin, TX 78701, USA",
            "Mon - Fri: 9:00 AM - 5:00 PM",
            "www.inventoryms.com");
    sendSuccess(ctx, data);
  }
}
