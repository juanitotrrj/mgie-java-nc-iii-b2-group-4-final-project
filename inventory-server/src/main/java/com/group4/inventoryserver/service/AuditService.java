package com.group4.inventoryserver.service;

import com.group4.inventoryserver.dto.PaginatedResponse;
import com.group4.inventoryserver.dto.PaginationMeta;
import com.group4.inventoryserver.dto.audit.AuditLogData;
import com.group4.inventoryserver.repository.AuditLogRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditService {

  private static final Logger log = LoggerFactory.getLogger(AuditService.class);

  private final AuditLogRepository auditLogRepository;

  public AuditService() {
    this(new AuditLogRepository());
  }

  public AuditService(AuditLogRepository auditLogRepository) {
    this.auditLogRepository = auditLogRepository;
  }

  public void logAction(
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

  public void logAction(
      Long userId,
      String username,
      String action,
      String module,
      String referenceNo,
      String details) {
    logAction(userId, username, action, module, referenceNo, details, null);
  }

  public PaginatedResponse<AuditLogData> list(
      int page,
      int size,
      String sortBy,
      String sortDir,
      String search,
      Long userId,
      String action,
      String module,
      String dateFrom,
      String dateTo) {
    int offset = (page - 1) * size;
    List<AuditLogData> data =
        auditLogRepository.findAll(
            offset, size, sortBy, sortDir, search, userId, action, module, dateFrom, dateTo);
    long total = auditLogRepository.count(search, userId, action, module, dateFrom, dateTo);
    PaginationMeta meta = new PaginationMeta(page, size, total, sortBy, sortDir);
    return new PaginatedResponse<>(data, meta);
  }
}
