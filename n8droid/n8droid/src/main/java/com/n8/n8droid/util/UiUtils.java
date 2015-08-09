package com.n8.n8droid.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.n8.n8droid.R;

public class UiUtils {

  /**
   * Check if device has an extra-large screen.
   */
  public static boolean isXLargeTablet(@NonNull Context context) throws NullPointerException {
    ObjectUtils.requireNonNull(context, "Context must not be null");

    return (context.getResources().getConfiguration().screenLayout
        & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
  }

  /**
   *  Check if device is a tablet (sw600dp).
   */
  public static boolean isTablet(@NonNull Context context) throws NullPointerException {
    ObjectUtils.requireNonNull(context, "Context must not be null");

    return context.getResources().getBoolean(R.bool.isTablet);
  }

  /**
   * @return True if the device has at least a 7in screen
   */
  public static boolean is7inTablet(@NonNull Context context) throws NullPointerException {
    ObjectUtils.requireNonNull(context, "Context must not be null");

    return context.getResources().getBoolean(R.bool.is7inTablet);
  }

  public static boolean is10inTablet(@NonNull Context context) throws NullPointerException {
    ObjectUtils.requireNonNull(context, "Context must not be null");

    return context.getResources().getBoolean(R.bool.is10inTablet);
  }

  public static boolean isLandscape(@NonNull Context context) throws NullPointerException {
    ObjectUtils.requireNonNull(context, "Context must not be null");

    return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
  }

  public static boolean isPortrait(@NonNull Context context) throws NullPointerException {
    ObjectUtils.requireNonNull(context, "Context must not be null");

    return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
  }

  public static int dipsToPixels(int dips) {
    if (dips == 0) {
      return 0;
    }

    float scale = Resources.getSystem().getDisplayMetrics().density;
    return (int) (dips * scale + 0.5f);
  }

  public static void showToast(@NonNull Context context, String message) throws NullPointerException{
    ObjectUtils.requireNonNull(context, "Context must not be null");

    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
  }
}
