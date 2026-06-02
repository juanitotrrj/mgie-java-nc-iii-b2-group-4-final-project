package com.group4.inventoryserver.dto.product;

public class StockMovementData {

  private final long stockMovementId;
  private final long productId;
  private final String productCode;
  private final String productName;
  private final String movementType;
  private final String referenceType;
  private final Long referenceId;
  private final String referenceNo;
  private final int quantityBefore;
  private final int quantityChange;
  private final int quantityAfter;
  private final String remarks;
  private final Long createdBy;
  private final String createdByName;
  private final String createdAt;

  public StockMovementData(
      long stockMovementId,
      long productId,
      String productCode,
      String productName,
      String movementType,
      String referenceType,
      Long referenceId,
      String referenceNo,
      int quantityBefore,
      int quantityChange,
      int quantityAfter,
      String remarks,
      Long createdBy,
      String createdByName,
      String createdAt) {
    this.stockMovementId = stockMovementId;
    this.productId = productId;
    this.productCode = productCode;
    this.productName = productName;
    this.movementType = movementType;
    this.referenceType = referenceType;
    this.referenceId = referenceId;
    this.referenceNo = referenceNo;
    this.quantityBefore = quantityBefore;
    this.quantityChange = quantityChange;
    this.quantityAfter = quantityAfter;
    this.remarks = remarks;
    this.createdBy = createdBy;
    this.createdByName = createdByName;
    this.createdAt = createdAt;
  }

  public long getStockMovementId() {
    return stockMovementId;
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

  public String getMovementType() {
    return movementType;
  }

  public String getReferenceType() {
    return referenceType;
  }

  public Long getReferenceId() {
    return referenceId;
  }

  public String getReferenceNo() {
    return referenceNo;
  }

  public int getQuantityBefore() {
    return quantityBefore;
  }

  public int getQuantityChange() {
    return quantityChange;
  }

  public int getQuantityAfter() {
    return quantityAfter;
  }

  public String getRemarks() {
    return remarks;
  }

  public Long getCreatedBy() {
    return createdBy;
  }

  public String getCreatedByName() {
    return createdByName;
  }

  public String getCreatedAt() {
    return createdAt;
  }
}
