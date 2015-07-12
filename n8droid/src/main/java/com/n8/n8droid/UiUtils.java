package com.n8.n8droid;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

/**
 * Created by n8 on 7/12/15.
 */
public class UiUtils {
  /**
   * Helper method to determine if the device has an extra-large screen. For
   * example, 10" tablets are extra-large.
   */
  public static boolean isXLargeTablet(Context context) {
    return (context.getResources().getConfiguration().screenLayout
        & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
  }

  public static boolean isTablet(Context context) {
    return context.getResources().getBoolean(R.bool.isTablet);
  }

  /**
   * @return True if the device has at least a 7in screen
   */
  public static boolean is7inTablet(Context context) {
    return context.getResources().getBoolean(R.bool.is7inTablet);
  }

  public static boolean is10inTablet(Context context) {
    return context.getResources().getBoolean(R.bool.is10inTablet);
  }

  public static boolean isLandscape(Context context) {
    return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
  }

  public static boolean isPortrait(Context context) {
    return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
  }

  public static int dipsToPixels(int dips) {
    if (dips == 0) {
      return 0;
    }

    float scale = Resources.getSystem().getDisplayMetrics().density;
    return (int) (dips * scale + 0.5f);
  }
}
