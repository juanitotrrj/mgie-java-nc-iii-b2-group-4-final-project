package com.group4.inventoryclient.ui;

import static org.assertj.swing.edt.GuiActionRunner.execute;

import com.group4.inventoryclient.ui.users.UserFormDialog;
import javax.swing.JFrame;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

public class UserFormDialogUiTest extends AssertJSwingJUnitTestCase {

  @Override
  protected void onSetUp() {
    UiTestSupport.enableSwingTesting();
  }

  @Test
  public void showsSaveAndCancelButtons() {
    UserFormDialog formDialog = execute(() -> new UserFormDialog(new JFrame(), null));
    UiTestSupport.assertDialogHasButtons(formDialog, "Save", "Cancel");
  }
}
