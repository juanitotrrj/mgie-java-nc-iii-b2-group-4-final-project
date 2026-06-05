package com.group4.inventoryclient.ui;

import static org.assertj.swing.core.matcher.JButtonMatcher.withText;
import static org.assertj.swing.edt.GuiActionRunner.execute;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItemInArray;

import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JListFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

public class MainFrameUiTest extends AssertJSwingJUnitTestCase {

  @Override
  protected void onSetUp() {
    UiTestSupport.enableSwingTesting();
    UiTestSupport.loginAsAdmin();
  }

  @Override
  protected void onTearDown() {
    UiTestSupport.resetSession();
  }

  @Test
  public void shows_title_and_navigation() {
    MainFrame frame =
        execute(
            () -> {
              MainFrame mainFrame = new MainFrame(UiTestSupport.testApiClient());
              mainFrame.setName("mainFrame");
              mainFrame.pack();
              mainFrame.setVisible(true);
              return mainFrame;
            });

    FrameFixture window = new FrameFixture(robot(), frame);
    UiTestSupport.dismissBlockingDialogs(robot());
    window.requireTitle("G4 Inventory Management System");

    JListFixture navList = window.list();
    assertThat(navList.contents(), hasItemInArray("Dashboard"));
    assertThat(navList.contents(), hasItemInArray("Products"));
    assertThat(navList.contents(), hasItemInArray("Categories"));

    JButtonFixture logoutBtn = window.button(withText("Logout"));
    logoutBtn.requireVisible();
    logoutBtn.requireEnabled();
    window.cleanUp();
  }
}
