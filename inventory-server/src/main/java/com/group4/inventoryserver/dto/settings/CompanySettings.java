package com.group4.inventoryserver.dto.settings;

public class CompanySettings {

  private String companyName;
  private String address;
  private String contactNumber;
  private String emailAddress;
  private String website;
  private String fiscalYearStart;

  public String getCompanyName() {
    return companyName;
  }

  public void setCompanyName(String companyName) {
    this.companyName = companyName;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getContactNumber() {
    return contactNumber;
  }

  public void setContactNumber(String contactNumber) {
    this.contactNumber = contactNumber;
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  public String getWebsite() {
    return website;
  }

  public void setWebsite(String website) {
    this.website = website;
  }

  public String getFiscalYearStart() {
    return fiscalYearStart;
  }

  public void setFiscalYearStart(String fiscalYearStart) {
    this.fiscalYearStart = fiscalYearStart;
  }
}
