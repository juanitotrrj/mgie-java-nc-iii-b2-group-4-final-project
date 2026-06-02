package com.group4.inventoryserver.dto.purchase;

public class PurchaseItemRequest {

  private Long productId;
  private Integer quantity;
  private Double unitCost;

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  public Double getUnitCost() {
    return unitCost;
  }

  public void setUnitCost(Double unitCost) {
    this.unitCost = unitCost;
  }
}
