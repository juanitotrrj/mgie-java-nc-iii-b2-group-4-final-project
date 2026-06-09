package com.group4.inventoryserver.dto.settings;

public class SystemSettings {

  private CompanySettings company;
  private InventorySettings inventory;
  private CurrencyTaxSettings currencyTax;
  private SecuritySettings security;
  private NotificationSettings notifications;
  private DatabaseSettings database;

  public CompanySettings getCompany() {
    return company;
  }

  public void setCompany(CompanySettings company) {
    this.company = company;
  }

  public InventorySettings getInventory() {
    return inventory;
  }

  public void setInventory(InventorySettings inventory) {
    this.inventory = inventory;
  }

  public CurrencyTaxSettings getCurrencyTax() {
    return currencyTax;
  }

  public void setCurrencyTax(CurrencyTaxSettings currencyTax) {
    this.currencyTax = currencyTax;
  }

  public SecuritySettings getSecurity() {
    return security;
  }

  public void setSecurity(SecuritySettings security) {
    this.security = security;
  }

  public NotificationSettings getNotifications() {
    return notifications;
  }

  public void setNotifications(NotificationSettings notifications) {
    this.notifications = notifications;
  }

  public DatabaseSettings getDatabase() {
    return database;
  }

  public void setDatabase(DatabaseSettings database) {
    this.database = database;
  }
}
