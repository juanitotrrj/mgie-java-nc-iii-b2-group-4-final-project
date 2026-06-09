package com.group4.inventoryserver.dto.dashboard;

import java.util.List;
import java.util.Map;

public class CashierDashboardData {

  private final List<MetricCard> cards;
  private final List<Map<String, Object>> recentSales;

  public CashierDashboardData(List<MetricCard> cards, List<Map<String, Object>> recentSales) {
    this.cards = cards;
    this.recentSales = recentSales;
  }

  public List<MetricCard> getCards() {
    return cards;
  }

  public List<Map<String, Object>> getRecentSales() {
    return recentSales;
  }
}
