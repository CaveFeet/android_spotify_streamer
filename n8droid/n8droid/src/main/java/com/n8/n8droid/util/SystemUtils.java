package com.n8.n8droid.util;

import android.app.ActivityManager;
import android.content.Context;
import android.support.annotation.NonNull;

public class SystemUtils {

  /**
   * Checks for a running instance of the specified service class.
   *
   * @see {http://stackoverflow.com/questions/600207/how-to-check-if-a-service-is-running-on-android}
   *
   * @param context Required to query running services.
   * @param serviceClass Service class to check for a running instance of.
   *
   * @return True if a running instance of the class was found, false otherwise.
   */
  private boolean isServiceRunning(@NonNull Context context, Class<?> serviceClass) {
    if (context == null || serviceClass == null) {
      return false;
    }

    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
      if (serviceClass.getName().equals(service.service.getClassName())) {
        return true;
      }
    }
    return false;
  }

}
