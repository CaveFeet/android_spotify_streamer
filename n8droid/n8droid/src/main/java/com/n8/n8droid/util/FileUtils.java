package com.n8.n8droid.util;

import android.content.Context;
import android.support.annotation.NonNull;

import com.n8.n8droid.R;

import java.io.File;

public class FileUtils {
  private static final String TAG = FileUtils.class.getSimpleName();

  public static final int THOUSAND_BYTES = 1024;

  public static final int[] UNITS_ABBREVIATION_IDS = new int[] {
      R.string.n8droid_file_size_abbreviation_byte,
      R.string.n8droid_file_size_abbreviation_kilobyte,
      R.string.n8droid_file_size_abbreviation_megabyte,
      R.string.n8droid_file_size_abbreviation_gigabyte,
      R.string.n8droid_file_size_abbreviation_terabyte
  };

  public static final boolean deleteRecursive(File fileOrDirectory) {
    return deleteRecursive(fileOrDirectory, true);
  }

  /**
   * Delete the file given in parameter. In case of a folder, it will delete it and its content recursively.
   *
   * @param fileOrDirectory File to delete.
   * @return Returns false if there was nothing to delete or the deletion failed, returns true otherwise.
   */
  public static final boolean deleteRecursive(File fileOrDirectory, boolean deleteParent) {
    boolean deleteCurrentLevel = deleteParent;
    if (fileOrDirectory == null) {
      return false;
    }

    if (fileOrDirectory.isDirectory()) {
      File[] listFiles = fileOrDirectory.listFiles();

      if (listFiles != null) {
        for (File child : listFiles) {
          if (!deleteRecursive(child, true)) {
            return false;
          }
        }
      }
    }

    if (deleteCurrentLevel) {
      return fileOrDirectory.delete();
    }
    return true;
  }

  /**
   * Takes a file size, in bytes, and returns a more readable, formatted string.  ex. 4.1 MB
   * @param size Size, in bytes, to be converted into a string.
   *
   * @return A formatted string representation of the passed file size.
   */
  public static String getReadableFileSize(@NonNull Context context, long size) {

    if (size < 0) {
      return context.getString(R.string.n8droid_file_size_abbreviation_unknown);
    } else if (size == 0) {
      return "0";
    }

    for (int i = UNITS_ABBREVIATION_IDS.length; i >= 0; i--) {
      double step = Math.pow(THOUSAND_BYTES, i);
      if (size > step) {
        return String.format("%3.1f %s", size / step, context.getString(UNITS_ABBREVIATION_IDS[i]));
      }
    }

    return Long.toString(size);
  }
}
