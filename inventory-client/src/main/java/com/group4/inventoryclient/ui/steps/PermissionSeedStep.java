package com.group4.inventoryclient.ui.steps;

import com.group4.inventoryclient.api.SetupApiClient;
import com.group4.inventoryclient.ui.WizardStep;
import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

public class PermissionSeedStep extends WizardStep {

  private static final Map<String, List<String>> DEFAULT_PERMISSIONS = new HashMap<>();

  static {
    DEFAULT_PERMISSIONS.put(
        "Admin",
        Arrays.asList(
            "PRODUCT_CREATE",
            "PRODUCT_READ",
            "PRODUCT_UPDATE",
            "PRODUCT_DELETE",
            "CATEGORY_CREATE",
            "CATEGORY_READ",
            "CATEGORY_UPDATE",
            "CATEGORY_DELETE",
            "SUPPLIER_CREATE",
            "SUPPLIER_READ",
            "SUPPLIER_UPDATE",
            "SUPPLIER_DELETE",
            "PURCHASE_CREATE",
            "PURCHASE_READ",
            "PURCHASE_UPDATE",
            "PURCHASE_DELETE",
            "SALE_CREATE",
            "SALE_READ",
            "SALE_UPDATE",
            "SALE_DELETE",
            "USER_CREATE",
            "USER_READ",
            "USER_UPDATE",
            "USER_DELETE",
            "ICR_CREATE",
            "ICR_READ",
            "ICR_UPDATE",
            "ICR_APPROVE",
            "STOCK_READ",
            "REPORT_READ",
            "EXPORT_DATA",
            "SETTINGS_MANAGE",
            "BACKUP_MANAGE",
            "AUDIT_READ",
            "DASHBOARD_VIEW"));
    DEFAULT_PERMISSIONS.put(
        "Manager",
        Arrays.asList(
            "PRODUCT_CREATE",
            "PRODUCT_READ",
            "PRODUCT_UPDATE",
            "CATEGORY_CREATE",
            "CATEGORY_READ",
            "CATEGORY_UPDATE",
            "SUPPLIER_CREATE",
            "SUPPLIER_READ",
            "SUPPLIER_UPDATE",
            "PURCHASE_CREATE",
            "PURCHASE_READ",
            "PURCHASE_UPDATE",
            "SALE_READ",
            "SALE_UPDATE",
            "USER_READ",
            "ICR_READ",
            "ICR_APPROVE",
            "STOCK_READ",
            "REPORT_READ",
            "EXPORT_DATA",
            "DASHBOARD_VIEW"));
    DEFAULT_PERMISSIONS.put(
        "Inventory Clerk",
        Arrays.asList(
            "PRODUCT_READ", "PRODUCT_UPDATE",
            "CATEGORY_READ", "SUPPLIER_READ",
            "ICR_CREATE", "ICR_READ",
            "STOCK_READ", "DASHBOARD_VIEW"));
    DEFAULT_PERMISSIONS.put(
        "Cashier", Arrays.asList("PRODUCT_READ", "SALE_CREATE", "SALE_READ", "DASHBOARD_VIEW"));
  }

  private final SetupApiClient api;
  private final JPanel panel;
  private final JTextArea summaryArea = new JTextArea(12, 45);

  public PermissionSeedStep(SetupApiClient api) {
    this.api = api;
    this.panel = buildPanel();
  }

  private JPanel buildPanel() {
    JPanel p = new JPanel(new BorderLayout(10, 10));
    p.setBorder(new TitledBorder("Seed Permissions"));

    summaryArea.setEditable(false);
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, List<String>> entry : DEFAULT_PERMISSIONS.entrySet()) {
      sb.append(entry.getKey()).append(":\n");
      for (String perm : entry.getValue()) {
        sb.append("  - ").append(perm).append("\n");
      }
      sb.append("\n");
    }
    summaryArea.setText(sb.toString());
    summaryArea.setCaretPosition(0);

    p.add(new JLabel("The following permission assignments will be seeded:"), BorderLayout.NORTH);
    p.add(new JScrollPane(summaryArea), BorderLayout.CENTER);
    p.add(
        new JLabel("<html><i>Existing assignments won't be duplicated (idempotent).</i></html>"),
        BorderLayout.SOUTH);

    return p;
  }

  @Override
  public JPanel getPanel() {
    return panel;
  }

  @Override
  public String getTitle() {
    return "Seed Permissions";
  }

  @Override
  public boolean validate() {
    return true;
  }

  @Override
  public void submit() throws Exception {
    api.seedPermissions(DEFAULT_PERMISSIONS);
  }
}
