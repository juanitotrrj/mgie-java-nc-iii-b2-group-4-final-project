package com.group4.inventoryserver.dto.report;

public class TopSellingProduct {

  private final int rank;
  private final long productId;
  private final String productCode;
  private final String productName;
  private final int quantitySold;
  private final double salesAmount;

  public TopSellingProduct(
      int rank,
      long productId,
      String productCode,
      String productName,
      int quantitySold,
      double salesAmount) {
    this.rank = rank;
    this.productId = productId;
    this.productCode = productCode;
    this.productName = productName;
    this.quantitySold = quantitySold;
    this.salesAmount = salesAmount;
  }

  public int getRank() {
    return rank;
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

  public int getQuantitySold() {
    return quantitySold;
  }

  public double getSalesAmount() {
    return salesAmount;
  }
}
