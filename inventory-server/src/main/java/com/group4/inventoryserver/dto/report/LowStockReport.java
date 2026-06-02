package com.group4.inventoryserver.dto.report;

import java.util.List;

public class LowStockReport {

  private final int lowStockCount;
  private final List<LowStockItem> items;

  public LowStockReport(int lowStockCount, List<LowStockItem> items) {
    this.lowStockCount = lowStockCount;
    this.items = items;
  }

  public int getLowStockCount() {
    return lowStockCount;
  }

  public List<LowStockItem> getItems() {
    return items;
  }
}
