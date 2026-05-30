package com.group4.inventoryserver.dto.guest;

import java.util.List;

public class AboutData {

  private final String overview;
  private final String purpose;
  private final List<String> coreModules;
  private final List<String> benefits;
  private final String guestLimitations;

  public AboutData(
      String overview,
      String purpose,
      List<String> coreModules,
      List<String> benefits,
      String guestLimitations) {
    this.overview = overview;
    this.purpose = purpose;
    this.coreModules = coreModules;
    this.benefits = benefits;
    this.guestLimitations = guestLimitations;
  }

  public String getOverview() {
    return overview;
  }

  public String getPurpose() {
    return purpose;
  }

  public List<String> getCoreModules() {
    return coreModules;
  }

  public List<String> getBenefits() {
    return benefits;
  }

  public String getGuestLimitations() {
    return guestLimitations;
  }
}
