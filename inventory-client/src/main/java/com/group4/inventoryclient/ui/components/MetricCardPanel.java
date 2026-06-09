package com.group4.inventoryclient.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class MetricCardPanel extends JPanel {

  private final JLabel valueLabel;
  private final JLabel titleLabel;
  private final JLabel descLabel;

  public MetricCardPanel(String title, String value, String description, Color accentColor) {
    super(new BorderLayout(4, 4));
    setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accentColor, 2),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)));

    titleLabel = new JLabel(title);
    titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 11f));
    titleLabel.setForeground(Color.DARK_GRAY);

    valueLabel = new JLabel(value);
    valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 22f));
    valueLabel.setHorizontalAlignment(SwingConstants.LEFT);

    descLabel = new JLabel(description);
    descLabel.setFont(descLabel.getFont().deriveFont(Font.PLAIN, 10f));
    descLabel.setForeground(Color.GRAY);

    add(titleLabel, BorderLayout.NORTH);
    add(valueLabel, BorderLayout.CENTER);
    add(descLabel, BorderLayout.SOUTH);
  }

  public void updateValue(String value) {
    valueLabel.setText(value);
  }

  public void updateDescription(String desc) {
    descLabel.setText(desc);
  }
}
