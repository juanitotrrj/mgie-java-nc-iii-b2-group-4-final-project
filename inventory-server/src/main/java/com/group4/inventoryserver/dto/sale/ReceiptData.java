package com.group4.inventoryserver.dto.sale;

import java.util.List;

public class ReceiptData {

  private final String invoiceNo;
  private final String saleDate;
  private final String cashierName;
  private final String customerName;
  private final List<SaleItemData> items;
  private final double subtotal;
  private final double taxAmount;
  private final double totalAmount;
  private final String paymentMethod;
  private final double amountReceived;
  private final double changeAmount;

  public ReceiptData(
      String invoiceNo,
      String saleDate,
      String cashierName,
      String customerName,
      List<SaleItemData> items,
      double subtotal,
      double taxAmount,
      double totalAmount,
      String paymentMethod,
      double amountReceived,
      double changeAmount) {
    this.invoiceNo = invoiceNo;
    this.saleDate = saleDate;
    this.cashierName = cashierName;
    this.customerName = customerName;
    this.items = items;
    this.subtotal = subtotal;
    this.taxAmount = taxAmount;
    this.totalAmount = totalAmount;
    this.paymentMethod = paymentMethod;
    this.amountReceived = amountReceived;
    this.changeAmount = changeAmount;
  }

  public String getInvoiceNo() {
    return invoiceNo;
  }

  public String getSaleDate() {
    return saleDate;
  }

  public String getCashierName() {
    return cashierName;
  }

  public String getCustomerName() {
    return customerName;
  }

  public List<SaleItemData> getItems() {
    return items;
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

  public String getPaymentMethod() {
    return paymentMethod;
  }

  public double getAmountReceived() {
    return amountReceived;
  }

  public double getChangeAmount() {
    return changeAmount;
  }
}
