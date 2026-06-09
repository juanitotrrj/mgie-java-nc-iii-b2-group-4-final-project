package com.group4.inventoryclient.ui.steps;

import com.group4.inventoryclient.api.SetupApiClient;
import com.group4.inventoryclient.ui.WizardStep;
import com.group4.inventoryclient.util.SwingUtil;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class CreateUsersStep extends WizardStep {

  private final SetupApiClient api;
  private final JPanel panel;
  private final JTextField usernameField = new JTextField(20);
  private final JPasswordField passwordField = new JPasswordField(20);
  private final JTextField emailField = new JTextField(25);
  private final JTextField fullNameField = new JTextField(25);
  private final JComboBox<String> roleCombo =
      new JComboBox<>(new String[] {"Admin", "Manager", "Inventory Clerk", "Cashier"});

  public CreateUsersStep(SetupApiClient api) {
    this.api = api;
    this.panel = buildPanel();
  }

  private JPanel buildPanel() {
    JPanel p = new JPanel(new BorderLayout(10, 10));
    p.setBorder(new TitledBorder("Create Initial User"));

    JPanel form = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 10, 5, 10);
    gbc.anchor = GridBagConstraints.WEST;

    int row = 0;
    addRow(form, gbc, row++, "Username:", usernameField);
    addRow(form, gbc, row++, "Password:", passwordField);
    addRow(form, gbc, row++, "Email:", emailField);
    addRow(form, gbc, row++, "Full Name:", fullNameField);
    gbc.gridx = 0;
    gbc.gridy = row;
    form.add(new JLabel("Role:"), gbc);
    gbc.gridx = 1;
    form.add(roleCombo, gbc);

    usernameField.setText("admin");
    emailField.setText("admin@inventory.local");
    fullNameField.setText("System Administrator");

    p.add(form, BorderLayout.CENTER);
    p.add(
        new JLabel("<html><i>Create at least one admin user for day-to-day operations.</i></html>"),
        BorderLayout.SOUTH);

    return p;
  }

  private void addRow(
      JPanel p, GridBagConstraints gbc, int row, String label, javax.swing.JComponent field) {
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
    return "Create Users";
  }

  @Override
  public boolean validate() {
    if (usernameField.getText().trim().isEmpty()) {
      SwingUtil.showError(panel, "Username is required.");
      return false;
    }
    if (passwordField.getPassword().length < 8) {
      SwingUtil.showError(panel, "Password must be at least 8 characters.");
      return false;
    }
    return true;
  }

  @Override
  public void submit() throws Exception {
    List<Map<String, String>> users = new ArrayList<>();
    Map<String, String> user = new HashMap<>();
    user.put("username", usernameField.getText().trim());
    user.put("password", new String(passwordField.getPassword()));
    user.put("email", emailField.getText().trim());
    user.put("fullName", fullNameField.getText().trim());
    user.put("role", (String) roleCombo.getSelectedItem());
    users.add(user);
    api.createUsers(users);
  }
}
