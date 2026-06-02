package com.group4.inventoryserver.dto.supplier;

public class SupplierData {

  private final long supplierId;
  private final String supplierCode;
  private final String supplierName;
  private final String contactPerson;
  private final String phone;
  private final String email;
  private final String address;
  private final String type;
  private final boolean preferred;
  private final int productCount;
  private final String status;
  private final String createdAt;
  private final String updatedAt;

  public SupplierData(
      long supplierId,
      String supplierCode,
      String supplierName,
      String contactPerson,
      String phone,
      String email,
      String address,
      String type,
      boolean preferred,
      int productCount,
      String status,
      String createdAt,
      String updatedAt) {
    this.supplierId = supplierId;
    this.supplierCode = supplierCode;
    this.supplierName = supplierName;
    this.contactPerson = contactPerson;
    this.phone = phone;
    this.email = email;
    this.address = address;
    this.type = type;
    this.preferred = preferred;
    this.productCount = productCount;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public long getSupplierId() {
    return supplierId;
  }

  public String getSupplierCode() {
    return supplierCode;
  }

  public String getSupplierName() {
    return supplierName;
  }

  public String getContactPerson() {
    return contactPerson;
  }

  public String getPhone() {
    return phone;
  }

  public String getEmail() {
    return email;
  }

  public String getAddress() {
    return address;
  }

  public String getType() {
    return type;
  }

  public boolean isPreferred() {
    return preferred;
  }

  public int getProductCount() {
    return productCount;
  }

  public String getStatus() {
    return status;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public String getUpdatedAt() {
    return updatedAt;
  }
}
