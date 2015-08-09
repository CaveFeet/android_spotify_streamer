package com.n8.n8droid.viewcontroller;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Abstract base class for a custom view used to encapsulate view specific logic.
 * <p>
 *   Concrete implementations of this class should be used as a root level layout element.  Define the remaining layout
 *   as normal within the custom view.  {@link #setController(FragmentActivity, ViewController)} should be
 *   called to supply a {@link ViewController} that will be notified of view events
 *   such as button or menu item clicks.
 * </p>
 * @param <T> Controller type
 */
public abstract class BaseFragmentView<T extends ViewController> extends FrameLayout {

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

  protected FragmentActivity mActivity;

  protected T mController;

  public void setController(@NonNull FragmentActivity activity, @NonNull T controller) {
    mActivity = activity;
    mController = controller;

    setupView();
  }

  protected abstract void setupView();
}