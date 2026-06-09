package com.group4.inventoryserver.dto.report;

import java.util.List;

public class InventoryValueReport {

  private final double totalInventoryValue;
  private final List<CategoryValueBreakdown> byCategory;
  private final List<InventoryValueProduct> products;

  public InventoryValueReport(
      double totalInventoryValue,
      List<CategoryValueBreakdown> byCategory,
      List<InventoryValueProduct> products) {
    this.totalInventoryValue = totalInventoryValue;
    this.byCategory = byCategory;
    this.products = products;
  }

  public double getTotalInventoryValue() {
    return totalInventoryValue;
  }

  public List<CategoryValueBreakdown> getByCategory() {
    return byCategory;
  }

  public List<InventoryValueProduct> getProducts() {
    return products;
  }
}
