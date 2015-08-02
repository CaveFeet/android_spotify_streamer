package com.n8.n8droid;


import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseViewControllerFragment<T extends BaseFragmentView> extends Fragment {

  public static final String TAG = BaseViewControllerFragment.class.getSimpleName();

  protected T mView;

  public BaseViewControllerFragment() { }


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
