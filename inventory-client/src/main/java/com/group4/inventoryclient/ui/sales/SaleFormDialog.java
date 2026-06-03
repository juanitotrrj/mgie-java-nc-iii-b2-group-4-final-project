package com.group4.inventoryclient.ui.sales;

import com.group4.inventoryclient.ui.components.FormDialog;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class SaleFormDialog extends FormDialog {

  private final JTextField customerNameField = new JTextField(20);
  private final JComboBox<String> paymentMethodCombo =
      new JComboBox<>(new String[] {"Cash", "Credit Card", "Debit Card"});
  private final DefaultTableModel itemsModel =
      new DefaultTableModel(new String[] {"Product ID", "Qty", "Unit Price"}, 0);
  private final JTable itemsTable = new JTable(itemsModel);
  private final JTextField amountReceivedField = new JTextField(10);
  private final JLabel changeLabel = new JLabel("Change: 0.00");

  public SaleFormDialog(Frame owner) {
    super(owner, "New Sale");

    addField("Customer Name", customerNameField);
    addField("Payment Method", paymentMethodCombo);

    JPanel itemsPanel = new JPanel(new BorderLayout(5, 5));
    itemsPanel.add(new JScrollPane(itemsTable), BorderLayout.CENTER);

    JPanel itemsBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
    JButton addItemBtn = new JButton("Add Item");
    JButton removeItemBtn = new JButton("Remove Item");
    itemsBtnPanel.add(addItemBtn);
    itemsBtnPanel.add(removeItemBtn);
    itemsPanel.add(itemsBtnPanel, BorderLayout.SOUTH);
    itemsPanel.setPreferredSize(new Dimension(400, 150));

    addField("Items", itemsPanel);
    addField("Amount Received", amountReceivedField);
    addField("Change", changeLabel);

    addItemBtn.addActionListener(e -> itemsModel.addRow(new Object[] {"", "", ""}));
    removeItemBtn.addActionListener(
        e -> {
          int row = itemsTable.getSelectedRow();
          if (row != -1) itemsModel.removeRow(row);
        });

    itemsModel.addTableModelListener(e -> updateChange());
    amountReceivedField
        .getDocument()
        .addDocumentListener(
            new javax.swing.event.DocumentListener() {
              @Override
              public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateChange();
              }

              @Override
              public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateChange();
              }

              @Override
              public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateChange();
              }
            });

    setSize(500, 500);
  }

  private void updateChange() {
    try {
      double total = 0.0;
      for (int i = 0; i < itemsModel.getRowCount(); i++) {
        String qtyStr = itemsModel.getValueAt(i, 1).toString().trim();
        String priceStr = itemsModel.getValueAt(i, 2).toString().trim();
        if (!qtyStr.isEmpty() && !priceStr.isEmpty()) {
          int qty = Integer.parseInt(qtyStr);
          double price = Double.parseDouble(priceStr);
          total += qty * price;
        }
      }
      double received = 0.0;
      String receivedStr = amountReceivedField.getText().trim();
      if (!receivedStr.isEmpty()) {
        received = Double.parseDouble(receivedStr);
      }
      double change = received - total;
      changeLabel.setText(String.format("Change: %.2f", change));
    } catch (Exception ex) {
      changeLabel.setText("Change: 0.00");
    }
  }

  @Override
  protected boolean validateForm() {
    if (itemsModel.getRowCount() == 0) {
      return false;
    }
    for (int i = 0; i < itemsModel.getRowCount(); i++) {
      String productId = itemsModel.getValueAt(i, 0).toString().trim();
      String qty = itemsModel.getValueAt(i, 1).toString().trim();
      String price = itemsModel.getValueAt(i, 2).toString().trim();
      if (productId.isEmpty() || qty.isEmpty() || price.isEmpty()) {
        return false;
      }
    }
    return true;
  }

  public Map<String, Object> getFormData() {
    Map<String, Object> data = new HashMap<>();
    String customerName = customerNameField.getText().trim();
    if (!customerName.isEmpty()) {
      data.put("customerName", customerName);
    }
    data.put("paymentMethod", paymentMethodCombo.getSelectedItem().toString());

    List<Map<String, Object>> items = new ArrayList<>();
    for (int i = 0; i < itemsModel.getRowCount(); i++) {
      Map<String, Object> item = new HashMap<>();
      item.put("productId", Long.parseLong(itemsModel.getValueAt(i, 0).toString().trim()));
      item.put("quantity", Integer.parseInt(itemsModel.getValueAt(i, 1).toString().trim()));
      item.put("unitPrice", Double.parseDouble(itemsModel.getValueAt(i, 2).toString().trim()));
      items.add(item);
    }
    data.put("items", items);

    String receivedStr = amountReceivedField.getText().trim();
    if (!receivedStr.isEmpty()) {
      data.put("amountReceived", Double.parseDouble(receivedStr));
    }

    return data;
  }
}
