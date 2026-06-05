package com.group4.inventoryclient.ui;

import static org.assertj.swing.edt.GuiActionRunner.execute;

import com.group4.inventoryclient.ui.users.ResetPasswordDialog;
import javax.swing.JFrame;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

public class ResetPasswordDialogUiTest extends AssertJSwingJUnitTestCase {

  @Override
  protected void onSetUp() {
    UiTestSupport.enableSwingTesting();
  }

  @Test
  public void showsSaveAndCancelButtons() {
    ResetPasswordDialog formDialog = execute(() -> new ResetPasswordDialog(new JFrame()));
    UiTestSupport.assertDialogHasButtons(formDialog, "Save", "Cancel");
  }
}
