package com.group4.inventoryserver.dto.setup;

public class BusinessSettingsRequest {

  private String companyName;
  private String currencyCode;
  private String currencySymbol;
  private String currencyName;
  private double taxRate;
  private String taxType;
  private int priceDecimalPlaces;
  private String timezone;

  public String getCompanyName() {
    return companyName;
  }

  public void setCompanyName(String companyName) {
    this.companyName = companyName;
  }

  public String getCurrencyCode() {
    return currencyCode;
  }

  public void setCurrencyCode(String currencyCode) {
    this.currencyCode = currencyCode;
  }

  public String getCurrencySymbol() {
    return currencySymbol;
  }

  public void setCurrencySymbol(String currencySymbol) {
    this.currencySymbol = currencySymbol;
  }

  public String getCurrencyName() {
    return currencyName;
  }

  public void setCurrencyName(String currencyName) {
    this.currencyName = currencyName;
  }

  public double getTaxRate() {
    return taxRate;
  }

  public void setTaxRate(double taxRate) {
    this.taxRate = taxRate;
  }

  public String getTaxType() {
    return taxType;
  }

  public void setTaxType(String taxType) {
    this.taxType = taxType;
  }

  public int getPriceDecimalPlaces() {
    return priceDecimalPlaces;
  }

  public void setPriceDecimalPlaces(int priceDecimalPlaces) {
    this.priceDecimalPlaces = priceDecimalPlaces;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }
}
