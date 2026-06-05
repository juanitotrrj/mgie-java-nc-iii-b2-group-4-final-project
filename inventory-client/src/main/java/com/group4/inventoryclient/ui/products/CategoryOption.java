package com.group4.inventoryclient.ui.products;

public final class CategoryOption {

  private final long categoryId;
  private final String categoryName;

  public CategoryOption(long categoryId, String categoryName) {
    this.categoryId = categoryId;
    this.categoryName = categoryName;
  }

  public long getCategoryId() {
    return categoryId;
  }

  public String getCategoryName() {
    return categoryName;
  }

  @Override
  public String toString() {
    return categoryName;
  }
}
