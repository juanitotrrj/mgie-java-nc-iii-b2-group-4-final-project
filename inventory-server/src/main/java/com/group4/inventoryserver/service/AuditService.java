package com.group4.inventoryserver.service;

import com.group4.inventoryserver.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditService {

  private static final Logger log = LoggerFactory.getLogger(AuditService.class);
  private static final AuditLogRepository auditLogRepository = new AuditLogRepository();

  private AuditService() {}

  public static void log(
      Long userId,
      String username,
      String action,
      String module,
      String referenceNo,
      String details,
      String ipAddress) {
    try {
      auditLogRepository.insert(userId, username, action, module, referenceNo, details, ipAddress);
    } catch (Exception e) {
      log.error(
          "Failed to write audit log: action={}, module={}, error={}",
          action,
          module,
          e.getMessage());
    }
  }

  public static void log(
      Long userId,
      String username,
      String action,
      String module,
      String referenceNo,
      String details) {
    log(userId, username, action, module, referenceNo, details, null);
  }
}
