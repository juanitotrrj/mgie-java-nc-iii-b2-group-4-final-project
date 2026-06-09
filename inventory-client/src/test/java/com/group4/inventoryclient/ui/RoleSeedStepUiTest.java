package com.group4.inventoryclient.ui;

import static org.assertj.swing.core.matcher.JLabelMatcher.withText;

import com.group4.inventoryclient.ui.steps.RoleSeedStep;
import javax.swing.JFrame;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

public class RoleSeedStepUiTest extends AssertJSwingJUnitTestCase {

  @Override
  protected void onSetUp() {
    UiTestSupport.enableSwingTesting();
  }

  @Test
  public void showsStepPanel() {
    JFrame frame =
        UiTestSupport.showFrame(
            f -> {
              RoleSeedStep step = new RoleSeedStep(UiTestSupport.testSetupApi());
              f.add(step.getPanel());
            });

    FrameFixture window = new FrameFixture(robot(), frame);
    window.requireVisible();
    window.label(withText("Select roles to create:")).requireVisible();
    window.list().requireVisible();
    window.cleanUp();
  }
}
