package com.group4.inventoryserver.dto.settings;

public class InventorySettings {

  private Integer lowStockThreshold;
  private Double reorderMultiplier;
  private String defaultProductStatus;
  private String costingMethod;
  private Boolean allowNegativeStock;
  private Boolean showDeleteConfirmation;
  private Boolean autoUpdateTotalValues;
  private Boolean warnWhenStockFallsBelowThreshold;
  private Boolean trackProductExpirationDates;
  private Integer defaultExpiryWarningDays;

  public Integer getLowStockThreshold() {
    return lowStockThreshold;
  }

  public void setLowStockThreshold(Integer lowStockThreshold) {
    this.lowStockThreshold = lowStockThreshold;
  }

  public Double getReorderMultiplier() {
    return reorderMultiplier;
  }

  public void setReorderMultiplier(Double reorderMultiplier) {
    this.reorderMultiplier = reorderMultiplier;
  }

  public String getDefaultProductStatus() {
    return defaultProductStatus;
  }

  public void setDefaultProductStatus(String defaultProductStatus) {
    this.defaultProductStatus = defaultProductStatus;
  }

  public String getCostingMethod() {
    return costingMethod;
  }

  public void setCostingMethod(String costingMethod) {
    this.costingMethod = costingMethod;
  }

  public Boolean getAllowNegativeStock() {
    return allowNegativeStock;
  }

  public void setAllowNegativeStock(Boolean allowNegativeStock) {
    this.allowNegativeStock = allowNegativeStock;
  }

  public Boolean getShowDeleteConfirmation() {
    return showDeleteConfirmation;
  }

  public void setShowDeleteConfirmation(Boolean showDeleteConfirmation) {
    this.showDeleteConfirmation = showDeleteConfirmation;
  }

  public Boolean getAutoUpdateTotalValues() {
    return autoUpdateTotalValues;
  }

  public void setAutoUpdateTotalValues(Boolean autoUpdateTotalValues) {
    this.autoUpdateTotalValues = autoUpdateTotalValues;
  }

  public Boolean getWarnWhenStockFallsBelowThreshold() {
    return warnWhenStockFallsBelowThreshold;
  }

  public void setWarnWhenStockFallsBelowThreshold(Boolean warnWhenStockFallsBelowThreshold) {
    this.warnWhenStockFallsBelowThreshold = warnWhenStockFallsBelowThreshold;
  }

  public Boolean getTrackProductExpirationDates() {
    return trackProductExpirationDates;
  }

  public void setTrackProductExpirationDates(Boolean trackProductExpirationDates) {
    this.trackProductExpirationDates = trackProductExpirationDates;
  }

  public Integer getDefaultExpiryWarningDays() {
    return defaultExpiryWarningDays;
  }

  public void setDefaultExpiryWarningDays(Integer defaultExpiryWarningDays) {
    this.defaultExpiryWarningDays = defaultExpiryWarningDays;
  }
}
