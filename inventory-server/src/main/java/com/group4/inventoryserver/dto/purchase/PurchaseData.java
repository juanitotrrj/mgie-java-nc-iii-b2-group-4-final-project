package com.group4.inventoryserver.dto.purchase;

public class PurchaseData {

  private final long purchaseId;
  private final String poNumber;
  private final long supplierId;
  private final String supplierName;
  private final String orderDate;
  private final String expectedDeliveryDate;
  private final String receivedDate;
  private final int itemCount;
  private final double totalAmount;
  private final String status;
  private final String notes;
  private final String cancelReason;
  private final String createdAt;
  private final String updatedAt;

  public PurchaseData(
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
      String updatedAt) {
    this.purchaseId = purchaseId;
    this.poNumber = poNumber;
    this.supplierId = supplierId;
    this.supplierName = supplierName;
    this.orderDate = orderDate;
    this.expectedDeliveryDate = expectedDeliveryDate;
    this.receivedDate = receivedDate;
    this.itemCount = itemCount;
    this.totalAmount = totalAmount;
    this.status = status;
    this.notes = notes;
    this.cancelReason = cancelReason;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public long getPurchaseId() {
    return purchaseId;
  }

  public String getPoNumber() {
    return poNumber;
  }

  public long getSupplierId() {
    return supplierId;
  }

  public String getSupplierName() {
    return supplierName;
  }

  public String getOrderDate() {
    return orderDate;
  }

  public String getExpectedDeliveryDate() {
    return expectedDeliveryDate;
  }

  public String getReceivedDate() {
    return receivedDate;
  }

  public int getItemCount() {
    return itemCount;
  }

  public double getTotalAmount() {
    return totalAmount;
  }

  public String getStatus() {
    return status;
  }

  public String getNotes() {
    return notes;
  }

  public String getCancelReason() {
    return cancelReason;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public String getUpdatedAt() {
    return updatedAt;
  }
}
