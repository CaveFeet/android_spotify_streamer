/*
COPYRIGHT 1995-2015 ESRI

TRADE SECRETS: ESRI PROPRIETARY AND CONFIDENTIAL 
Unpublished material - all rights reserved under the Copyright Laws of the United States.

For additional information, contact: Environmental Systems Research Institute, Inc. 
Attn: Contracts Dept 380 New York Street Redlands, California, USA 92373

email: contracts@esri.com
*/
package com.n8.spotifystreamer;

import android.support.annotation.NonNull;

import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

//import kaaes.spotify.webapi.android.models.Image;

public class ImageUtils {

  public static final int INVALID_IMAGE_INDEX = -1;

  public static int getIndexOfClosestSizeImage(@NonNull List<Image> images, float imageSize) {
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
}
