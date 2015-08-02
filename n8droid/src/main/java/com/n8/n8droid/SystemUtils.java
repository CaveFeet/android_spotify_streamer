package com.n8.n8droid;

import android.app.ActivityManager;
import android.content.Context;
import android.support.annotation.NonNull;

public class SystemUtils {

  /**
   * Checks for a running instance of the specified class.
   *
   * @param context
   * @param serviceClass Service class to check for a running instance of.
   *
   * @return True if a running instance of the class was found, false otherwise.
   */
  private boolean isServiceRunning(@NonNull Context context, Class<?> serviceClass) {
    if (context == null) {
      return false;
    }

    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
      if (serviceClass.getName().equals(service.service.getClassName())) {
        return true;
      }
    }
    return false;
  }

}
