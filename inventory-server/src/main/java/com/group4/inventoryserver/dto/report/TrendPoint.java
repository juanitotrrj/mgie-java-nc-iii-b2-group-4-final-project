package com.group4.inventoryserver.dto.report;

public class TrendPoint {

  private final String label;
  private final double value;

  public TrendPoint(String label, double value) {
    this.label = label;
    this.value = value;
  }

  public String getLabel() {
    return label;
  }

  public double getValue() {
    return value;
  }
}
