package com.group4.inventoryclient.ui.steps;

import com.google.gson.JsonObject;
import com.group4.inventoryclient.api.SetupApiClient;
import com.group4.inventoryclient.ui.WizardStep;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

public class StatusStep extends WizardStep {

  private final SetupApiClient api;
  private final JPanel panel;
  private final JLabel stateLabel = new JLabel("...");
  private final JLabel versionLabel = new JLabel("...");
  private final JLabel dbLabel = new JLabel("...");
  private final JLabel setupLabel = new JLabel("...");

  public StatusStep(SetupApiClient api) {
    this.api = api;
    this.panel = buildPanel();
  }

  private JPanel buildPanel() {
    JPanel p = new JPanel(new BorderLayout());
    JPanel info = new JPanel(new GridBagLayout());
    info.setBorder(new TitledBorder("Server Status"));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 10, 5, 10);
    gbc.anchor = GridBagConstraints.WEST;

    gbc.gridx = 0;
    gbc.gridy = 0;
    info.add(new JLabel("System State:"), gbc);
    gbc.gridx = 1;
    info.add(stateLabel, gbc);

    gbc.gridx = 0;
    gbc.gridy = 1;
    info.add(new JLabel("Server Version:"), gbc);
    gbc.gridx = 1;
    info.add(versionLabel, gbc);

    gbc.gridx = 0;
    gbc.gridy = 2;
    info.add(new JLabel("Database Ready:"), gbc);
    gbc.gridx = 1;
    info.add(dbLabel, gbc);

    gbc.gridx = 0;
    gbc.gridy = 3;
    info.add(new JLabel("App Setup Required:"), gbc);
    gbc.gridx = 1;
    info.add(setupLabel, gbc);

    p.add(info, BorderLayout.NORTH);
    JLabel instructions =
        new JLabel("<html><br>Click <b>Next</b> to begin the application setup wizard.</html>");
    p.add(instructions, BorderLayout.CENTER);
    return p;
  }

  @Override
  public JPanel getPanel() {
    return panel;
  }

  @Override
  public String getTitle() {
    return "Server Status";
  }

  @Override
  public boolean validate() {
    return true;
  }

  @Override
  public void submit() throws Exception {
    // no-op for status step
  }

  @Override
  public void onEnter() {
    new Thread(
            () -> {
              try {
                JsonObject status = api.getStatus();
                javax.swing.SwingUtilities.invokeLater(
                    () -> {
                      stateLabel.setText(getField(status, "state"));
                      versionLabel.setText(getField(status, "serverVersion"));
                      dbLabel.setText(
                          status.has("databaseReady")
                              ? String.valueOf(status.get("databaseReady").getAsBoolean())
                              : "unknown");
                      setupLabel.setText(
                          status.has("appSetupRequired")
                              ? String.valueOf(status.get("appSetupRequired").getAsBoolean())
                              : "unknown");
                    });
              } catch (Exception e) {
                javax.swing.SwingUtilities.invokeLater(
                    () -> stateLabel.setText("Error: " + e.getMessage()));
              }
            })
        .start();
  }

  private String getField(JsonObject obj, String field) {
    return obj.has(field) ? obj.get(field).getAsString() : "N/A";
  }
}
