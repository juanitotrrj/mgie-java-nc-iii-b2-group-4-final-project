package com.group4.inventoryclient.ui;

import static org.assertj.swing.core.matcher.JButtonMatcher.withText;

import com.group4.inventoryclient.api.ApiClient;
import com.group4.inventoryclient.ui.categories.CategoryListPanel;
import javax.swing.JFrame;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

public class CategoryListPanelUiTest extends AssertJSwingJUnitTestCase {

  @Override
  protected void onSetUp() {
    UiTestSupport.enableSwingTesting();
  }

  @Test
  public void showsTable() {
    JFrame frame =
        UiTestSupport.showFrame(
            f -> {
              ApiClient apiClient = UiTestSupport.testApiClient();
              f.add(new CategoryListPanel(UiTestSupport.testCategoryApi(), apiClient, f));
            });

    FrameFixture window = new FrameFixture(robot(), frame);
    UiTestSupport.dismissBlockingDialogs(robot());
    window.requireVisible();
    window.button(withText("Add")).requireVisible();
    window.cleanUp();
  }
}
