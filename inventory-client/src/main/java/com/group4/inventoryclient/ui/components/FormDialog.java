package com.group4.inventoryclient.ui.components;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public abstract class FormDialog extends JDialog {

  protected final JPanel formPanel = new JPanel(new GridBagLayout());
  protected final JButton okButton = new JButton("Save");
  protected final JButton cancelButton = new JButton("Cancel");
  private boolean confirmed = false;
  private int row = 0;

  protected FormDialog(Frame owner, String title) {
    super(owner, title, true);
    setLayout(new BorderLayout(10, 10));
    formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
    add(formPanel, BorderLayout.CENTER);

    JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
    btnPanel.add(okButton);
    btnPanel.add(cancelButton);
    add(btnPanel, BorderLayout.SOUTH);

    okButton.addActionListener(
        e -> {
          if (validateForm()) {
            confirmed = true;
            dispose();
          }
        });
    cancelButton.addActionListener(e -> dispose());
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
  }

  protected void addField(String label, JComponent field) {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.anchor = GridBagConstraints.WEST;

    gbc.gridx = 0;
    gbc.gridy = row;
    formPanel.add(new JLabel(label + ":"), gbc);

    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    formPanel.add(field, gbc);
    row++;
  }

  protected abstract boolean validateForm();

  public boolean isConfirmed() {
    return confirmed;
  }

  public void showAndWait() {
    pack();
    setLocationRelativeTo(getOwner());
    setVisible(true);
  }
}
