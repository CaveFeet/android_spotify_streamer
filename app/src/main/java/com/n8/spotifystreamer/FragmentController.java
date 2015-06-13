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
import android.support.v4.app.FragmentActivity;

public abstract class FragmentController<T> {

  protected FragmentActivity mActivity;

  protected T mView;

  public FragmentController(@NonNull FragmentActivity activity) {
    mActivity = activity;
  }

  public void onCreateView(@NonNull T view){
    mView = view;
  }

  public void onDetachView() {

  }
}
