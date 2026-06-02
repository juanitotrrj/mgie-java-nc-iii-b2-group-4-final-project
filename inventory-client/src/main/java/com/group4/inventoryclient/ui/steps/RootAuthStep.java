package com.group4.inventoryclient.ui.steps;

import com.group4.inventoryclient.api.SetupApiClient;
import com.group4.inventoryclient.ui.WizardStep;
import com.group4.inventoryclient.util.SwingUtil;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class RootAuthStep extends WizardStep {

  private final SetupApiClient api;
  private final JPanel panel;
  private final JTextField usernameField = new JTextField(20);
  private final JPasswordField passwordField = new JPasswordField(20);

  public RootAuthStep(SetupApiClient api) {
    this.api = api;
    this.panel = buildPanel();
  }

  private JPanel buildPanel() {
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(new TitledBorder("Root Authentication"));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(8, 10, 8, 10);
    gbc.anchor = GridBagConstraints.WEST;

    gbc.gridx = 0;
    gbc.gridy = 0;
    p.add(new JLabel("Root Username:"), gbc);
    gbc.gridx = 1;
    usernameField.setText("root");
    p.add(usernameField, gbc);

    gbc.gridx = 0;
    gbc.gridy = 1;
    p.add(new JLabel("Root Password:"), gbc);
    gbc.gridx = 1;
    p.add(passwordField, gbc);

    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.gridwidth = 2;
    p.add(
        new JLabel("<html><i>Enter the root credentials set during server setup.</i></html>"), gbc);

    return p;
  }

  @Override
  public JPanel getPanel() {
    return panel;
  }

  @Override
  public String getTitle() {
    return "Root Authentication";
  }

  @Override
  public boolean validate() {
    if (usernameField.getText().trim().isEmpty()) {
      SwingUtil.showError(panel, "Username is required.");
      return false;
    }
    if (passwordField.getPassword().length == 0) {
      SwingUtil.showError(panel, "Password is required.");
      return false;
    }
    return true;
  }

  @Override
  public void submit() throws Exception {
    String username = usernameField.getText().trim();
    String password = new String(passwordField.getPassword());
    api.rootLogin(username, password);
  }
}
