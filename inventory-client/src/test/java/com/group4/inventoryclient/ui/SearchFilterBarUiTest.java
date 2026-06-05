package com.group4.inventoryclient.ui;

import static org.assertj.swing.core.matcher.JButtonMatcher.withText;

import com.group4.inventoryclient.ui.components.SearchFilterBar;
import javax.swing.JFrame;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

public class SearchFilterBarUiTest extends AssertJSwingJUnitTestCase {

  @Override
  protected void onSetUp() {
    UiTestSupport.enableSwingTesting();
  }

  @Test
  public void showsComponent() {
    JFrame frame =
        UiTestSupport.showFrame(
            f -> {
              f.add(new SearchFilterBar());
            });

    FrameFixture window = new FrameFixture(robot(), frame);
    window.requireVisible();
    window.textBox().requireVisible();
    window.button(withText("Search")).requireVisible();
    window.button(withText("Clear")).requireVisible();
    window.cleanUp();
  }
}
