package com.group4.inventoryserver.dto.setup;

import static org.junit.Assert.*;

import com.group4.inventoryserver.util.JsonUtil;
import org.junit.Test;

public class BusinessSettingsRequestTest {

  @Test
  public void gettersAndSetters_workCorrectly() {
    BusinessSettingsRequest req = new BusinessSettingsRequest();
    req.setCompanyName("ABC Trading");
    req.setCurrencyCode("PHP");
    req.setCurrencySymbol("\u20B1");
    req.setCurrencyName("Philippine Peso");
    req.setTaxRate(12.0);
    req.setTaxType("Inclusive of Tax");
    req.setPriceDecimalPlaces(2);
    req.setTimezone("Asia/Manila");

    assertEquals("ABC Trading", req.getCompanyName());
    assertEquals("PHP", req.getCurrencyCode());
    assertEquals("\u20B1", req.getCurrencySymbol());
    assertEquals("Philippine Peso", req.getCurrencyName());
    assertEquals(12.0, req.getTaxRate(), 0.001);
    assertEquals("Inclusive of Tax", req.getTaxType());
    assertEquals(2, req.getPriceDecimalPlaces());
    assertEquals("Asia/Manila", req.getTimezone());
  }

  @Test
  public void roundTrip_jsonSerialization() {
    BusinessSettingsRequest original = new BusinessSettingsRequest();
    original.setCompanyName("Test Co");
    original.setTaxRate(10.5);
    original.setPriceDecimalPlaces(2);

    BusinessSettingsRequest parsed =
        JsonUtil.fromJson(JsonUtil.toJson(original), BusinessSettingsRequest.class);

    assertEquals("Test Co", parsed.getCompanyName());
    assertEquals(10.5, parsed.getTaxRate(), 0.001);
    assertEquals(2, parsed.getPriceDecimalPlaces());
  }
}
