package com.group4.inventoryserver.dto.settings;

public class CurrencyTaxSettings {

  private String currency;
  private String currencySymbol;
  private Double defaultTaxRate;
  private String taxType;
  private Integer rounding;

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public String getCurrencySymbol() {
    return currencySymbol;
  }

  public void setCurrencySymbol(String currencySymbol) {
    this.currencySymbol = currencySymbol;
  }

  public Double getDefaultTaxRate() {
    return defaultTaxRate;
  }

  public void setDefaultTaxRate(Double defaultTaxRate) {
    this.defaultTaxRate = defaultTaxRate;
  }

  public String getTaxType() {
    return taxType;
  }

  public void setTaxType(String taxType) {
    this.taxType = taxType;
  }

  public Integer getRounding() {
    return rounding;
  }

  public void setRounding(Integer rounding) {
    this.rounding = rounding;
  }
}
