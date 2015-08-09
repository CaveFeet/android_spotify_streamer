package com.n8.n8droid.util;

import java.util.concurrent.TimeUnit;

public class TimeUtils {

  /**
   * Converts a time in milliseconds into a formatted min:sec string.
   *
   * @param time Time in milliseconds
   *
   * @return Formatted time string.
   */
  public static String getFormattedTime(long time) {
    long minutes = TimeUnit.MILLISECONDS.toMinutes(time);
    long seconds = TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(minutes);

    return String.format("%d:%d", minutes, seconds);
  }
}
