package com.n8.n8droid.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;

public class PictureUtils {
  private static final String TAG = PictureUtils.class.getSimpleName();

  public static Bitmap getBitmapFromByteArray(@NonNull byte[] array) {
    if (array == null) {
      return null;
    }

    return BitmapFactory.decodeByteArray(array, 0, array.length, new BitmapFactory.Options());
  }

  /**
   * Save a bitmap to a file.
   *
   * @param image The image to save in the file
   * @param filePath The absolute path of the file to save the picture to
   *
   * @return True if the image has been successfully saved, false otherwise.
   */
  public static boolean saveBitmap(@NonNull Bitmap image, @NonNull String filePath) {
    if (image == null || filePath == null) {
      return false;
    }

    FileOutputStream out = null;

    try {
      out = new FileOutputStream(filePath);
      image.compress(Bitmap.CompressFormat.PNG, 90, out);
      out.flush();
      out.close();
    } catch (Exception e) {
      Log.e(TAG, "saveBitmap: error during file saving: ", e);
      return false;
    } finally {
      try {
        if (out != null) {
          out.close();
        }
      } catch (IOException ignore) {
        // Ignore
      }
    }
    return true;
  }

  /**
   * Converts a drawable into a bitmap.
   *
   * @param drawable The drawable to convert.
   *
   * @return Bitmap for the passed drawable, null if it fails.
   */
  public static Bitmap drawableToBitmap (@NonNull Drawable drawable) {
    if (drawable == null) {
      return null;
    }

    if (drawable instanceof BitmapDrawable) {
      return ((BitmapDrawable) drawable).getBitmap();
    }

    int width = Math.max(drawable.getIntrinsicWidth(), 1);
    int height = Math.max(drawable.getIntrinsicHeight(), 1);

    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);

    return bitmap;
  }
}
