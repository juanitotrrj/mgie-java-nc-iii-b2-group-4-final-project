package com.group4.inventoryclient.ui;

import static org.assertj.swing.core.matcher.JLabelMatcher.withText;

import com.group4.inventoryclient.ui.components.MetricCardPanel;
import java.awt.Color;
import javax.swing.JFrame;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

public class MetricCardPanelUiTest extends AssertJSwingJUnitTestCase {

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
                  new MetricCardPanel(
                      "Test Metric", "42", "Sample description", new Color(59, 130, 246)));
            });

    FrameFixture window = new FrameFixture(robot(), frame);
    window.requireVisible();
    window.label(withText("Test Metric")).requireVisible();
    window.label(withText("42")).requireVisible();
    window.cleanUp();
  }
}
