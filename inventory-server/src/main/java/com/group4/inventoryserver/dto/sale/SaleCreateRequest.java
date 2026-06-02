package com.group4.inventoryserver.dto.sale;

import java.util.List;

public class SaleCreateRequest {

  private String customerName;
  private List<SaleItemRequest> items;
  private String paymentMethod;
  private Double amountReceived;
  private String status;

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }

  public List<SaleItemRequest> getItems() {
    return items;
  }

  public void setItems(List<SaleItemRequest> items) {
    this.items = items;
  }

  public String getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(String paymentMethod) {
    this.paymentMethod = paymentMethod;
  }

  public Double getAmountReceived() {
    return amountReceived;
  }

  public void setAmountReceived(Double amountReceived) {
    this.amountReceived = amountReceived;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
