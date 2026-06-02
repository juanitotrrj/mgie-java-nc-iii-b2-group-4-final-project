package com.group4.inventoryserver.dto.sale;

public class SaleItemData {

  private final long saleItemId;
  private final long productId;
  private final String productCode;
  private final String productName;
  private final int quantity;
  private final double unitPrice;
  private final double lineTotal;

  public SaleItemData(
      long saleItemId,
      long productId,
      String productCode,
      String productName,
      int quantity,
      double unitPrice,
      double lineTotal) {
    this.saleItemId = saleItemId;
    this.productId = productId;
    this.productCode = productCode;
    this.productName = productName;
    this.quantity = quantity;
    this.unitPrice = unitPrice;
    this.lineTotal = lineTotal;
  }

  public long getSaleItemId() {
    return saleItemId;
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

  public int getQuantity() {
    return quantity;
  }

  public double getUnitPrice() {
    return unitPrice;
  }

  public double getLineTotal() {
    return lineTotal;
  }
}
