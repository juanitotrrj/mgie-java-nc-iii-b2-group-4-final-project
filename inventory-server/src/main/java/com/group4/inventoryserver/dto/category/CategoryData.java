package com.group4.inventoryserver.dto.category;

public class CategoryData {

  private final long categoryId;
  private final String categoryCode;
  private final String categoryName;
  private final String description;
  private final String type;
  private final int productCount;
  private final String status;
  private final String createdAt;
  private final String updatedAt;

  public CategoryData(
      long categoryId,
      String categoryCode,
      String categoryName,
      String description,
      String type,
      int productCount,
      String status,
      String createdAt,
      String updatedAt) {
    this.categoryId = categoryId;
    this.categoryCode = categoryCode;
    this.categoryName = categoryName;
    this.description = description;
    this.type = type;
    this.productCount = productCount;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public long getCategoryId() {
    return categoryId;
  }

  public String getCategoryCode() {
    return categoryCode;
  }

  public String getCategoryName() {
    return categoryName;
  }

  public String getDescription() {
    return description;
  }

  public String getType() {
    return type;
  }

  public int getProductCount() {
    return productCount;
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
