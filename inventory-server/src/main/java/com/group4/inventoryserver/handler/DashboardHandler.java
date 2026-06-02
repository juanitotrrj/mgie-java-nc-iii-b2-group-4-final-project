package com.group4.inventoryserver.handler;

import com.group4.inventoryserver.config.EnvConfig;
import com.group4.inventoryserver.dto.dashboard.AdminDashboardData;
import com.group4.inventoryserver.dto.dashboard.CashierDashboardData;
import com.group4.inventoryserver.dto.dashboard.ClerkDashboardData;
import com.group4.inventoryserver.dto.dashboard.ManagerDashboardData;
import com.group4.inventoryserver.dto.dashboard.MetricCard;
import com.group4.inventoryserver.exception.ApiException;
import com.group4.inventoryserver.repository.DashboardRepository;
import com.group4.inventoryserver.server.RequestContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardHandler extends BaseHandler {

  private static final String BASE_PATH = EnvConfig.appContextPath() + "/dashboard";
  private static final String PERMISSION = "DASHBOARD_VIEW";

  private final DashboardRepository dashboardRepository = new DashboardRepository();

  @Override
  public void handle(RequestContext ctx) throws IOException {
    requirePermission(ctx, PERMISSION);
    String pathParam = ctx.getPathParam(BASE_PATH);

    if (pathParam == null || pathParam.isEmpty()) {
      throw new ApiException(404, "Not found.");
    }

    if (!"GET".equals(ctx.getMethod())) {
      throw new ApiException(405, "Method Not Allowed: " + ctx.getMethod());
    }

    switch (pathParam) {
      case "admin":
        handleAdmin(ctx);
        break;
      case "manager":
        handleManager(ctx);
        break;
      case "clerk":
        handleClerk(ctx);
        break;
      case "cashier":
        handleCashier(ctx);
        break;
      default:
        throw new ApiException(404, "Not found.");
    }
  }

  private void handleAdmin(RequestContext ctx) throws IOException {
    List<MetricCard> cards = new ArrayList<>();
    cards.add(
        new MetricCard(
            "Total Products",
            String.valueOf(dashboardRepository.countActiveProducts()),
            "Active products in inventory",
            "normal"));
    cards.add(
        new MetricCard(
            "Total Users",
            String.valueOf(dashboardRepository.countActiveUsers()),
            "Active user accounts",
            "normal"));

    double todaySales = dashboardRepository.getTodaySalesTotal();
    cards.add(
        new MetricCard(
            "Today's Sales",
            String.format("%.2f", todaySales),
            "Total sales amount today",
            "normal"));

    int lowStock = dashboardRepository.countLowStockProducts();
    cards.add(
        new MetricCard(
            "Low Stock Items",
            String.valueOf(lowStock),
            "Products at or below reorder level",
            lowStock > 0 ? "warning" : "normal"));

    List<Map<String, Object>> recentActivity = dashboardRepository.getRecentAuditLogs(10);
    String systemStatus = "Healthy";

    AdminDashboardData data = new AdminDashboardData(cards, recentActivity, systemStatus);
    sendSuccess(ctx, data);
  }

  private void handleManager(RequestContext ctx) throws IOException {
    List<MetricCard> cards = new ArrayList<>();
    int pending = dashboardRepository.countPendingApprovals();
    cards.add(
        new MetricCard(
            "Pending Approvals",
            String.valueOf(pending),
            "Inventory change requests awaiting review",
            pending > 0 ? "warning" : "normal"));
    cards.add(
        new MetricCard(
            "Monthly Revenue",
            String.format("%.2f", dashboardRepository.getMonthlyRevenue()),
            "Total revenue this month",
            "normal"));
    cards.add(
        new MetricCard(
            "Purchase Orders",
            String.valueOf(dashboardRepository.countMonthlyPurchaseOrders()),
            "Purchase orders this month",
            "normal"));

    ManagerDashboardData data =
        new ManagerDashboardData(
            cards,
            dashboardRepository.getPendingIcrs(5),
            dashboardRepository.getRecentStockMovements(10),
            dashboardRepository.getTopSellingProducts(5));
    sendSuccess(ctx, data);
  }

  private void handleClerk(RequestContext ctx) throws IOException {
    long userId = getAuthUserId(ctx);
    List<MetricCard> cards = new ArrayList<>();

    int myPending = dashboardRepository.countPendingRequestsByUser(userId);
    cards.add(
        new MetricCard(
            "My Pending Requests",
            String.valueOf(myPending),
            "Your requests awaiting approval",
            myPending > 0 ? "info" : "normal"));
    cards.add(
        new MetricCard(
            "Approved Requests",
            String.valueOf(dashboardRepository.countApprovedRequestsByUser(userId)),
            "Your approved requests",
            "normal"));

    int lowStock = dashboardRepository.countLowStockProducts();
    cards.add(
        new MetricCard(
            "Low Stock Products",
            String.valueOf(lowStock),
            "Products below reorder level",
            lowStock > 0 ? "warning" : "normal"));

    Map<String, Integer> requestSummary = new LinkedHashMap<>();
    requestSummary.put("pending", myPending);
    requestSummary.put("approved", dashboardRepository.countApprovedRequestsByUser(userId));
    requestSummary.put("rejected", dashboardRepository.countRejectedRequestsByUser(userId));

    ClerkDashboardData data =
        new ClerkDashboardData(
            cards, requestSummary, dashboardRepository.getRecentStockMovements(10));
    sendSuccess(ctx, data);
  }

  private void handleCashier(RequestContext ctx) throws IOException {
    List<MetricCard> cards = new ArrayList<>();
    int todayCount = dashboardRepository.getTodaySalesCount();
    double todayRevenue = dashboardRepository.getTodaySalesTotal();
    double avgSale = dashboardRepository.getAverageSaleToday();

    cards.add(
        new MetricCard(
            "Today's Sales", String.valueOf(todayCount), "Number of sales today", "normal"));
    cards.add(
        new MetricCard(
            "Today's Revenue",
            String.format("%.2f", todayRevenue),
            "Total revenue today",
            "normal"));
    cards.add(
        new MetricCard(
            "Average Sale", String.format("%.2f", avgSale), "Average sale value today", "normal"));

    CashierDashboardData data =
        new CashierDashboardData(cards, dashboardRepository.getRecentSales(10));
    sendSuccess(ctx, data);
  }
}
