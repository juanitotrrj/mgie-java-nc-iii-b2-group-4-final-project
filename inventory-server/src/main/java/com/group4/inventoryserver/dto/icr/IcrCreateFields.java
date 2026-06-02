package com.group4.inventoryserver.dto.icr;

public class IcrCreateFields {

  private final Long productId;
  private final String requestType;
  private final Integer requestedQuantity;
  private final Integer quantityChange;
  private final String reason;

  public IcrCreateFields(
      Long productId,
      String requestType,
      Integer requestedQuantity,
      Integer quantityChange,
      String reason) {
    this.productId = productId;
    this.requestType = requestType;
    this.requestedQuantity = requestedQuantity;
    this.quantityChange = quantityChange;
    this.reason = reason;
  }

  public Long getProductId() {
    return productId;
  }

  public String getRequestType() {
    return requestType;
  }

  public Integer getRequestedQuantity() {
    return requestedQuantity;
  }

  public Integer getQuantityChange() {
    return quantityChange;
  }

  public String getReason() {
    return reason;
  }
}
