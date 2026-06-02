package com.group4.inventoryserver.dto.report;

public class CategoryValueBreakdown {

  private final long categoryId;
  private final String categoryName;
  private final double inventoryValue;
  private final double percentage;

  public CategoryValueBreakdown(
      long categoryId, String categoryName, double inventoryValue, double percentage) {
    this.categoryId = categoryId;
    this.categoryName = categoryName;
    this.inventoryValue = inventoryValue;
    this.percentage = percentage;
  }

  public long getCategoryId() {
    return categoryId;
  }

  public String getCategoryName() {
    return categoryName;
  }

  public double getInventoryValue() {
    return inventoryValue;
  }

  public double getPercentage() {
    return percentage;
  }
}
