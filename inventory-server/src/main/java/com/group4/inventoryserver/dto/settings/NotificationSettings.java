package com.group4.inventoryserver.dto.settings;

public class NotificationSettings {

  private Boolean enableEmailNotifications;
  private String smtpServer;
  private Integer port;
  private Boolean useSsl;
  private String fromEmail;
  private Boolean lowStockAlerts;
  private Boolean dailySummaryReports;
  private Boolean backupStatusNotifications;

  public Boolean getEnableEmailNotifications() {
    return enableEmailNotifications;
  }

  public void setEnableEmailNotifications(Boolean enableEmailNotifications) {
    this.enableEmailNotifications = enableEmailNotifications;
  }

  public String getSmtpServer() {
    return smtpServer;
  }

  public void setSmtpServer(String smtpServer) {
    this.smtpServer = smtpServer;
  }

  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  public Boolean getUseSsl() {
    return useSsl;
  }

  public void setUseSsl(Boolean useSsl) {
    this.useSsl = useSsl;
  }

  public String getFromEmail() {
    return fromEmail;
  }

  public void setFromEmail(String fromEmail) {
    this.fromEmail = fromEmail;
  }

  public Boolean getLowStockAlerts() {
    return lowStockAlerts;
  }

  public void setLowStockAlerts(Boolean lowStockAlerts) {
    this.lowStockAlerts = lowStockAlerts;
  }

  public Boolean getDailySummaryReports() {
    return dailySummaryReports;
  }

  public void setDailySummaryReports(Boolean dailySummaryReports) {
    this.dailySummaryReports = dailySummaryReports;
  }

  public Boolean getBackupStatusNotifications() {
    return backupStatusNotifications;
  }

  public void setBackupStatusNotifications(Boolean backupStatusNotifications) {
    this.backupStatusNotifications = backupStatusNotifications;
  }
}
