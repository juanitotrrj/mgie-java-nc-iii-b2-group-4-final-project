package com.group4.inventoryserver.dto.report;

import java.util.List;

public class PurchaseCostReport {

  private final String dateFrom;
  private final String dateTo;
  private final double totalPurchaseCost;
  private final int totalOrders;
  private final List<TrendPoint> trend;

  public PurchaseCostReport(
      String dateFrom,
      String dateTo,
      double totalPurchaseCost,
      int totalOrders,
      List<TrendPoint> trend) {
    this.dateFrom = dateFrom;
    this.dateTo = dateTo;
    this.totalPurchaseCost = totalPurchaseCost;
    this.totalOrders = totalOrders;
    this.trend = trend;
  }

  public String getDateFrom() {
    return dateFrom;
  }

  public String getDateTo() {
    return dateTo;
  }

  public double getTotalPurchaseCost() {
    return totalPurchaseCost;
  }

  public int getTotalOrders() {
    return totalOrders;
  }

  public List<TrendPoint> getTrend() {
    return trend;
  }
}
