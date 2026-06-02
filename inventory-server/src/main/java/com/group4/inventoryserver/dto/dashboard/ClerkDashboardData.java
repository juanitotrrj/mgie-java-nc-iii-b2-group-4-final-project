package com.group4.inventoryserver.dto.dashboard;

import java.util.List;
import java.util.Map;

public class ClerkDashboardData {

  private final List<MetricCard> cards;
  private final Map<String, Integer> requestSummary;
  private final List<Map<String, Object>> recentInventoryActivity;

  public ClerkDashboardData(
      List<MetricCard> cards,
      Map<String, Integer> requestSummary,
      List<Map<String, Object>> recentInventoryActivity) {
    this.cards = cards;
    this.requestSummary = requestSummary;
    this.recentInventoryActivity = recentInventoryActivity;
  }

  public List<MetricCard> getCards() {
    return cards;
  }

  public Map<String, Integer> getRequestSummary() {
    return requestSummary;
  }

  public List<Map<String, Object>> getRecentInventoryActivity() {
    return recentInventoryActivity;
  }
}
