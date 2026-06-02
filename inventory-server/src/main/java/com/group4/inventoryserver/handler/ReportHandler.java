package com.group4.inventoryserver.handler;

import com.group4.inventoryserver.config.EnvConfig;
import com.group4.inventoryserver.dto.report.CategoryValueBreakdown;
import com.group4.inventoryserver.dto.report.InventoryValueProduct;
import com.group4.inventoryserver.dto.report.InventoryValueReport;
import com.group4.inventoryserver.dto.report.LowStockItem;
import com.group4.inventoryserver.dto.report.LowStockReport;
import com.group4.inventoryserver.dto.report.PurchaseCostReport;
import com.group4.inventoryserver.dto.report.ReportOptionsData;
import com.group4.inventoryserver.dto.report.SalesSummaryReport;
import com.group4.inventoryserver.dto.report.TopSellingProduct;
import com.group4.inventoryserver.dto.report.TopSellingReport;
import com.group4.inventoryserver.dto.report.TrendPoint;
import com.group4.inventoryserver.exception.ApiException;
import com.group4.inventoryserver.exception.ValidationException;
import com.group4.inventoryserver.repository.ReportRepository;
import com.group4.inventoryserver.server.RequestContext;
import com.group4.inventoryserver.util.ExportUtil;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ReportHandler extends BaseHandler {

  private static final String BASE_PATH = EnvConfig.appContextPath() + "/reports";
  private static final String PERMISSION_READ = "REPORT_READ";
  private static final String PERMISSION_EXPORT = "EXPORT_DATA";

  private final ReportRepository reportRepository = new ReportRepository();

  @Override
  public void handle(RequestContext ctx) throws IOException {
    String pathParam = ctx.getPathParam(BASE_PATH);
    if (pathParam == null || pathParam.isEmpty()) {
      throw new ApiException(404, "Not found.");
    }
    if (!"GET".equals(ctx.getMethod())) {
      throw new ApiException(405, "Method Not Allowed: " + ctx.getMethod());
    }

    switch (pathParam) {
      case "options":
        requirePermission(ctx, PERMISSION_READ);
        handleOptions(ctx);
        break;
      case "sales-summary":
        requirePermission(ctx, PERMISSION_READ);
        handleSalesSummary(ctx);
        break;
      case "inventory-value":
        requirePermission(ctx, PERMISSION_READ);
        handleInventoryValue(ctx);
        break;
      case "purchase-cost":
        requirePermission(ctx, PERMISSION_READ);
        handlePurchaseCost(ctx);
        break;
      case "low-stock":
        requirePermission(ctx, PERMISSION_READ);
        handleLowStock(ctx);
        break;
      case "top-selling-products":
        requirePermission(ctx, PERMISSION_READ);
        handleTopSelling(ctx);
        break;
      case "export":
        requirePermission(ctx, PERMISSION_EXPORT);
        handleExport(ctx);
        break;
      default:
        throw new ApiException(404, "Not found.");
    }
  }

  private void handleOptions(RequestContext ctx) throws IOException {
    List<ReportOptionsData.ReportTypeOption> reportTypes =
        Arrays.asList(
            new ReportOptionsData.ReportTypeOption("sales-summary", "Sales Summary"),
            new ReportOptionsData.ReportTypeOption("inventory-value", "Inventory Value"),
            new ReportOptionsData.ReportTypeOption("purchase-cost", "Purchase Cost"),
            new ReportOptionsData.ReportTypeOption("low-stock", "Low Stock"),
            new ReportOptionsData.ReportTypeOption("top-selling-products", "Top Selling Products"));
    List<String> groupByOptions = Arrays.asList("DAY", "WEEK", "MONTH");
    List<ReportOptionsData.CategoryOption> categories = reportRepository.getActiveCategories();
    List<ReportOptionsData.SupplierOption> suppliers = reportRepository.getActiveSuppliers();
    ReportOptionsData data =
        new ReportOptionsData(reportTypes, groupByOptions, categories, suppliers);
    sendSuccess(ctx, data);
  }

  private void handleSalesSummary(RequestContext ctx) throws IOException {
    String dateFrom = getDateFrom(ctx);
    String dateTo = getDateTo(ctx);
    Long categoryId = parseLongOrNull(ctx.getQueryParam("categoryId"));
    String groupBy = ctx.getQueryParam("groupBy") != null ? ctx.getQueryParam("groupBy") : "DAY";

    double totalSales = reportRepository.getSalesTotalAmount(dateFrom, dateTo, categoryId);
    int totalOrders = reportRepository.getSalesOrderCount(dateFrom, dateTo, categoryId);
    String bestDay = reportRepository.getSalesBestDay(dateFrom, dateTo, categoryId);
    List<TrendPoint> trend = reportRepository.getSalesTrend(dateFrom, dateTo, categoryId, groupBy);
    List<TopSellingProduct> topProducts =
        reportRepository.getTopSellingProducts(dateFrom, dateTo, 5);

    long dayCount = daysBetween(dateFrom, dateTo);
    double averagePerDay = dayCount > 0 ? totalSales / dayCount : totalSales;

    SalesSummaryReport report =
        new SalesSummaryReport(
            dateFrom,
            dateTo,
            totalSales,
            Math.round(averagePerDay * 100.0) / 100.0,
            bestDay,
            totalOrders,
            trend,
            topProducts);
    sendSuccess(ctx, report);
  }

  private void handleInventoryValue(RequestContext ctx) throws IOException {
    Long categoryId = parseLongOrNull(ctx.getQueryParam("categoryId"));
    double totalValue = reportRepository.getTotalInventoryValue(categoryId);
    List<CategoryValueBreakdown> byCategory =
        reportRepository.getInventoryValueByCategory(categoryId);
    List<InventoryValueProduct> products = reportRepository.getInventoryValueProducts(categoryId);
    InventoryValueReport report = new InventoryValueReport(totalValue, byCategory, products);
    sendSuccess(ctx, report);
  }

  private void handlePurchaseCost(RequestContext ctx) throws IOException {
    String dateFrom = getDateFrom(ctx);
    String dateTo = getDateTo(ctx);
    Long supplierId = parseLongOrNull(ctx.getQueryParam("supplierId"));
    Long categoryId = parseLongOrNull(ctx.getQueryParam("categoryId"));

    double totalCost =
        reportRepository.getPurchaseCostTotal(dateFrom, dateTo, supplierId, categoryId);
    int totalOrders =
        reportRepository.getPurchaseOrderCount(dateFrom, dateTo, supplierId, categoryId);
    List<TrendPoint> trend =
        reportRepository.getPurchaseCostTrend(dateFrom, dateTo, supplierId, categoryId);

    PurchaseCostReport report =
        new PurchaseCostReport(dateFrom, dateTo, totalCost, totalOrders, trend);
    sendSuccess(ctx, report);
  }

  private void handleLowStock(RequestContext ctx) throws IOException {
    Long categoryId = parseLongOrNull(ctx.getQueryParam("categoryId"));
    int count = reportRepository.getLowStockCount(categoryId);
    List<LowStockItem> items = reportRepository.getLowStockProducts(categoryId);
    LowStockReport report = new LowStockReport(count, items);
    sendSuccess(ctx, report);
  }

  private void handleTopSelling(RequestContext ctx) throws IOException {
    String dateFrom = getDateFrom(ctx);
    String dateTo = getDateTo(ctx);
    int limit = parseIntOrDefault(ctx.getQueryParam("limit"), 10);
    if (limit < 1) limit = 1;
    if (limit > 100) limit = 100;

    List<TopSellingProduct> items = reportRepository.getTopSellingProducts(dateFrom, dateTo, limit);
    TopSellingReport report = new TopSellingReport(dateFrom, dateTo, items);
    sendSuccess(ctx, report);
  }

  private void handleExport(RequestContext ctx) throws IOException {
    String reportType = ctx.getQueryParam("reportType");
    if (reportType == null || reportType.trim().isEmpty()) {
      throw new ValidationException("Report type is required.");
    }

    String format = ctx.getQueryParam("format");
    if (format == null || format.trim().isEmpty()) {
      format = EnvConfig.exportDefaultFormat();
    }
    format = format.toLowerCase();
    if (!"csv".equals(format) && !"xlsx".equals(format)) {
      throw new ValidationException("Supported formats: csv, xlsx");
    }
    if ("xlsx".equals(format) && !EnvConfig.excelExportEnabled()) {
      throw new ValidationException("XLSX export is disabled.");
    }
    if ("csv".equals(format) && !EnvConfig.csvExportEnabled()) {
      throw new ValidationException("CSV export is disabled.");
    }

    String dateFrom = getDateFrom(ctx);
    String dateTo = getDateTo(ctx);
    Long categoryId = parseLongOrNull(ctx.getQueryParam("categoryId"));

    String[] headers;
    List<String[]> rows;

    switch (reportType) {
      case "sales-summary":
        headers = new String[] {"Date", "Total Sales"};
        List<TrendPoint> salesTrend =
            reportRepository.getSalesTrend(dateFrom, dateTo, categoryId, "DAY");
        rows = new ArrayList<>();
        for (TrendPoint tp : salesTrend) {
          rows.add(new String[] {tp.getLabel(), String.valueOf(tp.getValue())});
        }
        break;
      case "inventory-value":
        headers =
            new String[] {
              "Product Code", "Product Name", "Category", "Quantity", "Unit Price", "Total Value"
            };
        List<InventoryValueProduct> ivProducts =
            reportRepository.getInventoryValueProducts(categoryId);
        rows = new ArrayList<>();
        for (InventoryValueProduct p : ivProducts) {
          rows.add(
              new String[] {
                p.getProductCode(),
                p.getProductName(),
                p.getCategoryName(),
                String.valueOf(p.getQuantity()),
                String.valueOf(p.getPrice()),
                String.valueOf(p.getTotalValue())
              });
        }
        break;
      case "purchase-cost":
        headers = new String[] {"Date", "Total Cost"};
        Long supplierId = parseLongOrNull(ctx.getQueryParam("supplierId"));
        List<TrendPoint> purchaseTrend =
            reportRepository.getPurchaseCostTrend(dateFrom, dateTo, supplierId, categoryId);
        rows = new ArrayList<>();
        for (TrendPoint tp : purchaseTrend) {
          rows.add(new String[] {tp.getLabel(), String.valueOf(tp.getValue())});
        }
        break;
      case "low-stock":
        headers =
            new String[] {
              "Product Code", "Product Name", "Category", "Quantity", "Reorder Level", "Deficit"
            };
        List<LowStockItem> lowItems = reportRepository.getLowStockProducts(categoryId);
        rows = new ArrayList<>();
        for (LowStockItem item : lowItems) {
          rows.add(
              new String[] {
                item.getProductCode(),
                item.getProductName(),
                item.getCategoryName(),
                String.valueOf(item.getQuantity()),
                String.valueOf(item.getReorderLevel()),
                String.valueOf(item.getDeficit())
              });
        }
        break;
      case "top-selling-products":
        headers =
            new String[] {"Rank", "Product Code", "Product Name", "Quantity Sold", "Sales Amount"};
        int limit = parseIntOrDefault(ctx.getQueryParam("limit"), 10);
        if (limit < 1) limit = 1;
        if (limit > 100) limit = 100;
        List<TopSellingProduct> topItems =
            reportRepository.getTopSellingProducts(dateFrom, dateTo, limit);
        rows = new ArrayList<>();
        for (TopSellingProduct tp : topItems) {
          rows.add(
              new String[] {
                String.valueOf(tp.getRank()),
                tp.getProductCode(),
                tp.getProductName(),
                String.valueOf(tp.getQuantitySold()),
                String.valueOf(tp.getSalesAmount())
              });
        }
        break;
      default:
        throw new ValidationException(
            "Invalid report type. Valid: sales-summary, inventory-value, purchase-cost, "
                + "low-stock, top-selling-products");
    }

    byte[] content;
    String contentType;
    String extension;
    if ("xlsx".equals(format)) {
      content = ExportUtil.toXlsx(reportType, headers, rows);
      contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
      extension = "xlsx";
    } else {
      content = ExportUtil.toCsv(headers, rows);
      contentType = "text/csv; charset=UTF-8";
      extension = "csv";
    }

    String filename = reportType + "-" + dateFrom + "-to-" + dateTo + "." + extension;
    ctx.getExchange().getResponseHeaders().set("Content-Type", contentType);
    ctx.getExchange()
        .getResponseHeaders()
        .set("Content-Disposition", "attachment; filename=\"" + filename + "\"");
    ctx.getExchange().sendResponseHeaders(200, content.length);
    try (OutputStream os = ctx.getExchange().getResponseBody()) {
      os.write(content);
    }
  }

  // --- Date helpers ---

  private String getDateFrom(RequestContext ctx) {
    String dateFrom = ctx.getQueryParam("dateFrom");
    if (dateFrom != null && !dateFrom.trim().isEmpty()) return dateFrom.trim();
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.DAY_OF_MONTH, 1);
    return new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
  }

  private String getDateTo(RequestContext ctx) {
    String dateTo = ctx.getQueryParam("dateTo");
    if (dateTo != null && !dateTo.trim().isEmpty()) return dateTo.trim();
    return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
  }

  private long daysBetween(String dateFrom, String dateTo) {
    try {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      Date from = sdf.parse(dateFrom);
      Date to = sdf.parse(dateTo);
      long diff = to.getTime() - from.getTime();
      return Math.max(1, diff / (1000 * 60 * 60 * 24) + 1);
    } catch (Exception e) {
      return 1;
    }
  }

  private Long parseLongOrNull(String value) {
    if (value == null || value.trim().isEmpty()) return null;
    try {
      return Long.parseLong(value.trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private int parseIntOrDefault(String value, int defaultValue) {
    if (value == null || value.trim().isEmpty()) return defaultValue;
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }
}
