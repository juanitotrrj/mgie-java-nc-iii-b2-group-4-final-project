package com.group4.inventoryserver.dto.report;

import java.util.List;

public class TopSellingReport {

  private final String dateFrom;
  private final String dateTo;
  private final List<TopSellingProduct> items;

  public TopSellingReport(String dateFrom, String dateTo, List<TopSellingProduct> items) {
    this.dateFrom = dateFrom;
    this.dateTo = dateTo;
    this.items = items;
  }

  public String getDateFrom() {
    return dateFrom;
  }

  public String getDateTo() {
    return dateTo;
  }

  public List<TopSellingProduct> getItems() {
    return items;
  }
}
