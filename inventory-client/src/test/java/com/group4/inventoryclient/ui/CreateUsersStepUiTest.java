package com.group4.inventoryclient.ui;

import static org.assertj.swing.core.matcher.JLabelMatcher.withText;

import com.group4.inventoryclient.ui.steps.CreateUsersStep;
import javax.swing.JFrame;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

public class CreateUsersStepUiTest extends AssertJSwingJUnitTestCase {

  @Override
  protected void onSetUp() {
    UiTestSupport.enableSwingTesting();
  }

  @Test
  public void showsStepPanel() {
    JFrame frame =
        UiTestSupport.showFrame(
            f -> {
              CreateUsersStep step = new CreateUsersStep(UiTestSupport.testSetupApi());
              f.add(step.getPanel());
            });

    FrameFixture window = new FrameFixture(robot(), frame);
    window.requireVisible();
    window.label(withText("Username:")).requireVisible();
    window.label(withText("Full Name:")).requireVisible();
    window.cleanUp();
  }
}
