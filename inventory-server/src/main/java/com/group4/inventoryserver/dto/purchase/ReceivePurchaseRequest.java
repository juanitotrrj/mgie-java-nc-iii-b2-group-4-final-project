package com.group4.inventoryserver.dto.purchase;

public class ReceivePurchaseRequest {

  private String receivedDate;
  private String remarks;

  public String getReceivedDate() {
    return receivedDate;
  }

  public void setReceivedDate(String receivedDate) {
    this.receivedDate = receivedDate;
  }

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }
}
