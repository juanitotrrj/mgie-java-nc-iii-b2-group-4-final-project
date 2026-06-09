package com.group4.inventoryclient.ui.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.group4.inventoryclient.api.ApiClient;
import com.group4.inventoryclient.api.SettingsApiClient;
import com.group4.inventoryclient.util.SwingUtil;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class SettingsPanel extends JPanel {

  private final SettingsApiClient settingsApiClient;
  private final JTabbedPane tabbedPane;
  private final Map<String, SettingsTab> tabs = new HashMap<>();
  private JsonObject cachedSettings;

  public SettingsPanel(ApiClient apiClient) {
    super(new BorderLayout());
    this.settingsApiClient = new SettingsApiClient(apiClient);

    tabbedPane = new JTabbedPane();

    tabs.put(
        "company",
        new SettingsTab(
            "company",
            this::updateCompany,
            new String[][] {
              {"companyName", "Company Name", "text"},
              {"address", "Address", "text"},
              {"contactNumber", "Contact Number", "text"},
              {"emailAddress", "Email Address", "text"},
              {"website", "Website", "text"},
              {"fiscalYearStart", "Fiscal Year Start", "text"}
            }));

    tabs.put(
        "inventory",
        new SettingsTab(
            "inventory",
            this::updateInventory,
            new String[][] {
              {"lowStockThreshold", "Low Stock Threshold", "text"},
              {"reorderMultiplier", "Reorder Multiplier", "text"},
              {"defaultProductStatus", "Default Product Status", "text"},
              {"costingMethod", "Costing Method", "combo:FIFO,LIFO,Average"},
              {"allowNegativeStock", "Allow Negative Stock", "bool"},
              {"showDeleteConfirmation", "Show Delete Confirmation", "bool"},
              {"autoUpdateTotalValues", "Auto Update Total Values", "bool"},
              {"warnWhenStockFallsBelowThreshold", "Warn Below Threshold", "bool"}
            }));

    tabs.put(
        "security",
        new SettingsTab(
            "security",
            this::updateSecurity,
            new String[][] {
              {"sessionTimeoutMinutes", "Session Timeout (minutes)", "text"},
              {"passwordPolicy", "Password Policy", "combo:Standard,Strong,Custom"},
              {"lockAccountAfterFailedAttempts", "Lock After Failed Attempts", "text"},
              {"minimumPasswordLength", "Minimum Password Length", "text"},
              {"passwordExpiryDays", "Password Expiry (days)", "text"},
              {"requireLoginOnStartup", "Require Login On Startup", "bool"}
            }));

    tabs.put(
        "notifications",
        new SettingsTab(
            "notifications",
            this::updateNotifications,
            new String[][] {
              {"enableEmailNotifications", "Enable Email Notifications", "bool"},
              {"smtpServer", "SMTP Server", "text"},
              {"port", "SMTP Port", "text"},
              {"useSsl", "Use SSL", "bool"},
              {"fromEmail", "From Email", "text"},
              {"lowStockAlerts", "Low Stock Alerts", "bool"},
              {"dailySummaryReports", "Daily Summary Reports", "bool"}
            }));

    tabbedPane.addTab("Company", tabs.get("company"));
    tabbedPane.addTab("Inventory", tabs.get("inventory"));
    tabbedPane.addTab("Security", tabs.get("security"));
    tabbedPane.addTab("Notifications", tabs.get("notifications"));
    tabbedPane.addTab("Database", createDatabaseTab());

    tabbedPane.addChangeListener(
        e -> {
          int index = tabbedPane.getSelectedIndex();
          if (index < 0 || index >= tabbedPane.getTabCount()) {
            return;
          }
          String title = tabbedPane.getTitleAt(index).toLowerCase();
          if (tabs.containsKey(title)) {
            tabs.get(title).populateFromCache();
          }
        });

    add(tabbedPane, BorderLayout.CENTER);
    loadAllSettings();
  }

  private ApiClient.ApiResponse updateCompany(Map<String, Object> body) throws java.io.IOException {
    return settingsApiClient.updateCompany(body);
  }

  private ApiClient.ApiResponse updateInventory(Map<String, Object> body)
      throws java.io.IOException {
    return settingsApiClient.updateInventory(body);
  }

  private ApiClient.ApiResponse updateSecurity(Map<String, Object> body)
      throws java.io.IOException {
    return settingsApiClient.updateSecurity(body);
  }

  private ApiClient.ApiResponse updateNotifications(Map<String, Object> body)
      throws java.io.IOException {
    return settingsApiClient.updateNotifications(body);
  }

  private JPanel createDatabaseTab() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(10, 10, 10, 10);
    gbc.anchor = GridBagConstraints.CENTER;

    JButton testConnectionBtn = new JButton("Test Connection");
    JButton backupBtn = new JButton("Backup Database");

    testConnectionBtn.addActionListener(e -> testConnection());
    backupBtn.addActionListener(e -> backupDatabase());

    gbc.gridx = 0;
    gbc.gridy = 0;
    panel.add(testConnectionBtn, gbc);

    gbc.gridy = 1;
    panel.add(backupBtn, gbc);

    return panel;
  }

  private void loadAllSettings() {
    new Thread(
            () -> {
              try {
                ApiClient.ApiResponse response = settingsApiClient.getAll();
                SwingUtilities.invokeLater(
                    () -> {
                      if (response.isSuccess()) {
                        cachedSettings = response.getDataAsObject();
                        int index = tabbedPane.getSelectedIndex();
                        if (index >= 0) {
                          String title = tabbedPane.getTitleAt(index).toLowerCase();
                          if (tabs.containsKey(title)) {
                            tabs.get(title).populateFromCache();
                          }
                        }
                      } else {
                        SwingUtil.showError(this, response.getErrorMessage());
                      }
                    });
              } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> SwingUtil.showError(this, ex.getMessage()));
              }
            })
        .start();
  }

  private void testConnection() {
    new Thread(
            () -> {
              try {
                ApiClient.ApiResponse response = settingsApiClient.testConnection();
                SwingUtilities.invokeLater(
                    () -> {
                      if (response.isSuccess()) {
                        JsonObject data = response.getDataAsObject();
                        String status =
                            data.has("status") ? data.get("status").getAsString() : "Unknown";
                        long responseTime =
                            data.has("responseTimeMs") ? data.get("responseTimeMs").getAsLong() : 0;
                        SwingUtil.showInfo(
                            this, "Database: " + status + " (" + responseTime + " ms)");
                      } else {
                        SwingUtil.showError(this, response.getErrorMessage());
                      }
                    });
              } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> SwingUtil.showError(this, ex.getMessage()));
              }
            })
        .start();
  }

  private void backupDatabase() {
    new Thread(
            () -> {
              try {
                ApiClient.ApiResponse response = settingsApiClient.backup();
                SwingUtilities.invokeLater(
                    () -> {
                      if (response.isSuccess()) {
                        JsonObject data = response.getDataAsObject();
                        String filename =
                            data.has("filename") ? data.get("filename").getAsString() : "backup";
                        SwingUtil.showInfo(this, "Backup created: " + filename);
                      } else {
                        SwingUtil.showError(this, response.getErrorMessage());
                      }
                    });
              } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> SwingUtil.showError(this, ex.getMessage()));
              }
            })
        .start();
  }

  private class SettingsTab extends JPanel {

    private final String sectionKey;
    private final SettingsUpdater updater;
    private final Map<String, JComponent> fields = new HashMap<>();
    private final JButton saveBtn = new JButton("Save");

    public SettingsTab(String sectionKey, SettingsUpdater updater, String[][] fieldDefinitions) {
      super(new BorderLayout());
      this.sectionKey = sectionKey;
      this.updater = updater;

      JPanel formPanel = new JPanel(new GridBagLayout());
      formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

      GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = new Insets(5, 5, 5, 5);
      gbc.anchor = GridBagConstraints.WEST;

      int row = 0;
      for (String[] fieldDef : fieldDefinitions) {
        String key = fieldDef[0];
        String label = fieldDef[1];
        String type = fieldDef[2];

        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel(label + ":"), gbc);

        JComponent field = createField(type);
        fields.put(key, field);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(field, gbc);
        row++;
      }

      JPanel buttonPanel = new JPanel();
      buttonPanel.add(saveBtn);
      saveBtn.addActionListener(e -> saveSettings());

      add(formPanel, BorderLayout.CENTER);
      add(buttonPanel, BorderLayout.SOUTH);
    }

    private JComponent createField(String type) {
      if (type.startsWith("combo:")) {
        String[] options = type.substring(6).split(",");
        return new JComboBox<>(options);
      }
      if ("bool".equals(type)) {
        return new JCheckBox();
      }
      return new JTextField(20);
    }

    public void populateFromCache() {
      if (cachedSettings == null || !cachedSettings.has(sectionKey)) {
        return;
      }
      JsonObject section = cachedSettings.getAsJsonObject(sectionKey);
      for (Map.Entry<String, JComponent> entry : fields.entrySet()) {
        String key = entry.getKey();
        JComponent field = entry.getValue();
        if (!section.has(key) || section.get(key).isJsonNull()) {
          continue;
        }
        JsonElement value = section.get(key);
        if (field instanceof JTextField) {
          ((JTextField) field).setText(jsonValueAsText(value));
        } else if (field instanceof JCheckBox) {
          ((JCheckBox) field).setSelected(value.getAsBoolean());
        } else if (field instanceof JComboBox) {
          @SuppressWarnings("unchecked")
          JComboBox<String> combo = (JComboBox<String>) field;
          combo.setSelectedItem(value.getAsString());
        }
      }
    }

    private void saveSettings() {
      Map<String, Object> data = new HashMap<>();
      for (Map.Entry<String, JComponent> entry : fields.entrySet()) {
        String key = entry.getKey();
        JComponent field = entry.getValue();

        if (field instanceof JTextField) {
          String text = ((JTextField) field).getText().trim();
          if (!text.isEmpty()) {
            if (isIntegerField(key)) {
              data.put(key, Integer.parseInt(text));
            } else if (isDoubleField(key)) {
              data.put(key, Double.parseDouble(text));
            } else {
              data.put(key, text);
            }
          }
        } else if (field instanceof JCheckBox) {
          data.put(key, ((JCheckBox) field).isSelected());
        } else if (field instanceof JComboBox) {
          @SuppressWarnings("unchecked")
          JComboBox<String> combo = (JComboBox<String>) field;
          Object selected = combo.getSelectedItem();
          if (selected != null) {
            data.put(key, selected.toString());
          }
        }
      }

      new Thread(
              () -> {
                try {
                  ApiClient.ApiResponse response = updater.update(data);
                  SwingUtilities.invokeLater(
                      () -> {
                        if (response.isSuccess()) {
                          cachedSettings = response.getDataAsObject();
                          SwingUtil.showInfo(SettingsPanel.this, "Settings saved successfully");
                        } else {
                          SwingUtil.showError(SettingsPanel.this, response.getErrorMessage());
                        }
                      });
                } catch (Exception ex) {
                  SwingUtilities.invokeLater(
                      () -> SwingUtil.showError(SettingsPanel.this, ex.getMessage()));
                }
              })
          .start();
    }

    private boolean isIntegerField(String key) {
      return key.endsWith("Threshold")
          || key.endsWith("Minutes")
          || key.endsWith("Attempts")
          || key.endsWith("Length")
          || key.endsWith("Days")
          || "port".equals(key);
    }

    private boolean isDoubleField(String key) {
      return key.endsWith("Multiplier");
    }
  }

  private static String jsonValueAsText(JsonElement value) {
    if (value.isJsonPrimitive()) {
      return value.getAsJsonPrimitive().getAsString();
    }
    return value.getAsString();
  }

  @FunctionalInterface
  private interface SettingsUpdater {
    ApiClient.ApiResponse update(Map<String, Object> body) throws java.io.IOException;
  }
}
