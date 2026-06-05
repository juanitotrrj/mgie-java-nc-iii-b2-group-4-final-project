package com.group4.inventoryclient.ui;

import static org.assertj.swing.core.matcher.JLabelMatcher.withText;

import com.group4.inventoryclient.ui.steps.BusinessSettingsStep;
import javax.swing.JFrame;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

public class BusinessSettingsStepUiTest extends AssertJSwingJUnitTestCase {

  @Override
  protected void onSetUp() {
    UiTestSupport.enableSwingTesting();
  }

  @Test
  public void showsStepPanel() {
    JFrame frame =
        UiTestSupport.showFrame(
            f -> {
              BusinessSettingsStep step = new BusinessSettingsStep(UiTestSupport.testSetupApi());
              f.add(step.getPanel());
            });

    FrameFixture window = new FrameFixture(robot(), frame);
    window.requireVisible();
    window.label(withText("Company Name:")).requireVisible();
    window.label(withText("Currency Code:")).requireVisible();
    window.cleanUp();
  }
}
