package com.group4.inventoryclient.ui.purchases;

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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class PurchaseFormDialog extends FormDialog {

  private final JTextField supplierIdField = new JTextField(20);
  private final JTextField expectedDateField = new JTextField(20);
  private final JTextArea notesArea = new JTextArea(3, 20);
  private final DefaultTableModel itemsModel =
      new DefaultTableModel(new String[] {"Product ID", "Qty", "Unit Cost"}, 0);
  private final JTable itemsTable = new JTable(itemsModel);

  public PurchaseFormDialog(Frame owner) {
    super(owner, "New Purchase Order");

    addField("Supplier ID", supplierIdField);
    addField("Expected Delivery (YYYY-MM-DD)", expectedDateField);
    addField("Notes", new JScrollPane(notesArea));

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

    addItemBtn.addActionListener(e -> itemsModel.addRow(new Object[] {"", "", ""}));
    removeItemBtn.addActionListener(
        e -> {
          int row = itemsTable.getSelectedRow();
          if (row != -1) itemsModel.removeRow(row);
        });

    setSize(500, 450);
  }

  @Override
  protected boolean validateForm() {
    if (supplierIdField.getText().trim().isEmpty()) {
      return false;
    }
    if (expectedDateField.getText().trim().isEmpty()) {
      return false;
    }
    if (itemsModel.getRowCount() == 0) {
      return false;
    }
    for (int i = 0; i < itemsModel.getRowCount(); i++) {
      String productId = itemsModel.getValueAt(i, 0).toString().trim();
      String qty = itemsModel.getValueAt(i, 1).toString().trim();
      String cost = itemsModel.getValueAt(i, 2).toString().trim();
      if (productId.isEmpty() || qty.isEmpty() || cost.isEmpty()) {
        return false;
      }
    }
    return true;
  }

  public Map<String, Object> getFormData() {
    Map<String, Object> data = new HashMap<>();
    data.put("supplierId", Long.parseLong(supplierIdField.getText().trim()));
    data.put("expectedDeliveryDate", expectedDateField.getText().trim());
    data.put("notes", notesArea.getText().trim());

    List<Map<String, Object>> items = new ArrayList<>();
    for (int i = 0; i < itemsModel.getRowCount(); i++) {
      Map<String, Object> item = new HashMap<>();
      item.put("productId", Long.parseLong(itemsModel.getValueAt(i, 0).toString().trim()));
      item.put("quantity", Integer.parseInt(itemsModel.getValueAt(i, 1).toString().trim()));
      item.put("unitCost", Double.parseDouble(itemsModel.getValueAt(i, 2).toString().trim()));
      items.add(item);
    }
    data.put("items", items);

    return data;
  }
}
