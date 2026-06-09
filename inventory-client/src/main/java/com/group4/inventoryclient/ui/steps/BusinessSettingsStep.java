package com.group4.inventoryclient.ui.steps;

import com.group4.inventoryclient.api.SetupApiClient;
import com.group4.inventoryclient.ui.WizardStep;
import com.group4.inventoryclient.util.SwingUtil;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class BusinessSettingsStep extends WizardStep {

  private final SetupApiClient api;
  private final JPanel panel;
  private final JTextField companyField = new JTextField(25);
  private final JTextField currencyCodeField = new JTextField(10);
  private final JTextField currencySymbolField = new JTextField(5);
  private final JTextField currencyNameField = new JTextField(20);
  private final JTextField taxRateField = new JTextField(8);
  private final JTextField taxTypeField = new JTextField(20);
  private final JTextField decimalField = new JTextField(5);
  private final JTextField timezoneField = new JTextField(15);

  public BusinessSettingsStep(SetupApiClient api) {
    this.api = api;
    this.panel = buildPanel();
  }

  private JPanel buildPanel() {
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(new TitledBorder("Business Settings"));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 10, 5, 10);
    gbc.anchor = GridBagConstraints.WEST;

    int row = 0;
    addRow(p, gbc, row++, "Company Name:", companyField);
    addRow(p, gbc, row++, "Currency Code:", currencyCodeField);
    addRow(p, gbc, row++, "Currency Symbol:", currencySymbolField);
    addRow(p, gbc, row++, "Currency Name:", currencyNameField);
    addRow(p, gbc, row++, "Tax Rate (%):", taxRateField);
    addRow(p, gbc, row++, "Tax Type:", taxTypeField);
    addRow(p, gbc, row++, "Price Decimal Places:", decimalField);
    addRow(p, gbc, row++, "Timezone:", timezoneField);

    companyField.setText("ABC Trading");
    currencyCodeField.setText("PHP");
    currencySymbolField.setText("\u20B1");
    currencyNameField.setText("Philippine Peso");
    taxRateField.setText("12.00");
    taxTypeField.setText("Inclusive of Tax");
    decimalField.setText("2");
    timezoneField.setText("Asia/Manila");

    return p;
  }

  private void addRow(JPanel p, GridBagConstraints gbc, int row, String label, JTextField field) {
    gbc.gridx = 0;
    gbc.gridy = row;
    p.add(new JLabel(label), gbc);
    gbc.gridx = 1;
    p.add(field, gbc);
  }

  @Override
  public JPanel getPanel() {
    return panel;
  }

  @Override
  public String getTitle() {
    return "Business Settings";
  }

  @Override
  public boolean validate() {
    if (companyField.getText().trim().isEmpty()) {
      SwingUtil.showError(panel, "Company name is required.");
      return false;
    }
    if (currencyCodeField.getText().trim().isEmpty()) {
      SwingUtil.showError(panel, "Currency code is required.");
      return false;
    }
    return true;
  }

  @Override
  public void submit() throws Exception {
    Map<String, Object> settings = new HashMap<>();
    settings.put("companyName", companyField.getText().trim());
    settings.put("currencyCode", currencyCodeField.getText().trim());
    settings.put("currencySymbol", currencySymbolField.getText().trim());
    settings.put("currencyName", currencyNameField.getText().trim());
    settings.put("taxRate", Double.parseDouble(taxRateField.getText().trim()));
    settings.put("taxType", taxTypeField.getText().trim());
    settings.put("priceDecimalPlaces", Integer.parseInt(decimalField.getText().trim()));
    settings.put("timezone", timezoneField.getText().trim());
    api.saveBusinessSettings(settings);
  }
}
