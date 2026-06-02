package com.group4.inventoryserver.dto.icr;

public class IcrData {

  private final long requestId;
  private final String requestNo;
  private final long productId;
  private final String productCode;
  private final String productName;
  private final String requestType;
  private final int currentQuantity;
  private final Integer requestedQuantity;
  private final Integer quantityChange;
  private final String reason;
  private final String status;
  private final long requestedById;
  private final String requestedBy;
  private final String requestedAt;
  private final String reviewedBy;
  private final String reviewedAt;
  private final String reviewNotes;

  public IcrData(
      long requestId,
      String requestNo,
      long productId,
      String productCode,
      String productName,
      String requestType,
      int currentQuantity,
      Integer requestedQuantity,
      Integer quantityChange,
      String reason,
      String status,
      long requestedById,
      String requestedBy,
      String requestedAt,
      String reviewedBy,
      String reviewedAt,
      String reviewNotes) {
    this.requestId = requestId;
    this.requestNo = requestNo;
    this.productId = productId;
    this.productCode = productCode;
    this.productName = productName;
    this.requestType = requestType;
    this.currentQuantity = currentQuantity;
    this.requestedQuantity = requestedQuantity;
    this.quantityChange = quantityChange;
    this.reason = reason;
    this.status = status;
    this.requestedById = requestedById;
    this.requestedBy = requestedBy;
    this.requestedAt = requestedAt;
    this.reviewedBy = reviewedBy;
    this.reviewedAt = reviewedAt;
    this.reviewNotes = reviewNotes;
  }

  public long getRequestId() {
    return requestId;
  }

  public String getRequestNo() {
    return requestNo;
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

  public String getRequestType() {
    return requestType;
  }

  public int getCurrentQuantity() {
    return currentQuantity;
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

  public String getStatus() {
    return status;
  }

  public long getRequestedById() {
    return requestedById;
  }

  public String getRequestedBy() {
    return requestedBy;
  }

  public String getRequestedAt() {
    return requestedAt;
  }

  public String getReviewedBy() {
    return reviewedBy;
  }

  public String getReviewedAt() {
    return reviewedAt;
  }

  public String getReviewNotes() {
    return reviewNotes;
  }
}
