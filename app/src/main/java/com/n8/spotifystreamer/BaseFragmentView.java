/*
COPYRIGHT 1995-2015 ESRI

TRADE SECRETS: ESRI PROPRIETARY AND CONFIDENTIAL 
Unpublished material - all rights reserved under the Copyright Laws of the United States.

For additional information, contact: Environmental Systems Research Institute, Inc. 
Attn: Contracts Dept 380 New York Street Redlands, California, USA 92373

email: contracts@esri.com
*/
package com.n8.spotifystreamer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.n8.spotifystreamer.artists.ArtistSearchController;
import com.n8.spotifystreamer.artists.ArtistSearchFragmentView;

import butterknife.ButterKnife;

/**
 * Abstract base class for a custom view used to encapsulate view specific logic.
 * <p>
 *   Concrete implementations of this class should be used as a root level layout element.  Define the remaining layout
 *   as normal within the custom view.  {@link #setController(AppCompatActivity, Controller)} should be
 *   called to supply a {@link com.n8.spotifystreamer.BaseFragmentView.Controller} that will be notified of view events
 *   such as button or menu item clicks.
 * </p>
 * @param <T> Controller type
 */
public abstract class BaseFragmentView<T extends ArtistSearchFragmentView.Controller> extends FrameLayout {

  public interface Controller {

  }

  public BaseFragmentView(Context context) {
    super(context);
  }

  public BaseFragmentView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public BaseFragmentView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public BaseFragmentView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  protected AppCompatActivity mActivity;

  protected T mController;

  public void setController(@NonNull AppCompatActivity activity, @NonNull T controller) {
    mActivity = activity;
    mController = controller;

    setupView();
  }

  /**
   * Override this method to setup/initialize any child views or to install any listeners used to notify the
   * {@link com.n8.spotifystreamer.BaseFragmentView.Controller}.
   */
  protected abstract void setupView();
}
