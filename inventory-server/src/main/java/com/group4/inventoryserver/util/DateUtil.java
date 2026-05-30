package com.group4.inventoryserver.util;

import com.group4.inventoryserver.config.EnvConfig;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public final class DateUtil {

  private static final String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  private DateUtil() {}

  public static String nowIso() {
    SimpleDateFormat sdf = new SimpleDateFormat(ISO_FORMAT);
    sdf.setTimeZone(TimeZone.getTimeZone(EnvConfig.appTimezone()));
    return sdf.format(new Date());
  }

  public static String formatIso(Date date) {
    SimpleDateFormat sdf = new SimpleDateFormat(ISO_FORMAT);
    sdf.setTimeZone(TimeZone.getTimeZone(EnvConfig.appTimezone()));
    return sdf.format(date);
  }

  public static String format(Date date, String pattern) {
    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
    sdf.setTimeZone(TimeZone.getTimeZone(EnvConfig.appTimezone()));
    return sdf.format(date);
  }
}
