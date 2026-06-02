package com.group4.inventoryserver.dto.purchase;

import java.util.List;

public class PurchaseUpdateRequest {

  private Long supplierId;
  private String orderDate;
  private String expectedDeliveryDate;
  private String notes;
  private List<PurchaseItemRequest> items;

  public Long getSupplierId() {
    return supplierId;
  }

  public void setSupplierId(Long supplierId) {
    this.supplierId = supplierId;
  }

  public String getOrderDate() {
    return orderDate;
  }

  public void setOrderDate(String orderDate) {
    this.orderDate = orderDate;
  }

  public String getExpectedDeliveryDate() {
    return expectedDeliveryDate;
  }

  public void setExpectedDeliveryDate(String expectedDeliveryDate) {
    this.expectedDeliveryDate = expectedDeliveryDate;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public List<PurchaseItemRequest> getItems() {
    return items;
  }

  public void setItems(List<PurchaseItemRequest> items) {
    this.items = items;
  }
}
