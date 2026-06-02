package com.group4.inventoryserver.dto.report;

public class InventoryValueProduct {

  private final long productId;
  private final String productCode;
  private final String productName;
  private final String categoryName;
  private final int quantity;
  private final double price;
  private final double totalValue;

  public InventoryValueProduct(
      long productId,
      String productCode,
      String productName,
      String categoryName,
      int quantity,
      double price,
      double totalValue) {
    this.productId = productId;
    this.productCode = productCode;
    this.productName = productName;
    this.categoryName = categoryName;
    this.quantity = quantity;
    this.price = price;
    this.totalValue = totalValue;
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

  public double getPrice() {
    return price;
  }

  public double getTotalValue() {
    return totalValue;
  }
}
