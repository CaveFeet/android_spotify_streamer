package com.n8.n8droid.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;

import com.n8.n8droid.R;

public class AndroidUtils {

  /**
   * Convenience method to determine if the current thread is the main thread (UI thread)
   *
   * @return if the thread this method is called from is the UI thread
   */
  public static boolean isRunningInUiThread() {
    return (Looper.myLooper() == Looper.getMainLooper());
  }

  /**
   * Returns the application label or 'Unknown' if retrieving the label fails.
   *
   * @param context Context used to get application info.
   * @return The application's label.
   */
  public static String getAppLabel(@NonNull Context context) throws NullPointerException {
    ObjectUtils.requireNonNull(context, "Context must not be null");

    PackageManager packageManager = context.getPackageManager();
    ApplicationInfo applicationInfo = null;
    try {
      applicationInfo = packageManager.getApplicationInfo(context.getApplicationInfo().packageName, 0);
    } catch (final PackageManager.NameNotFoundException e) {
    }
    return applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo).toString() : context.getString(
        R
        .string.n8droid_unknown);
  }

  /**
   * Checks whether device currently is connected, or is connecting to, a network.
   *
   * @return True if connected or connecting, False otherwise.
   */
  public static boolean hasInternetConnection(@NonNull Context context) throws NullPointerException {
    ObjectUtils.requireNonNull(context, "Context must not be null");

    ConnectivityManager connectivityManager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

    return networkInfo != null && networkInfo.isConnectedOrConnecting();
  }

  /**
   * Checks whether device has an internet connection.
   * <p>
   *  If no connection is available, a dialog is shown with a 'Settings' button that will take the
   *  user to the phone settings so they may enable a network connection to use the app.
   * </p>
   *
   * @param context Context context needed to show the dialog.  Must not be null;
   *
   * @return True if an internet connection exists, false otherwise.
   */
  public static boolean checkInternetAccessWithAlert(@NonNull final Context context) throws NullPointerException {
    return checkInternetAccessWithAlert(context, context.getString(R.string.n8droid_turn_on_wifi_to_access_internet));
  }

  public static boolean checkInternetAccessWithAlert(@NonNull final Context context, String message) throws NullPointerException{
    ObjectUtils.requireNonNull(context, "Context must not be null");

    boolean hasConnection = hasInternetConnection(context);

    if (hasConnection) {
      return hasConnection;
    }

    // If there is no connection available, show an alert dialog.  The dialog has an OK button that dismisses the dialog,
    // and a SETTINGS button that will take the user to the phone settings.
    //
    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setTitle(R.string.n8droid_unable_to_access_internet);
    builder.setMessage(message);
    builder.setNegativeButton(R.string.n8droid_cancel, null);
    builder.setPositiveButton(R.string.n8droid_settings, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        // show the system settings activty
        Intent settingIntent = new Intent(Settings.ACTION_SETTINGS);
        settingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(settingIntent);
      }
    });

    builder.show();

    return false;
  }
}
