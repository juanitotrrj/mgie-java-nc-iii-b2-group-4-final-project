package com.group4.inventoryserver.dto.guest;

public class InquiryData {

  private final long inquiryId;
  private final String submittedAt;

  public InquiryData(long inquiryId, String submittedAt) {
    this.inquiryId = inquiryId;
    this.submittedAt = submittedAt;
  }

  public long getInquiryId() {
    return inquiryId;
  }

  public String getSubmittedAt() {
    return submittedAt;
  }
}
