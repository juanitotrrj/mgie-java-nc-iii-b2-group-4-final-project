package com.group4.inventoryclient.ui;

import static org.assertj.swing.edt.GuiActionRunner.execute;
import static org.assertj.swing.finder.WindowFinder.findFrame;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.group4.inventoryclient.api.ApiClient;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

public class GuestFrameUiTest extends AssertJSwingJUnitTestCase {

  @Override
  protected void onSetUp() {
    UiTestSupport.enableSwingTesting();
  }

  @Test
  public void guest_frame_shows_title() {
    GuestFrame frame =
        execute(
            () -> {
              GuestFrame guestFrame = new GuestFrame(new ApiClient("http://localhost:18080/api"));
              guestFrame.setName("guestFrame");
              guestFrame.pack();
              guestFrame.setVisible(true);
              return guestFrame;
            });

    FrameFixture window = new FrameFixture(robot(), frame);
    window.requireTitle("G4 Inventory Management System");
    window.cleanUp();
  }

  @Test
  public void guest_frame_can_be_found_by_name() {
    execute(
        () -> {
          GuestFrame guestFrame = new GuestFrame(new ApiClient("http://localhost:18080/api"));
          guestFrame.setName("guestFrame");
          guestFrame.pack();
          guestFrame.setVisible(true);
        });

    FrameFixture window = findFrame("guestFrame").withTimeout(3000).using(robot());
    assertThat(window.target(), is(notNullValue()));
    window.cleanUp();
  }
}
