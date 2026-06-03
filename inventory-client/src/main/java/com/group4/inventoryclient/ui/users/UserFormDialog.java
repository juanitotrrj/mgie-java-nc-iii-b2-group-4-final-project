package com.group4.inventoryclient.ui.users;

import com.google.gson.JsonObject;
import com.group4.inventoryclient.ui.components.FormDialog;
import java.awt.Frame;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class UserFormDialog extends FormDialog {

  private final JTextField usernameField = new JTextField(20);
  private final JTextField fullNameField = new JTextField(20);
  private final JTextField emailField = new JTextField(20);
  private final JComboBox<String> roleCombo =
      new JComboBox<>(new String[] {"Administrator", "Manager", "Inventory Clerk", "Cashier"});
  private final JPasswordField passwordField = new JPasswordField(20);
  private final JComboBox<String> statusCombo =
      new JComboBox<>(new String[] {"Active", "Inactive"});
  private final boolean isEditMode;

  public UserFormDialog(Frame owner, JsonObject existingUser) {
    super(owner, existingUser == null ? "Add User" : "Edit User");
    this.isEditMode = existingUser != null;

    addField("Username", usernameField);
    addField("Full Name", fullNameField);
    addField("Email", emailField);
    addField("Role", roleCombo);
    if (!isEditMode) {
      addField("Password", passwordField);
    }
    addField("Status", statusCombo);

    if (isEditMode && existingUser != null) {
      if (existingUser.has("username")) {
        usernameField.setText(existingUser.get("username").getAsString());
      }
      if (existingUser.has("full_name")) {
        fullNameField.setText(existingUser.get("full_name").getAsString());
      }
      if (existingUser.has("email")) {
        emailField.setText(existingUser.get("email").getAsString());
      }
      if (existingUser.has("role")) {
        roleCombo.setSelectedItem(existingUser.get("role").getAsString());
      }
      if (existingUser.has("status")) {
        statusCombo.setSelectedItem(existingUser.get("status").getAsString());
      }
    }
  }

  @Override
  protected boolean validateForm() {
    if (usernameField.getText().trim().isEmpty()) {
      javax.swing.JOptionPane.showMessageDialog(
          this, "Username is required", "Validation Error", javax.swing.JOptionPane.ERROR_MESSAGE);
      return false;
    }
    if (fullNameField.getText().trim().isEmpty()) {
      javax.swing.JOptionPane.showMessageDialog(
          this, "Full Name is required", "Validation Error", javax.swing.JOptionPane.ERROR_MESSAGE);
      return false;
    }
    if (emailField.getText().trim().isEmpty()) {
      javax.swing.JOptionPane.showMessageDialog(
          this, "Email is required", "Validation Error", javax.swing.JOptionPane.ERROR_MESSAGE);
      return false;
    }
    if (!isEditMode && passwordField.getPassword().length == 0) {
      javax.swing.JOptionPane.showMessageDialog(
          this, "Password is required", "Validation Error", javax.swing.JOptionPane.ERROR_MESSAGE);
      return false;
    }
    return true;
  }

  public Map<String, Object> getFormData() {
    Map<String, Object> data = new HashMap<>();
    data.put("username", usernameField.getText().trim());
    data.put("full_name", fullNameField.getText().trim());
    data.put("email", emailField.getText().trim());
    data.put("role", roleCombo.getSelectedItem().toString());
    data.put("status", statusCombo.getSelectedItem().toString());
    if (!isEditMode) {
      data.put("password", new String(passwordField.getPassword()));
    }
    return data;
  }

  @Override
  public void show() {
    showAndWait();
  }
}
