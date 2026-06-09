package com.group4.inventoryserver.dto.purchase;

import java.util.List;

public class PurchaseDetailData extends PurchaseData {

  private final List<PurchaseItemData> items;

  public PurchaseDetailData(
      long purchaseId,
      String poNumber,
      long supplierId,
      String supplierName,
      String orderDate,
      String expectedDeliveryDate,
      String receivedDate,
      int itemCount,
      double totalAmount,
      String status,
      String notes,
      String cancelReason,
      String createdAt,
      String updatedAt,
      List<PurchaseItemData> items) {
    super(
        purchaseId,
        poNumber,
        supplierId,
        supplierName,
        orderDate,
        expectedDeliveryDate,
        receivedDate,
        itemCount,
        totalAmount,
        status,
        notes,
        cancelReason,
        createdAt,
        updatedAt);
    this.items = items;
  }

  public List<PurchaseItemData> getItems() {
    return items;
  }
}
