package com.group4.inventoryclient.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import org.junit.Test;

public class SwingUtilTest {

  @Test
  public void createBoldLabel_setsBoldFontAndText() {
    JLabel label = SwingUtil.createBoldLabel("Title");
    assertThat(label.getText(), is("Title"));
    assertThat(label.getFont().isBold(), is(true));
    assertThat(label.getFont().getStyle() & Font.BOLD, is(Font.BOLD));
  }

  @Test
  public void createTitledPanel_setsTitledBorder() {
    JPanel panel = SwingUtil.createTitledPanel("Section");
    assertThat(panel.getBorder() instanceof TitledBorder, is(true));
    TitledBorder border = (TitledBorder) panel.getBorder();
    assertThat(border.getTitle(), is("Section"));
  }
}
