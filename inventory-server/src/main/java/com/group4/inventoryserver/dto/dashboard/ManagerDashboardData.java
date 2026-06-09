package com.group4.inventoryserver.dto.dashboard;

import com.group4.inventoryserver.dto.report.TopSellingProduct;
import java.util.List;
import java.util.Map;

public class ManagerDashboardData {

  private final List<MetricCard> cards;
  private final List<Map<String, Object>> pendingApprovals;
  private final List<Map<String, Object>> recentTransactions;
  private final List<TopSellingProduct> topSellingItems;

  public ManagerDashboardData(
      List<MetricCard> cards,
      List<Map<String, Object>> pendingApprovals,
      List<Map<String, Object>> recentTransactions,
      List<TopSellingProduct> topSellingItems) {
    this.cards = cards;
    this.pendingApprovals = pendingApprovals;
    this.recentTransactions = recentTransactions;
    this.topSellingItems = topSellingItems;
  }

  public List<MetricCard> getCards() {
    return cards;
  }

  public List<Map<String, Object>> getPendingApprovals() {
    return pendingApprovals;
  }

  public List<Map<String, Object>> getRecentTransactions() {
    return recentTransactions;
  }

  public List<TopSellingProduct> getTopSellingItems() {
    return topSellingItems;
  }
}
