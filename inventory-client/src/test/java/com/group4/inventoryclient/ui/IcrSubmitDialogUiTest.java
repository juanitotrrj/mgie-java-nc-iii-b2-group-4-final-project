package com.group4.inventoryclient.ui;

import static org.assertj.swing.core.matcher.JButtonMatcher.withText;

import com.group4.inventoryclient.ui.icr.IcrSubmitDialog;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

public class IcrSubmitDialogUiTest extends AssertJSwingJUnitTestCase {

  @Override
  protected void onSetUp() {
    UiTestSupport.enableSwingTesting();
  }

  @Test
  public void showsSaveAndCancelButtons() {
    IcrSubmitDialog formDialog = UiTestSupport.showDialog(owner -> new IcrSubmitDialog(owner));

    DialogFixture dialog = new DialogFixture(robot(), formDialog);
    dialog.show();
    dialog.requireVisible();
    dialog.button(withText("Save")).requireVisible();
    dialog.button(withText("Cancel")).requireVisible();
    dialog.cleanUp();
  }
}
