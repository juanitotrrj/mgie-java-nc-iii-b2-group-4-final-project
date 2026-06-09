package com.group4.inventoryserver.dto.report;

import java.util.List;

public class SalesSummaryReport {

  private final String dateFrom;
  private final String dateTo;
  private final double totalSales;
  private final double averagePerDay;
  private final String bestDay;
  private final int totalOrders;
  private final List<TrendPoint> trend;
  private final List<TopSellingProduct> topProducts;

  public SalesSummaryReport(
      String dateFrom,
      String dateTo,
      double totalSales,
      double averagePerDay,
      String bestDay,
      int totalOrders,
      List<TrendPoint> trend,
      List<TopSellingProduct> topProducts) {
    this.dateFrom = dateFrom;
    this.dateTo = dateTo;
    this.totalSales = totalSales;
    this.averagePerDay = averagePerDay;
    this.bestDay = bestDay;
    this.totalOrders = totalOrders;
    this.trend = trend;
    this.topProducts = topProducts;
  }

  public String getDateFrom() {
    return dateFrom;
  }

  public String getDateTo() {
    return dateTo;
  }

  public double getTotalSales() {
    return totalSales;
  }

  public double getAveragePerDay() {
    return averagePerDay;
  }

  public String getBestDay() {
    return bestDay;
  }

  public int getTotalOrders() {
    return totalOrders;
  }

  public List<TrendPoint> getTrend() {
    return trend;
  }

  public List<TopSellingProduct> getTopProducts() {
    return topProducts;
  }
}
