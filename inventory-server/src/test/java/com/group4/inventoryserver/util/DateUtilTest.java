package com.group4.inventoryserver.util;

import static org.junit.Assert.*;

import java.util.Date;
import org.junit.Test;

public class DateUtilTest {

  @Test
  public void nowIso_returnsFormattedTimestamp() {
    String iso = DateUtil.nowIso();

    assertNotNull(iso);
    assertTrue(iso.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z"));
  }

  @Test
  public void formatIso_formatsDate() {
    Date date = new Date(0L);
    String iso = DateUtil.formatIso(date);

    assertNotNull(iso);
    assertTrue(iso.endsWith("Z"));
  }

  @Test
  public void format_usesCustomPattern() {
    Date date = new Date(0L);
    String formatted = DateUtil.format(date, "yyyy-MM-dd");

    assertEquals(10, formatted.length());
    assertTrue(formatted.matches("\\d{4}-\\d{2}-\\d{2}"));
  }
}
