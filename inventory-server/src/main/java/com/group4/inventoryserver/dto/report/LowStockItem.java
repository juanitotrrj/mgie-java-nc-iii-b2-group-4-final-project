package com.group4.inventoryserver.dto.report;

public class LowStockItem {

  private final long productId;
  private final String productCode;
  private final String productName;
  private final String categoryName;
  private final int quantity;
  private final int reorderLevel;
  private final int deficit;

  public LowStockItem(
      long productId,
      String productCode,
      String productName,
      String categoryName,
      int quantity,
      int reorderLevel) {
    this.productId = productId;
    this.productCode = productCode;
    this.productName = productName;
    this.categoryName = categoryName;
    this.quantity = quantity;
    this.reorderLevel = reorderLevel;
    this.deficit = reorderLevel - quantity;
  }

  public long getProductId() {
    return productId;
  }

  public String getProductCode() {
    return productCode;
  }

  public String getProductName() {
    return productName;
  }

  public String getCategoryName() {
    return categoryName;
  }

  public int getQuantity() {
    return quantity;
  }

  public int getReorderLevel() {
    return reorderLevel;
  }

  public int getDeficit() {
    return deficit;
  }
}
