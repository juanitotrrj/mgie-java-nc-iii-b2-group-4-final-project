package com.group4.inventoryclient.ui.icr;

import com.group4.inventoryclient.ui.components.FormDialog;
import java.awt.Frame;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class IcrSubmitDialog extends FormDialog {

  private final JTextField productIdField = new JTextField(20);
  private final JComboBox<String> requestTypeCombo =
      new JComboBox<>(new String[] {"Adjustment", "Correction", "Damage", "Lost", "Found"});
  private final JTextField quantityField = new JTextField(20);
  private final JTextArea reasonArea = new JTextArea(3, 20);

  public IcrSubmitDialog(Frame owner) {
    super(owner, "Submit Inventory Change Request");

    addField("Product ID", productIdField);
    addField("Request Type", requestTypeCombo);
    addField("Quantity", quantityField);
    addField("Reason", new JScrollPane(reasonArea));

    setSize(450, 320);
  }

  @Override
  protected boolean validateForm() {
    if (productIdField.getText().trim().isEmpty()) {
      return false;
    }
    if (quantityField.getText().trim().isEmpty()) {
      return false;
    }
    return !reasonArea.getText().trim().isEmpty();
  }

  public Map<String, Object> getFormData() {
    Map<String, Object> data = new HashMap<>();
    data.put("productId", Long.parseLong(productIdField.getText().trim()));
    data.put("requestType", requestTypeCombo.getSelectedItem().toString());
    data.put("quantity", Integer.parseInt(quantityField.getText().trim()));
    data.put("reason", reasonArea.getText().trim());
    return data;
  }
}
