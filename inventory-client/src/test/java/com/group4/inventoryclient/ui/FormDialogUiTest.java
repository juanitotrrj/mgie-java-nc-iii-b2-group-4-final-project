package com.group4.inventoryclient.ui;

import static org.assertj.swing.core.matcher.JButtonMatcher.withText;

import com.group4.inventoryclient.ui.products.ProductFormDialog;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

public class FormDialogUiTest extends AssertJSwingJUnitTestCase {

  @Override
  protected void onSetUp() {
    UiTestSupport.enableSwingTesting();
  }

  @Test
  public void formDialogHasStandardButtons() {
    ProductFormDialog formDialog =
        UiTestSupport.showDialog(owner -> new ProductFormDialog(owner, null));

    DialogFixture dialog = new DialogFixture(robot(), formDialog);
    dialog.show();
    dialog.requireVisible();
    dialog.button(withText("Save")).requireVisible();
    dialog.button(withText("Cancel")).requireVisible();
    dialog.cleanUp();
  }
}
