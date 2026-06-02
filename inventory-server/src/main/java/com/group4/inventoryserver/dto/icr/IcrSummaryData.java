package com.group4.inventoryserver.dto.icr;

public class IcrSummaryData {

  private final long pending;
  private final long approved;
  private final long rejected;

  public IcrSummaryData(long pending, long approved, long rejected) {
    this.pending = pending;
    this.approved = approved;
    this.rejected = rejected;
  }

  public long getPending() {
    return pending;
  }

  public long getApproved() {
    return approved;
  }

  public long getRejected() {
    return rejected;
  }
}
