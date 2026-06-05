package com.group4.inventoryclient.ui;

import static org.assertj.swing.core.matcher.JButtonMatcher.withText;
import static org.assertj.swing.edt.GuiActionRunner.execute;

import com.group4.inventoryclient.config.ClientConfig;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

public class WizardFrameUiTest extends AssertJSwingJUnitTestCase {

  @Override
  protected void onSetUp() {
    UiTestSupport.enableSwingTesting();
  }

  @Test
  public void opens_with_title_and_navigation_buttons() {
    WizardFrame frame =
        execute(
            () -> {
              WizardFrame wizardFrame =
                  new WizardFrame(new ClientConfig("http://localhost:18080/api"));
              wizardFrame.setName("wizardFrame");
              wizardFrame.pack();
              wizardFrame.setVisible(true);
              return wizardFrame;
            });

    FrameFixture window = new FrameFixture(robot(), frame);
    UiTestSupport.dismissBlockingDialogs(robot());
    window.requireTitle("G4IMS - Initial Setup Wizard");
    window.button(withText("< Back")).requireVisible();
    window.button(withText("Next >")).requireVisible();
    window.cleanUp();
  }
}
