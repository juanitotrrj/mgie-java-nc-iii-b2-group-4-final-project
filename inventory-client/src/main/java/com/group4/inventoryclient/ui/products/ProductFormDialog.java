package com.group4.inventoryclient.ui.products;

import com.google.gson.JsonObject;
import com.group4.inventoryclient.ui.components.FormDialog;
import java.awt.Frame;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ProductFormDialog extends FormDialog {

  private final JTextField nameField;
  private final JTextField skuField;
  private final JTextField categoryIdField;
  private final JTextArea descriptionArea;
  private final JTextField unitPriceField;
  private final JTextField reorderLevelField;
  private final JComboBox<String> statusCombo;

  public ProductFormDialog(Frame owner, JsonObject product) {
    super(owner, product == null ? "Add Product" : "Edit Product");

    nameField = new JTextField(20);
    skuField = new JTextField(20);
    categoryIdField = new JTextField(20);
    descriptionArea = new JTextArea(3, 20);
    descriptionArea.setLineWrap(true);
    descriptionArea.setWrapStyleWord(true);
    unitPriceField = new JTextField(20);
    reorderLevelField = new JTextField(20);
    statusCombo = new JComboBox<>(new String[] {"Active", "Inactive"});

    addField("Name", nameField);
    addField("SKU", skuField);
    addField("Category ID", categoryIdField);
    addField("Description", new JScrollPane(descriptionArea));
    addField("Unit Price", unitPriceField);
    addField("Reorder Level", reorderLevelField);
    addField("Status", statusCombo);

    if (product != null) {
      if (product.has("name")) nameField.setText(product.get("name").getAsString());
      if (product.has("sku")) skuField.setText(product.get("sku").getAsString());
      if (product.has("categoryId"))
        categoryIdField.setText(product.get("categoryId").getAsString());
      if (product.has("description"))
        descriptionArea.setText(product.get("description").getAsString());
      if (product.has("unitPrice")) unitPriceField.setText(product.get("unitPrice").getAsString());
      if (product.has("reorderLevel"))
        reorderLevelField.setText(product.get("reorderLevel").getAsString());
      if (product.has("status")) {
        String status = product.get("status").getAsString();
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
    if (skuField.getText().trim().isEmpty()) {
      javax.swing.JOptionPane.showMessageDialog(
          this, "SKU is required", "Validation Error", javax.swing.JOptionPane.ERROR_MESSAGE);
      return false;
    }
    try {
      Double.parseDouble(unitPriceField.getText().trim());
    } catch (NumberFormatException e) {
      javax.swing.JOptionPane.showMessageDialog(
          this,
          "Unit Price must be a valid number",
          "Validation Error",
          javax.swing.JOptionPane.ERROR_MESSAGE);
      return false;
    }
    return true;
  }

  public Map<String, Object> getFormData() {
    Map<String, Object> data = new HashMap<>();
    data.put("name", nameField.getText().trim());
    data.put("sku", skuField.getText().trim());

    String categoryIdText = categoryIdField.getText().trim();
    if (!categoryIdText.isEmpty()) {
      try {
        data.put("categoryId", Long.parseLong(categoryIdText));
      } catch (NumberFormatException e) {
        data.put("categoryId", categoryIdText);
      }
    }

    data.put("description", descriptionArea.getText().trim());
    data.put("unitPrice", Double.parseDouble(unitPriceField.getText().trim()));

    String reorderText = reorderLevelField.getText().trim();
    if (!reorderText.isEmpty()) {
      try {
        data.put("reorderLevel", Integer.parseInt(reorderText));
      } catch (NumberFormatException e) {
        data.put("reorderLevel", 0);
      }
    }

    data.put("status", statusCombo.getSelectedItem().toString());
    return data;
  }
}
