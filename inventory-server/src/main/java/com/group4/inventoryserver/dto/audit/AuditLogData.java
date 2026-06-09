package com.group4.inventoryserver.dto.audit;

public class AuditLogData {

  private final long auditLogId;
  private final String dateTime;
  private final Long userId;
  private final String username;
  private final String action;
  private final String module;
  private final String referenceNo;
  private final String details;

  public AuditLogData(
      long auditLogId,
      String dateTime,
      Long userId,
      String username,
      String action,
      String module,
      String referenceNo,
      String details) {
    this.auditLogId = auditLogId;
    this.dateTime = dateTime;
    this.userId = userId;
    this.username = username;
    this.action = action;
    this.module = module;
    this.referenceNo = referenceNo;
    this.details = details;
  }

  public long getAuditLogId() {
    return auditLogId;
  }

  public String getDateTime() {
    return dateTime;
  }

  public Long getUserId() {
    return userId;
  }

  public String getUsername() {
    return username;
  }

  public String getAction() {
    return action;
  }

  public String getModule() {
    return module;
  }

  public String getReferenceNo() {
    return referenceNo;
  }

  public String getDetails() {
    return details;
  }
}
