package com.group4.inventoryclient.ui;

import static org.assertj.swing.core.matcher.JButtonMatcher.withText;

import com.group4.inventoryclient.ui.components.ExportButton;
import javax.swing.JFrame;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

public class ExportButtonUiTest extends AssertJSwingJUnitTestCase {

  @Override
  protected void onSetUp() {
    UiTestSupport.enableSwingTesting();
  }

  @Test
  public void showsComponent() {
    JFrame frame =
        UiTestSupport.showFrame(
            f -> {
              f.add(
                  new ExportButton(
                      UiTestSupport.testApiClient(), "/products/export?format=csv", f));
            });

    FrameFixture window = new FrameFixture(robot(), frame);
    window.requireVisible();
    window.button(withText("Export")).requireVisible();
    window.cleanUp();
  }
}
