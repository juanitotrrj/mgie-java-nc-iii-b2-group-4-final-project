package com.group4.inventoryclient.ui;

import com.google.gson.JsonObject;
import com.group4.inventoryclient.api.ApiClient;
import com.group4.inventoryclient.api.SetupApiClient;
import com.group4.inventoryclient.config.ClientConfig;
import com.group4.inventoryclient.ui.steps.BusinessSettingsStep;
import com.group4.inventoryclient.ui.steps.CreateUsersStep;
import com.group4.inventoryclient.ui.steps.PermissionSeedStep;
import com.group4.inventoryclient.ui.steps.ReviewFinishStep;
import com.group4.inventoryclient.ui.steps.RoleSeedStep;
import com.group4.inventoryclient.ui.steps.RootAuthStep;
import com.group4.inventoryclient.ui.steps.StatusStep;
import com.group4.inventoryclient.util.SwingUtil;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class WizardFrame extends JFrame {

  private final ApiClient apiClient;
  private final SetupApiClient setupApi;
  private final List<WizardStep> steps = new ArrayList<>();
  private final CardLayout cardLayout = new CardLayout();
  private final JPanel cardPanel = new JPanel(cardLayout);
  private final JLabel stepLabel = new JLabel();
  private final JButton backButton = new JButton("< Back");
  private final JButton nextButton = new JButton("Next >");
  private int currentStep = 0;

  public WizardFrame(ClientConfig config) {
    super("G4IMS - Initial Setup Wizard");
    this.apiClient = new ApiClient(config.getServerUrl());
    this.setupApi = new SetupApiClient(apiClient);

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setMinimumSize(new Dimension(650, 500));
    setLocationRelativeTo(null);

    initSteps();
    initLayout();
    checkServerStatus();
  }

  private void initSteps() {
    steps.add(new StatusStep(setupApi));
    steps.add(new RootAuthStep(setupApi));
    steps.add(new BusinessSettingsStep(setupApi));
    steps.add(new RoleSeedStep(setupApi));
    steps.add(new PermissionSeedStep(setupApi));
    steps.add(new CreateUsersStep(setupApi));
    steps.add(new ReviewFinishStep(setupApi));

    for (int i = 0; i < steps.size(); i++) {
      cardPanel.add(steps.get(i).getPanel(), String.valueOf(i));
    }
  }

  private void initLayout() {
    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    stepLabel.setFont(stepLabel.getFont().deriveFont(java.awt.Font.BOLD, 14f));
    topPanel.add(stepLabel);
    topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

    JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    backButton.addActionListener(e -> navigateBack());
    nextButton.addActionListener(e -> navigateNext());
    bottomPanel.add(backButton);
    bottomPanel.add(nextButton);
    bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

    cardPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

    setLayout(new BorderLayout());
    add(topPanel, BorderLayout.NORTH);
    add(cardPanel, BorderLayout.CENTER);
    add(bottomPanel, BorderLayout.SOUTH);

    updateNavigation();
  }

  private void checkServerStatus() {
    new Thread(
            () -> {
              try {
                JsonObject status = setupApi.getStatus();
                String state = status.has("state") ? status.get("state").getAsString() : "";
                if ("INITIALIZED".equals(state)) {
                  SwingUtilities.invokeLater(
                      () -> {
                        SwingUtil.showInfo(
                            this,
                            "System is already initialized.\n"
                                + "The setup wizard is no longer needed.");
                        dispose();
                        GuestFrame guest = new GuestFrame(apiClient);
                        guest.setVisible(true);
                      });
                }
              } catch (Exception e) {
                SwingUtilities.invokeLater(
                    () ->
                        SwingUtil.showError(this, "Cannot connect to server:\n" + e.getMessage()));
              }
            })
        .start();
  }

  private void navigateBack() {
    if (currentStep > 0) {
      currentStep--;
      showStep();
    }
  }

  private void navigateNext() {
    WizardStep step = steps.get(currentStep);
    if (!step.validate()) return;

    nextButton.setEnabled(false);
    new Thread(
            () -> {
              try {
                step.submit();
                SwingUtilities.invokeLater(
                    () -> {
                      if (currentStep < steps.size() - 1) {
                        currentStep++;
                        showStep();
                      }
                      nextButton.setEnabled(true);
                    });
              } catch (Exception e) {
                SwingUtilities.invokeLater(
                    () -> {
                      SwingUtil.showError(WizardFrame.this, e.getMessage());
                      nextButton.setEnabled(true);
                    });
              }
            })
        .start();
  }

  private void showStep() {
    cardLayout.show(cardPanel, String.valueOf(currentStep));
    steps.get(currentStep).onEnter();
    updateNavigation();
  }

  private void updateNavigation() {
    int total = steps.size();
    stepLabel.setText(
        "Step " + (currentStep + 1) + " of " + total + ": " + steps.get(currentStep).getTitle());
    backButton.setEnabled(currentStep > 0);
    if (currentStep == total - 1) {
      nextButton.setText("Finish");
    } else {
      nextButton.setText("Next >");
    }
  }
}
