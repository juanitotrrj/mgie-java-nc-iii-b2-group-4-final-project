package com.group4.inventoryclient.ui;

import com.group4.inventoryclient.api.ApiClient;
import com.group4.inventoryclient.api.CategoryApiClient;
import com.group4.inventoryclient.api.DashboardApiClient;
import com.group4.inventoryclient.api.ProductApiClient;
import com.group4.inventoryclient.api.SupplierApiClient;
import com.group4.inventoryclient.session.SessionManager;
import com.group4.inventoryclient.ui.audit.AuditLogPanel;
import com.group4.inventoryclient.ui.categories.CategoryListPanel;
import com.group4.inventoryclient.ui.dashboard.AdminDashboard;
import com.group4.inventoryclient.ui.dashboard.CashierDashboard;
import com.group4.inventoryclient.ui.dashboard.ClerkDashboard;
import com.group4.inventoryclient.ui.dashboard.ManagerDashboard;
import com.group4.inventoryclient.ui.icr.IcrListPanel;
import com.group4.inventoryclient.ui.products.ProductListPanel;
import com.group4.inventoryclient.ui.purchases.PurchaseListPanel;
import com.group4.inventoryclient.ui.reports.ReportPanel;
import com.group4.inventoryclient.ui.sales.SaleListPanel;
import com.group4.inventoryclient.ui.settings.SettingsPanel;
import com.group4.inventoryclient.ui.stock.StockMovementPanel;
import com.group4.inventoryclient.ui.suppliers.SupplierListPanel;
import com.group4.inventoryclient.ui.users.UserListPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class MainFrame extends JFrame {

  private final ApiClient apiClient;
  private final SessionManager session = SessionManager.getInstance();
  private final CardLayout contentLayout = new CardLayout();
  private final JPanel contentPanel = new JPanel(contentLayout);
  private final DefaultListModel<String> navModel = new DefaultListModel<>();
  private final JList<String> navList = new JList<>(navModel);

  public MainFrame(ApiClient apiClient) {
    super("G4 Inventory Management System");
    this.apiClient = apiClient;
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setMinimumSize(new Dimension(1100, 700));
    setLocationRelativeTo(null);
    initUI();
  }

  private void initUI() {
    JPanel sidebar = buildSidebar();
    buildContentPanels();

    JPanel headerPanel = new JPanel(new BorderLayout());
    headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    JLabel userLabel = new JLabel(session.getFullName() + " (" + session.getRole() + ")");
    userLabel.setFont(userLabel.getFont().deriveFont(Font.BOLD));
    headerPanel.add(userLabel, BorderLayout.WEST);
    JButton logoutBtn = new JButton("Logout");
    logoutBtn.addActionListener(e -> logout());
    headerPanel.add(logoutBtn, BorderLayout.EAST);

    setLayout(new BorderLayout());
    add(headerPanel, BorderLayout.NORTH);
    add(sidebar, BorderLayout.WEST);
    add(contentPanel, BorderLayout.CENTER);

    navList.setSelectedIndex(0);
  }

  private JPanel buildSidebar() {
    JPanel sidebar = new JPanel(new BorderLayout());
    sidebar.setPreferredSize(new Dimension(180, 0));
    sidebar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));

    navModel.addElement("Dashboard");
    if (session.hasPermission("PRODUCT_READ")) navModel.addElement("Products");
    if (session.hasPermission("CATEGORY_READ")) navModel.addElement("Categories");
    if (session.hasPermission("SUPPLIER_READ")) navModel.addElement("Suppliers");
    if (session.hasPermission("PURCHASE_READ")) navModel.addElement("Purchases");
    if (session.hasPermission("SALE_READ")) navModel.addElement("Sales");
    if (session.hasPermission("INVENTORY_CHANGE_REQUEST_CREATE")
        || session.hasPermission("INVENTORY_CHANGE_REQUEST_REVIEW"))
      navModel.addElement("Change Requests");
    if (session.hasPermission("STOCK_MOVEMENT_READ")) navModel.addElement("Stock Movements");
    if (session.hasPermission("REPORT_READ")) navModel.addElement("Reports");
    if (session.hasPermission("USER_MANAGE")) navModel.addElement("Users");
    if (session.hasPermission("SETTINGS_MANAGE")) navModel.addElement("Settings");
    if (session.hasPermission("AUDIT_LOG_READ")) navModel.addElement("Audit Logs");

    navList.setFont(navList.getFont().deriveFont(13f));
    navList.addListSelectionListener(
        e -> {
          if (!e.getValueIsAdjusting()) {
            String selected = navList.getSelectedValue();
            if (selected != null) showPanel(selected);
          }
        });

    sidebar.add(new JScrollPane(navList), BorderLayout.CENTER);
    return sidebar;
  }

  private void buildContentPanels() {
    DashboardApiClient dashApi = new DashboardApiClient(apiClient);
    String role = session.getRole();
    JPanel dashboard;
    switch (role != null ? role : "") {
      case "Administrator":
        dashboard = new AdminDashboard(dashApi);
        break;
      case "Manager":
        dashboard = new ManagerDashboard(dashApi);
        break;
      case "Inventory Clerk":
        dashboard = new ClerkDashboard(dashApi);
        break;
      case "Cashier":
        dashboard = new CashierDashboard(dashApi);
        break;
      default:
        dashboard = new AdminDashboard(dashApi);
        break;
    }
    contentPanel.add(dashboard, "Dashboard");
    contentPanel.add(
        new ProductListPanel(new ProductApiClient(apiClient), apiClient, this), "Products");
    contentPanel.add(
        new CategoryListPanel(new CategoryApiClient(apiClient), apiClient, this), "Categories");
    contentPanel.add(
        new SupplierListPanel(new SupplierApiClient(apiClient), apiClient, this), "Suppliers");
    contentPanel.add(new PurchaseListPanel(apiClient, this), "Purchases");
    contentPanel.add(new SaleListPanel(apiClient, this), "Sales");
    contentPanel.add(new IcrListPanel(apiClient, this), "Change Requests");
    contentPanel.add(new StockMovementPanel(apiClient), "Stock Movements");
    contentPanel.add(new ReportPanel(apiClient), "Reports");
    contentPanel.add(new UserListPanel(apiClient), "Users");
    contentPanel.add(new SettingsPanel(apiClient), "Settings");
    contentPanel.add(new AuditLogPanel(apiClient), "Audit Logs");
  }

  private void showPanel(String name) {
    contentLayout.show(contentPanel, name);
  }

  private void logout() {
    new Thread(
            () -> {
              try {
                new com.group4.inventoryclient.api.AuthApiClient(apiClient).logout();
              } catch (Exception ignored) {
              }
              apiClient.setBearerToken(null);
              session.logout();
              SwingUtilities.invokeLater(
                  () -> {
                    dispose();
                    GuestFrame guest = new GuestFrame(apiClient);
                    guest.setVisible(true);
                  });
            })
        .start();
  }
}
