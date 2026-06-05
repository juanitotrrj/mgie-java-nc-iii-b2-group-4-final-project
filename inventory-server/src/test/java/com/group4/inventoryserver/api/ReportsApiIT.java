package com.group4.inventoryserver.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.group4.inventoryserver.testing.E2eHttpClient;
import org.junit.Test;

public class ReportsApiIT extends ApiITBase {

  @Test
  public void options_returns_filter_metadata() throws Exception {
    E2eHttpClient.HttpResult result = adminClient.get("/reports/options");
    assertThat(result.status, is(200));
    assertThat(result.jsonRoot().getAsJsonObject("data").has("categories"), is(true));
  }

  @Test
  public void sales_summary_returns_report_data() throws Exception {
    E2eHttpClient.HttpResult result = adminClient.get("/reports/sales-summary");
    assertThat(result.status, is(200));
    assertThat(result.jsonRoot().getAsJsonObject("data").has("totalSales"), is(true));
  }
}
