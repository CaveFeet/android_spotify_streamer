/*
COPYRIGHT 1995-2015 ESRI

TRADE SECRETS: ESRI PROPRIETARY AND CONFIDENTIAL 
Unpublished material - all rights reserved under the Copyright Laws of the United States.

For additional information, contact: Environmental Systems Research Institute, Inc. 
Attn: Contracts Dept 380 New York Street Redlands, California, USA 92373

email: contracts@esri.com
*/
package com.n8.n8droid;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseViewControllerDialogFragment<T extends BaseFragmentView> extends DialogFragment {

  public static final String TAG = BaseViewControllerFragment.class.getSimpleName();

  protected T mView;

  public BaseViewControllerDialogFragment() { }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    try {
      // Inflate the layout for this fragment
      mView = (T) inflater.inflate(getLayoutId(), container, false);
    } catch (ClassCastException e) {
      Log.e(TAG, "Root layout element was not of the correct, custom BaseFragmentView type. " + e.getMessage());
    }

    setViewController();

    return mView;
  }

  @LayoutRes
  protected abstract int getLayoutId();

  protected abstract void setViewController();
}
