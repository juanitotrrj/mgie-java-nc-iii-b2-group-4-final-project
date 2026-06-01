package com.group4.inventoryserver.dto.guest;

public class ContactData {

  private final String supportEmail;
  private final String hotline;
  private final String mobileNumber;
  private final String officeAddress;
  private final String officeHours;
  private final String website;

  public ContactData(
      String supportEmail,
      String hotline,
      String mobileNumber,
      String officeAddress,
      String officeHours,
      String website) {
    this.supportEmail = supportEmail;
    this.hotline = hotline;
    this.mobileNumber = mobileNumber;
    this.officeAddress = officeAddress;
    this.officeHours = officeHours;
    this.website = website;
  }

  public String getSupportEmail() {
    return supportEmail;
  }

  public String getHotline() {
    return hotline;
  }

  public String getMobileNumber() {
    return mobileNumber;
  }

  public String getOfficeAddress() {
    return officeAddress;
  }

  public String getOfficeHours() {
    return officeHours;
  }

  public String getWebsite() {
    return website;
  }
}
