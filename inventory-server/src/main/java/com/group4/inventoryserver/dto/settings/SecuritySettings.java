package com.group4.inventoryserver.dto.settings;

public class SecuritySettings {

  private Integer sessionTimeoutMinutes;
  private String passwordPolicy;
  private Boolean requireLoginOnStartup;
  private Boolean allowMultipleConcurrentSessions;
  private Integer lockAccountAfterFailedAttempts;
  private Integer requirePasswordChangeEveryDays;
  private Integer minimumPasswordLength;
  private Integer passwordExpiryDays;

  public Integer getSessionTimeoutMinutes() {
    return sessionTimeoutMinutes;
  }

  public void setSessionTimeoutMinutes(Integer sessionTimeoutMinutes) {
    this.sessionTimeoutMinutes = sessionTimeoutMinutes;
  }

  public String getPasswordPolicy() {
    return passwordPolicy;
  }

  public void setPasswordPolicy(String passwordPolicy) {
    this.passwordPolicy = passwordPolicy;
  }

  public Boolean getRequireLoginOnStartup() {
    return requireLoginOnStartup;
  }

  public void setRequireLoginOnStartup(Boolean requireLoginOnStartup) {
    this.requireLoginOnStartup = requireLoginOnStartup;
  }

  public Boolean getAllowMultipleConcurrentSessions() {
    return allowMultipleConcurrentSessions;
  }

  public void setAllowMultipleConcurrentSessions(Boolean allowMultipleConcurrentSessions) {
    this.allowMultipleConcurrentSessions = allowMultipleConcurrentSessions;
  }

  public Integer getLockAccountAfterFailedAttempts() {
    return lockAccountAfterFailedAttempts;
  }

  public void setLockAccountAfterFailedAttempts(Integer lockAccountAfterFailedAttempts) {
    this.lockAccountAfterFailedAttempts = lockAccountAfterFailedAttempts;
  }

  public Integer getRequirePasswordChangeEveryDays() {
    return requirePasswordChangeEveryDays;
  }

  public void setRequirePasswordChangeEveryDays(Integer requirePasswordChangeEveryDays) {
    this.requirePasswordChangeEveryDays = requirePasswordChangeEveryDays;
  }

  public Integer getMinimumPasswordLength() {
    return minimumPasswordLength;
  }

  public void setMinimumPasswordLength(Integer minimumPasswordLength) {
    this.minimumPasswordLength = minimumPasswordLength;
  }

  public Integer getPasswordExpiryDays() {
    return passwordExpiryDays;
  }

  public void setPasswordExpiryDays(Integer passwordExpiryDays) {
    this.passwordExpiryDays = passwordExpiryDays;
  }
}
