package com.group4.inventoryclient.ui.categories;

import com.google.gson.JsonObject;
import com.group4.inventoryclient.ui.components.FormDialog;
import java.awt.Frame;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class CategoryFormDialog extends FormDialog {

  private final JTextField nameField;
  private final JTextArea descriptionArea;
  private final JComboBox<String> statusCombo;

  public CategoryFormDialog(Frame owner, JsonObject category) {
    super(owner, category == null ? "Add Category" : "Edit Category");

    nameField = new JTextField(20);
    descriptionArea = new JTextArea(3, 20);
    descriptionArea.setLineWrap(true);
    descriptionArea.setWrapStyleWord(true);
    statusCombo = new JComboBox<>(new String[] {"Active", "Inactive"});

    addField("Name", nameField);
    addField("Description", new JScrollPane(descriptionArea));
    addField("Status", statusCombo);

    if (category != null) {
      if (category.has("name")) nameField.setText(category.get("name").getAsString());
      if (category.has("description"))
        descriptionArea.setText(category.get("description").getAsString());
      if (category.has("status")) {
        String status = category.get("status").getAsString();
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
    return true;
  }

  public Map<String, Object> getFormData() {
    Map<String, Object> data = new HashMap<>();
    data.put("name", nameField.getText().trim());
    data.put("description", descriptionArea.getText().trim());
    data.put("status", statusCombo.getSelectedItem().toString());
    return data;
  }
}
