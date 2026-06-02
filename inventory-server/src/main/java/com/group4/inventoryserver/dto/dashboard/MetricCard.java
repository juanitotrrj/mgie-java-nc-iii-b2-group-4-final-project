package com.group4.inventoryserver.dto.dashboard;

public class MetricCard {

  private final String label;
  private final String value;
  private final String description;
  private final String severity;

  public MetricCard(String label, String value, String description, String severity) {
    this.label = label;
    this.value = value;
    this.description = description;
    this.severity = severity;
  }

  public String getLabel() {
    return label;
  }

  public String getValue() {
    return value;
  }

  public String getDescription() {
    return description;
  }

  public String getSeverity() {
    return severity;
  }
}
