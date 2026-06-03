package com.group4.inventoryclient.ui.suppliers;

import com.google.gson.JsonObject;
import com.group4.inventoryclient.ui.components.FormDialog;
import java.awt.Frame;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class SupplierFormDialog extends FormDialog {

  private final JTextField nameField;
  private final JTextField contactPersonField;
  private final JTextField phoneField;
  private final JTextField emailField;
  private final JTextArea addressArea;
  private final JComboBox<String> statusCombo;

  public SupplierFormDialog(Frame owner, JsonObject supplier) {
    super(owner, supplier == null ? "Add Supplier" : "Edit Supplier");

    nameField = new JTextField(20);
    contactPersonField = new JTextField(20);
    phoneField = new JTextField(20);
    emailField = new JTextField(20);
    addressArea = new JTextArea(3, 20);
    addressArea.setLineWrap(true);
    addressArea.setWrapStyleWord(true);
    statusCombo = new JComboBox<>(new String[] {"Active", "Inactive"});

    addField("Name", nameField);
    addField("Contact Person", contactPersonField);
    addField("Phone", phoneField);
    addField("Email", emailField);
    addField("Address", new JScrollPane(addressArea));
    addField("Status", statusCombo);

    if (supplier != null) {
      if (supplier.has("name")) nameField.setText(supplier.get("name").getAsString());
      if (supplier.has("contactPerson"))
        contactPersonField.setText(supplier.get("contactPerson").getAsString());
      if (supplier.has("phone")) phoneField.setText(supplier.get("phone").getAsString());
      if (supplier.has("email")) emailField.setText(supplier.get("email").getAsString());
      if (supplier.has("address")) addressArea.setText(supplier.get("address").getAsString());
      if (supplier.has("status")) {
        String status = supplier.get("status").getAsString();
        statusCombo.setSelectedItem(status);
      }
    }
  }

  @Override
  protected boolean validateForm() {
    if (nameField.getText().trim().isEmpty()) {
      javax.swing.JOptionPane.showMessageDialog(
          this, "Name is required", "Validation Error", javax.swing.JOptionPane.ERROR_MESSAGE);
      return false;
    }
    if (contactPersonField.getText().trim().isEmpty()) {
      javax.swing.JOptionPane.showMessageDialog(
          this,
          "Contact Person is required",
          "Validation Error",
          javax.swing.JOptionPane.ERROR_MESSAGE);
      return false;
    }
    String email = emailField.getText().trim();
    if (!email.isEmpty() && !email.contains("@")) {
      javax.swing.JOptionPane.showMessageDialog(
          this, "Email must be valid", "Validation Error", javax.swing.JOptionPane.ERROR_MESSAGE);
      return false;
    }
    return true;
  }

  public Map<String, Object> getFormData() {
    Map<String, Object> data = new HashMap<>();
    data.put("name", nameField.getText().trim());
    data.put("contactPerson", contactPersonField.getText().trim());
    data.put("phone", phoneField.getText().trim());
    data.put("email", emailField.getText().trim());
    data.put("address", addressArea.getText().trim());
    data.put("status", statusCombo.getSelectedItem().toString());
    return data;
  }
}
