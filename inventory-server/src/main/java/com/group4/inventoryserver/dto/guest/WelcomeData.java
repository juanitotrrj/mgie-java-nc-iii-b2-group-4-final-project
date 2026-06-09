package com.group4.inventoryserver.dto.guest;

import java.util.List;

public class WelcomeData {

  private final String systemName;
  private final String headline;
  private final String description;
  private final List<String> guestCapabilities;
  private final List<String> actions;

  public WelcomeData(
      String systemName,
      String headline,
      String description,
      List<String> guestCapabilities,
      List<String> actions) {
    this.systemName = systemName;
    this.headline = headline;
    this.description = description;
    this.guestCapabilities = guestCapabilities;
    this.actions = actions;
  }

  public String getSystemName() {
    return systemName;
  }

  public String getHeadline() {
    return headline;
  }

  public String getDescription() {
    return description;
  }

  public List<String> getGuestCapabilities() {
    return guestCapabilities;
  }

  public List<String> getActions() {
    return actions;
  }
}
