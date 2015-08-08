package com.n8.n8droid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;

public class PictureUtils {
  private static final String TAG = PictureUtils.class.getSimpleName();

  public static Bitmap getBitmapFromByteArray(byte[] byteArray) {
    if (byteArray == null) {
      return null;
    }
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inPurgeable = true;
    return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
  }

  /**
   * Save the bitmap to a file.
   *
   * @param bitmap The picture to save in the file
   * @param filePath The absolute path of the file to save the picture to
   * @return True if the bitmap has been successfully saved, false otherwise.
   */
  public static boolean saveBitmap(Bitmap bitmap, String filePath) {
    FileOutputStream out = null;

    try {
      out = new FileOutputStream(filePath);
      bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
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
   * Produces the largest possible crop rectangle of an image with the requested aspect ratio.
   * The cropped rectangle will be centered within the source image.
   * @param image The source image to be cropped
   * @param croppedAspect The desired aspect ratio
   * @return The cropped rectangle of the image with the requested aspect ratio
   */
  public static Rect getCroppedThumbnailRect(Bitmap image, float croppedAspect) {
    float aspect = (float)image.getWidth() / (float)image.getHeight();
    int croppedWidth;
    int croppedHeight;
    if (aspect > croppedAspect) {
      croppedHeight = image.getHeight();
      croppedWidth = Math.round(croppedHeight * croppedAspect);
    } else {
      croppedWidth = image.getWidth();
      croppedHeight = Math.round(croppedWidth / croppedAspect);
    }

    int croppedLeft = Math.round((image.getWidth() - croppedWidth) / 2.0f);
    int croppedTop = Math.round((image.getHeight() - croppedHeight) / 2.0f);
    return new Rect(croppedLeft, croppedTop, croppedLeft + croppedWidth, croppedTop + croppedHeight);
  }

  public static Bitmap drawableToBitmap (Drawable drawable) {
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
