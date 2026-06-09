package com.group4.inventoryserver.dto.purchase;

public class PurchaseItemData {

  private final long purchaseOrderItemId;
  private final long productId;
  private final String productCode;
  private final String productName;
  private final int quantity;
  private final double unitCost;
  private final double lineTotal;

  public PurchaseItemData(
      long purchaseOrderItemId,
      long productId,
      String productCode,
      String productName,
      int quantity,
      double unitCost,
      double lineTotal) {
    this.purchaseOrderItemId = purchaseOrderItemId;
    this.productId = productId;
    this.productCode = productCode;
    this.productName = productName;
    this.quantity = quantity;
    this.unitCost = unitCost;
    this.lineTotal = lineTotal;
  }

  public long getPurchaseOrderItemId() {
    return purchaseOrderItemId;
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

  public double getUnitCost() {
    return unitCost;
  }

  public double getLineTotal() {
    return lineTotal;
  }
}
