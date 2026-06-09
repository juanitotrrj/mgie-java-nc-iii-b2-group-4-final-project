package com.group4.inventoryclient.ui;

import com.group4.inventoryclient.ui.settings.SettingsPanel;
import javax.swing.JFrame;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

public class SettingsPanelUiTest extends AssertJSwingJUnitTestCase {

  @Override
  protected void onSetUp() {
    UiTestSupport.enableSwingTesting();
  }

  @Test
  public void showsSettingsTabs() {
    JFrame frame =
        UiTestSupport.showFrame(
            f -> {
              f.add(new SettingsPanel(UiTestSupport.testApiClient()));
            });

    FrameFixture window = new FrameFixture(robot(), frame);
    UiTestSupport.dismissBlockingDialogs(robot());
    window.requireVisible();
    window.cleanUp();
  }
}
