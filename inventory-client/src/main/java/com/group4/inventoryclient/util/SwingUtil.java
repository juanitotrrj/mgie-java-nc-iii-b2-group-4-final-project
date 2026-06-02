package com.group4.inventoryclient.util;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

public final class SwingUtil {

  private SwingUtil() {}

  public static void showError(Component parent, String message) {
    SwingUtilities.invokeLater(
        () -> JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE));
  }

  public static void showInfo(Component parent, String message) {
    SwingUtilities.invokeLater(
        () ->
            JOptionPane.showMessageDialog(
                parent, message, "Information", JOptionPane.INFORMATION_MESSAGE));
  }

  public static JPanel createTitledPanel(String title) {
    JPanel panel = new JPanel();
    panel.setBorder(new TitledBorder(title));
    return panel;
  }

  public static JLabel createBoldLabel(String text) {
    JLabel label = new JLabel(text);
    label.setFont(label.getFont().deriveFont(java.awt.Font.BOLD));
    return label;
  }

  public static void runAsync(Runnable task, Component parent, Runnable onSuccess) {
    new Thread(
            () -> {
              try {
                task.run();
                if (onSuccess != null) SwingUtilities.invokeLater(onSuccess);
              } catch (Exception e) {
                showError(parent, e.getMessage());
              }
            })
        .start();
  }
}
