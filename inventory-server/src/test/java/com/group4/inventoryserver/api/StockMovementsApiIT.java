package com.group4.inventoryserver.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

public class StockMovementsApiIT extends ApiITBase {

  @Test
  public void list_stock_movements_returns_200() throws Exception {
    assertThat(adminClient.get("/stock-movements?page=1&pageSize=10").status, is(200));
  }
}
