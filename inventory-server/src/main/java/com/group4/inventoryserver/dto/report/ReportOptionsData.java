package com.group4.inventoryserver.dto.report;

import java.util.List;

public class ReportOptionsData {

  private final List<ReportTypeOption> reportTypes;
  private final List<String> groupByOptions;
  private final List<CategoryOption> categories;
  private final List<SupplierOption> suppliers;

  public ReportOptionsData(
      List<ReportTypeOption> reportTypes,
      List<String> groupByOptions,
      List<CategoryOption> categories,
      List<SupplierOption> suppliers) {
    this.reportTypes = reportTypes;
    this.groupByOptions = groupByOptions;
    this.categories = categories;
    this.suppliers = suppliers;
  }

  public List<ReportTypeOption> getReportTypes() {
    return reportTypes;
  }

  public List<String> getGroupByOptions() {
    return groupByOptions;
  }

  public List<CategoryOption> getCategories() {
    return categories;
  }

  public List<SupplierOption> getSuppliers() {
    return suppliers;
  }

  public static class ReportTypeOption {
    private final String id;
    private final String name;

    public ReportTypeOption(String id, String name) {
      this.id = id;
      this.name = name;
    }

    public String getId() {
      return id;
    }

    public String getName() {
      return name;
    }
  }

  public static class CategoryOption {
    private final long categoryId;
    private final String categoryName;

    public CategoryOption(long categoryId, String categoryName) {
      this.categoryId = categoryId;
      this.categoryName = categoryName;
    }

    public long getCategoryId() {
      return categoryId;
    }

    public String getCategoryName() {
      return categoryName;
    }
  }

  public static class SupplierOption {
    private final long supplierId;
    private final String supplierName;

    public SupplierOption(long supplierId, String supplierName) {
      this.supplierId = supplierId;
      this.supplierName = supplierName;
    }

    public long getSupplierId() {
      return supplierId;
    }

    public String getSupplierName() {
      return supplierName;
    }
  }
}
