package com.group4.inventoryclient.ui.products;

import com.google.gson.JsonObject;
import com.group4.inventoryclient.ui.components.FormDialog;
import java.awt.Frame;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ProductFormDialog extends FormDialog {

  private final JTextField nameField;
  private final JTextField skuField;
  private final JComboBox<CategoryOption> categoryCombo;
  private final JTextArea descriptionArea;
  private final JTextField quantityField;
  private final JTextField unitPriceField;
  private final JTextField reorderLevelField;
  private final JComboBox<String> statusCombo;

  public ProductFormDialog(Frame owner, JsonObject product) {
    this(owner, product, Collections.emptyList());
  }

  public ProductFormDialog(Frame owner, JsonObject product, List<CategoryOption> categories) {
    super(owner, product == null ? "Add Product" : "Edit Product");

    nameField = new JTextField(20);
    skuField = new JTextField(20);
    categoryCombo = new JComboBox<>();
    descriptionArea = new JTextArea(3, 20);
    descriptionArea.setLineWrap(true);
    descriptionArea.setWrapStyleWord(true);
    quantityField = new JTextField(20);
    unitPriceField = new JTextField(20);
    reorderLevelField = new JTextField(20);
    statusCombo =
        new JComboBox<>(new String[] {"In Stock", "Low Stock", "Out of Stock", "Inactive"});

    for (CategoryOption option : categories) {
      categoryCombo.addItem(option);
    }
    if (categoryCombo.getItemCount() > 0) {
      categoryCombo.setSelectedIndex(0);
    }

    addField("Name", nameField);
    addField("SKU", skuField);
    addField("Category", categoryCombo);
    addField("Description", new JScrollPane(descriptionArea));
    addField("Quantity", quantityField);
    addField("Unit Price", unitPriceField);
    addField("Reorder Level", reorderLevelField);
    addField("Status", statusCombo);

    if (product != null) {
      if (product.has("productName")) {
        nameField.setText(product.get("productName").getAsString());
      } else if (product.has("name")) {
        nameField.setText(product.get("name").getAsString());
      }
      if (product.has("productCode")) {
        skuField.setText(product.get("productCode").getAsString());
      } else if (product.has("sku")) {
        skuField.setText(product.get("sku").getAsString());
      }
      selectCategory(product);
      if (product.has("description")) {
        descriptionArea.setText(product.get("description").getAsString());
      }
      if (product.has("quantity")) {
        quantityField.setText(product.get("quantity").getAsString());
      }
      if (product.has("unitPrice")) {
        unitPriceField.setText(product.get("unitPrice").getAsString());
      }
      if (product.has("reorderLevel")) {
        reorderLevelField.setText(product.get("reorderLevel").getAsString());
      }
      if (product.has("status")) {
        statusCombo.setSelectedItem(product.get("status").getAsString());
      }
    }
  }

  private void selectCategory(JsonObject product) {
    if (!product.has("categoryId")) {
      return;
    }
    long categoryId = product.get("categoryId").getAsLong();
    for (int i = 0; i < categoryCombo.getItemCount(); i++) {
      CategoryOption option = categoryCombo.getItemAt(i);
      if (option.getCategoryId() == categoryId) {
        categoryCombo.setSelectedIndex(i);
        return;
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
    if (categoryCombo.getSelectedItem() == null) {
      javax.swing.JOptionPane.showMessageDialog(
          this, "Category is required", "Validation Error", javax.swing.JOptionPane.ERROR_MESSAGE);
      return false;
    }
    try {
      int quantity = Integer.parseInt(quantityField.getText().trim());
      if (quantity < 0) {
        javax.swing.JOptionPane.showMessageDialog(
            this,
            "Quantity must not be negative",
            "Validation Error",
            javax.swing.JOptionPane.ERROR_MESSAGE);
        return false;
      }
    } catch (NumberFormatException e) {
      javax.swing.JOptionPane.showMessageDialog(
          this,
          "Quantity must be a valid whole number",
          "Validation Error",
          javax.swing.JOptionPane.ERROR_MESSAGE);
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
    data.put("productName", nameField.getText().trim());
    data.put("productCode", skuField.getText().trim());

    CategoryOption selected = (CategoryOption) categoryCombo.getSelectedItem();
    if (selected != null) {
      data.put("categoryId", selected.getCategoryId());
    }

    data.put("description", descriptionArea.getText().trim());
    data.put("quantity", Integer.parseInt(quantityField.getText().trim()));
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
