package com.group4.inventoryserver.dto.dashboard;

import java.util.List;
import java.util.Map;

public class AdminDashboardData {

  private final List<MetricCard> cards;
  private final List<Map<String, Object>> recentActivity;
  private final String systemStatus;

  public AdminDashboardData(
      List<MetricCard> cards, List<Map<String, Object>> recentActivity, String systemStatus) {
    this.cards = cards;
    this.recentActivity = recentActivity;
    this.systemStatus = systemStatus;
  }

  public List<MetricCard> getCards() {
    return cards;
  }

  public List<Map<String, Object>> getRecentActivity() {
    return recentActivity;
  }

  public String getSystemStatus() {
    return systemStatus;
  }
}
