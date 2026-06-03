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

  public SettingsPanel(ApiClient apiClient) {
    super(new BorderLayout());
    this.settingsApiClient = new SettingsApiClient(apiClient);

    tabbedPane = new JTabbedPane();

    tabs.put(
        "company",
        new SettingsTab(
            "company",
            new String[][] {
              {"name", "Company Name", "text"},
              {"address", "Address", "text"},
              {"phone", "Phone", "text"},
              {"email", "Email", "text"},
              {"tax_id", "Tax ID", "text"}
            }));

    tabs.put(
        "inventory",
        new SettingsTab(
            "inventory",
            new String[][] {
              {"low_stock_threshold", "Low Stock Threshold", "text"},
              {"auto_reorder", "Auto Reorder", "combo:Enabled,Disabled"},
              {"reorder_level", "Reorder Level", "text"},
              {"barcode_format", "Barcode Format", "combo:EAN13,UPC,CODE128"}
            }));

    tabs.put(
        "security",
        new SettingsTab(
            "security",
            new String[][] {
              {"session_timeout", "Session Timeout (minutes)", "text"},
              {"password_expiry_days", "Password Expiry (days)", "text"},
              {"max_login_attempts", "Max Login Attempts", "text"},
              {"require_password_change", "Require Password Change", "combo:Yes,No"}
            }));

    tabs.put(
        "notifications",
        new SettingsTab(
            "notifications",
            new String[][] {
              {"email_enabled", "Email Notifications", "combo:Enabled,Disabled"},
              {"smtp_host", "SMTP Host", "text"},
              {"smtp_port", "SMTP Port", "text"},
              {"smtp_username", "SMTP Username", "text"},
              {"smtp_password", "SMTP Password", "password"}
            }));

    tabbedPane.addTab("Company", tabs.get("company"));
    tabbedPane.addTab("Inventory", tabs.get("inventory"));
    tabbedPane.addTab("Security", tabs.get("security"));
    tabbedPane.addTab("Notifications", tabs.get("notifications"));
    tabbedPane.addTab("Database", createDatabaseTab());

    tabbedPane.addChangeListener(
        e -> {
          int index = tabbedPane.getSelectedIndex();
          String title = tabbedPane.getTitleAt(index).toLowerCase();
          if (tabs.containsKey(title)) {
            tabs.get(title).loadSettings();
          }
        });

    add(tabbedPane, BorderLayout.CENTER);

    tabs.get("company").loadSettings();
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

  private void testConnection() {
    new Thread(
            () -> {
              try {
                ApiClient.ApiResponse response = settingsApiClient.testConnection();
                SwingUtilities.invokeLater(
                    () -> {
                      if (response.isSuccess()) {
                        SwingUtil.showInfo(this, "Database connection successful");
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
                        SwingUtil.showInfo(this, "Database backup initiated successfully");
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

    private final String section;
    private final Map<String, JComponent> fields = new HashMap<>();
    private final JButton saveBtn = new JButton("Save");

    public SettingsTab(String section, String[][] fieldDefinitions) {
      super(new BorderLayout());
      this.section = section;

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
      } else if (type.equals("password")) {
        return new javax.swing.JPasswordField(20);
      } else {
        return new JTextField(20);
      }
    }

    public void loadSettings() {
      new Thread(
              () -> {
                try {
                  ApiClient.ApiResponse response = settingsApiClient.get(section);
                  SwingUtilities.invokeLater(
                      () -> {
                        if (response.isSuccess()) {
                          JsonObject data = response.getDataAsObject();
                          for (Map.Entry<String, JComponent> entry : fields.entrySet()) {
                            String key = entry.getKey();
                            JComponent field = entry.getValue();

                            if (data.has(key)) {
                              JsonElement value = data.get(key);
                              if (field instanceof JTextField) {
                                ((JTextField) field).setText(value.getAsString());
                              } else if (field instanceof JComboBox) {
                                @SuppressWarnings("unchecked")
                                JComboBox<String> combo = (JComboBox<String>) field;
                                combo.setSelectedItem(value.getAsString());
                              }
                            }
                          }
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

    private void saveSettings() {
      Map<String, Object> data = new HashMap<>();
      for (Map.Entry<String, JComponent> entry : fields.entrySet()) {
        String key = entry.getKey();
        JComponent field = entry.getValue();

        if (field instanceof JTextField) {
          data.put(key, ((JTextField) field).getText().trim());
        } else if (field instanceof JComboBox) {
          @SuppressWarnings("unchecked")
          JComboBox<String> combo = (JComboBox<String>) field;
          data.put(key, combo.getSelectedItem().toString());
        }
      }

      new Thread(
              () -> {
                try {
                  ApiClient.ApiResponse response = settingsApiClient.update(section, data);
                  SwingUtilities.invokeLater(
                      () -> {
                        if (response.isSuccess()) {
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
  }
}
