package com.group4.inventoryclient.ui.steps;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.group4.inventoryclient.api.SetupApiClient;
import com.group4.inventoryclient.ui.WizardStep;
import com.group4.inventoryclient.util.SwingUtil;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

public class ReviewFinishStep extends WizardStep {

  private final SetupApiClient api;
  private final JPanel panel;
  private final JTextArea summaryArea = new JTextArea(10, 40);
  private boolean finished = false;

  public ReviewFinishStep(SetupApiClient api) {
    this.api = api;
    this.panel = buildPanel();
  }

  private JPanel buildPanel() {
    JPanel p = new JPanel(new BorderLayout(10, 10));
    p.setBorder(new TitledBorder("Review & Finish"));

    summaryArea.setEditable(false);
    summaryArea.setText("Loading progress...");

    p.add(
        new JLabel(
            "<html><b>Review the completed steps and click Finish to initialize the system.</b></html>"),
        BorderLayout.NORTH);
    p.add(new JScrollPane(summaryArea), BorderLayout.CENTER);
    p.add(
        new JLabel(
            "<html><i>After finishing, the system will be INITIALIZED and setup endpoints will be locked.</i></html>"),
        BorderLayout.SOUTH);

    return p;
  }

  @Override
  public JPanel getPanel() {
    return panel;
  }

  @Override
  public String getTitle() {
    return "Review & Finish";
  }

  @Override
  public boolean validate() {
    return true;
  }

  @Override
  public void submit() throws Exception {
    if (finished) return;
    JsonObject result = api.finish();
    finished = true;
    String msg = result.has("message") ? result.get("message").getAsString() : "Setup complete!";
    SwingUtil.showInfo(panel, msg + "\nYou may now close this wizard and use the system.");
  }

  @Override
  public void onEnter() {
    new Thread(
            () -> {
              try {
                JsonObject progress = api.getProgress();
                StringBuilder sb = new StringBuilder();
                sb.append("Current State: ")
                    .append(
                        progress.has("currentState")
                            ? progress.get("currentState").getAsString()
                            : "unknown")
                    .append("\n\n");
                sb.append("Completed Steps:\n");
                if (progress.has("completedSteps")) {
                  JsonArray steps = progress.getAsJsonArray("completedSteps");
                  for (int i = 0; i < steps.size(); i++) {
                    sb.append("  \u2713 ").append(steps.get(i).getAsString()).append("\n");
                  }
                }
                if (sb.indexOf("\u2713") < 0) {
                  sb.append("  (none detected yet - steps may have been submitted)\n");
                }
                sb.append("\nClick 'Finish' to finalize the setup.");
                javax.swing.SwingUtilities.invokeLater(
                    () -> {
                      summaryArea.setText(sb.toString());
                      summaryArea.setCaretPosition(0);
                    });
              } catch (Exception e) {
                javax.swing.SwingUtilities.invokeLater(
                    () -> summaryArea.setText("Could not load progress: " + e.getMessage()));
              }
            })
        .start();
  }
}
