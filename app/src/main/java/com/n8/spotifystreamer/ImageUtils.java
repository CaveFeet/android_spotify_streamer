/*
COPYRIGHT 1995-2015 ESRI

TRADE SECRETS: ESRI PROPRIETARY AND CONFIDENTIAL 
Unpublished material - all rights reserved under the Copyright Laws of the United States.

For additional information, contact: Environmental Systems Research Institute, Inc. 
Attn: Contracts Dept 380 New York Street Redlands, California, USA 92373

email: contracts@esri.com
*/
package com.n8.spotifystreamer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.n8.spotifystreamer.models.ParcelableImage;

import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

//import kaaes.spotify.webapi.android.models.Image;

public class ImageUtils {

  public static final int INVALID_IMAGE_INDEX = -1;

  public static int getIndexOfClosestSizeImage(@NonNull List<ParcelableImage> images, float imageSize) {
    if (images == null || images.size() == 0) {
      return INVALID_IMAGE_INDEX;
    }

    double closestRatio = Math.abs(((double) images.get(0).height) / imageSize - 1);
    int closesIndex = 0;

    for (int i = 1; i < images.size(); i++) {
      double ratio = Math.abs(((double) images.get(i).height) / imageSize - 1);
      if (ratio < closestRatio) {
        closestRatio = ratio;
        closesIndex = i;
      }
    }

    return closesIndex;
  }

  public static Bitmap drawableToBitmap (Drawable drawable) {
    Bitmap bitmap = null;

    if (drawable instanceof BitmapDrawable) {
      BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
      if(bitmapDrawable.getBitmap() != null) {
        return bitmapDrawable.getBitmap();
      }
    }

    if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
      bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
    } else {
      bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
    }

    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);
    return bitmap;
  }
}
