package com.group4.inventoryclient.ui.users;

import com.group4.inventoryclient.ui.components.FormDialog;
import java.awt.Frame;
import java.util.Arrays;
import javax.swing.JPasswordField;

public class ResetPasswordDialog extends FormDialog {

  private final JPasswordField newPasswordField = new JPasswordField(20);
  private final JPasswordField confirmPasswordField = new JPasswordField(20);

  public ResetPasswordDialog(Frame owner) {
    super(owner, "Reset Password");
    addField("New Password", newPasswordField);
    addField("Confirm Password", confirmPasswordField);
  }

  @Override
  protected boolean validateForm() {
    char[] newPass = newPasswordField.getPassword();
    char[] confirmPass = confirmPasswordField.getPassword();

    if (newPass.length == 0) {
      javax.swing.JOptionPane.showMessageDialog(
          this,
          "New password is required",
          "Validation Error",
          javax.swing.JOptionPane.ERROR_MESSAGE);
      return false;
    }

    if (!Arrays.equals(newPass, confirmPass)) {
      javax.swing.JOptionPane.showMessageDialog(
          this,
          "Passwords do not match",
          "Validation Error",
          javax.swing.JOptionPane.ERROR_MESSAGE);
      return false;
    }

    return true;
  }

  public String getNewPassword() {
    return new String(newPasswordField.getPassword());
  }

  @Override
  public void show() {
    showAndWait();
  }
}
