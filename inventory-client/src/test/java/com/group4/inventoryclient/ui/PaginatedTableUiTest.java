package com.group4.inventoryclient.ui;

import static org.assertj.swing.core.matcher.JButtonMatcher.withText;

import com.group4.inventoryclient.ui.components.PaginatedTable;
import javax.swing.JFrame;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

public class PaginatedTableUiTest extends AssertJSwingJUnitTestCase {

  @Override
  protected void onSetUp() {
    UiTestSupport.enableSwingTesting();
  }

  @Test
  public void showsComponent() {
    JFrame frame =
        UiTestSupport.showFrame(
            f -> {
              f.add(new PaginatedTable(new String[] {"Col A", "Col B"}));
            });

    FrameFixture window = new FrameFixture(robot(), frame);
    window.requireVisible();
    window.table().requireVisible();
    window.button(withText("< Prev")).requireVisible();
    window.button(withText("Next >")).requireVisible();
    window.cleanUp();
  }
}
