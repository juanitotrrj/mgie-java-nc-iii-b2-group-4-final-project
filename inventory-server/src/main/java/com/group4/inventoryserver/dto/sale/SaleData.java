package com.group4.inventoryserver.dto.sale;

public class SaleData {

  private final long saleId;
  private final String invoiceNo;
  private final String customerName;
  private final Long cashierId;
  private final String cashierName;
  private final String saleDate;
  private final String paymentMethod;
  private final String status;
  private final int itemCount;
  private final double subtotal;
  private final double taxAmount;
  private final double totalAmount;
  private final double amountReceived;
  private final double changeAmount;
  private final String cancelReason;
  private final String createdAt;
  private final String updatedAt;

  public SaleData(
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
      String updatedAt) {
    this.saleId = saleId;
    this.invoiceNo = invoiceNo;
    this.customerName = customerName;
    this.cashierId = cashierId;
    this.cashierName = cashierName;
    this.saleDate = saleDate;
    this.paymentMethod = paymentMethod;
    this.status = status;
    this.itemCount = itemCount;
    this.subtotal = subtotal;
    this.taxAmount = taxAmount;
    this.totalAmount = totalAmount;
    this.amountReceived = amountReceived;
    this.changeAmount = changeAmount;
    this.cancelReason = cancelReason;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public long getSaleId() {
    return saleId;
  }

  public String getInvoiceNo() {
    return invoiceNo;
  }

  public String getCustomerName() {
    return customerName;
  }

  public Long getCashierId() {
    return cashierId;
  }

  public String getCashierName() {
    return cashierName;
  }

  public String getSaleDate() {
    return saleDate;
  }

  public String getPaymentMethod() {
    return paymentMethod;
  }

  public String getStatus() {
    return status;
  }

  public int getItemCount() {
    return itemCount;
  }

  public double getSubtotal() {
    return subtotal;
  }

  public double getTaxAmount() {
    return taxAmount;
  }

  public double getTotalAmount() {
    return totalAmount;
  }

  public double getAmountReceived() {
    return amountReceived;
  }

  public double getChangeAmount() {
    return changeAmount;
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
