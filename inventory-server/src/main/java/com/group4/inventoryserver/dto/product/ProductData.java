package com.group4.inventoryserver.dto.product;

public class ProductData {

  private final long productId;
  private final String productCode;
  private final String productName;
  private final long categoryId;
  private final String categoryName;
  private final Long supplierId;
  private final String supplierName;
  private final int quantity;
  private final int reorderLevel;
  private final double unitPrice;
  private final double totalValue;
  private final String status;
  private final String createdAt;
  private final String updatedAt;

  public ProductData(
      long productId,
      String productCode,
      String productName,
      long categoryId,
      String categoryName,
      Long supplierId,
      String supplierName,
      int quantity,
      int reorderLevel,
      double unitPrice,
      String status,
      String createdAt,
      String updatedAt) {
    this.productId = productId;
    this.productCode = productCode;
    this.productName = productName;
    this.categoryId = categoryId;
    this.categoryName = categoryName;
    this.supplierId = supplierId;
    this.supplierName = supplierName;
    this.quantity = quantity;
    this.reorderLevel = reorderLevel;
    this.unitPrice = unitPrice;
    this.totalValue = quantity * unitPrice;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
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

  public long getCategoryId() {
    return categoryId;
  }

  public String getCategoryName() {
    return categoryName;
  }

  public Long getSupplierId() {
    return supplierId;
  }

  public String getSupplierName() {
    return supplierName;
  }

  public int getQuantity() {
    return quantity;
  }

  public int getReorderLevel() {
    return reorderLevel;
  }

  public double getUnitPrice() {
    return unitPrice;
  }

  public double getTotalValue() {
    return totalValue;
  }

  public String getStatus() {
    return status;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public String getUpdatedAt() {
    return updatedAt;
  }
}
