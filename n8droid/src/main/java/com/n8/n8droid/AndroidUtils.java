package com.n8.n8droid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.widget.Toast;

public class AndroidUtils {

  public static void showToast(@NonNull Context context, String message) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
  }

  /**
   * Returns the application label or 'Unknown' if retrieving the label fails.
   *
   * @param context Context used to get application info.
   * @return The application's label.
   */
  public static String getAppLabel(Context context) {
    PackageManager packageManager = context.getPackageManager();
    ApplicationInfo applicationInfo = null;
    try {
      applicationInfo = packageManager.getApplicationInfo(context.getApplicationInfo().packageName, 0);
    } catch (final PackageManager.NameNotFoundException e) {
    }
    return applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo).toString() : context.getString(R
        .string.n8droid_unknown);
  }

  /**
   * Checks whether the device currently is connected to or is connecting to a network.
   *
   * @return True if connected to or connecting to an internet connection, False otherwise.
   */
  public static boolean hasInternetConnection(@NonNull Context context) {
    ConnectivityManager connectivityManager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

    NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

    return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
  }

  /**
   * Checks whether the device has an internet connection.  If no connection is available, a dialog is shown with a
   * Settings button that will take the user to the phone settings so they may enable a network connection to use the
   * app.  This dialog shows a title and message specific to needing internet access to use the app.
   *
   * @param context Context context needed to show the dialog
   *
   * @return True if an internet connection exists, false otherwise.
   */
  public static boolean checkInternetAccessWithAlert(@NonNull final Context context) {
    return checkInternetAccessWithAlert(context, context.getString(R.string.n8droid_turn_on_wifi_to_access_internet));
  }

  public static boolean checkInternetAccessWithAlert(@NonNull final Context context, String message) {
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
