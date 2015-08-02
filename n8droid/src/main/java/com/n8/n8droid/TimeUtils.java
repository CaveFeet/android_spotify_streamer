package com.n8.n8droid;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimeUtils {

  public static String getFormattedTime(long time) {
    long minutes = TimeUnit.MILLISECONDS.toMinutes(time);
    long seconds = TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(minutes);

    return String.format("%d:%d", minutes, seconds);
  }
}
