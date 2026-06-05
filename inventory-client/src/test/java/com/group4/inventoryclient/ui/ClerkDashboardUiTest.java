package com.group4.inventoryclient.ui;

import static org.assertj.swing.core.matcher.JLabelMatcher.withText;

import com.group4.inventoryclient.ui.dashboard.ClerkDashboard;
import javax.swing.JFrame;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

public class ClerkDashboardUiTest extends AssertJSwingJUnitTestCase {

  @Override
  protected void onSetUp() {
    UiTestSupport.enableSwingTesting();
  }

  @Test
  public void showsMetricCards() {
    JFrame frame =
        UiTestSupport.showFrame(
            f -> {
              f.add(new ClerkDashboard(UiTestSupport.testDashboardApi()));
            });

    FrameFixture window = new FrameFixture(robot(), frame);
    UiTestSupport.dismissBlockingDialogs(robot());
    window.requireVisible();
    window.label(withText("My Pending ICRs")).requireVisible();
    window.label(withText("Products Managed")).requireVisible();
    window.cleanUp();
  }
}
