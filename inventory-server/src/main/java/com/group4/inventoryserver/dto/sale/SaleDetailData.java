package com.group4.inventoryserver.dto.sale;

import java.util.List;

public class SaleDetailData extends SaleData {

  private final List<SaleItemData> items;

  public SaleDetailData(
      long saleId,
      String invoiceNo,
      String customerName,
      Long cashierId,
      String cashierName,
      String saleDate,
      String paymentMethod,
      String status,
      int itemCount,
      double subtotal,
      double taxAmount,
      double totalAmount,
      double amountReceived,
      double changeAmount,
      String cancelReason,
      String createdAt,
      String updatedAt,
      List<SaleItemData> items) {
    super(
        saleId,
        invoiceNo,
        customerName,
        cashierId,
        cashierName,
        saleDate,
        paymentMethod,
        status,
        itemCount,
        subtotal,
        taxAmount,
        totalAmount,
        amountReceived,
        changeAmount,
        cancelReason,
        createdAt,
        updatedAt);
    this.items = items;
  }

  public List<SaleItemData> getItems() {
    return items;
  }
}
