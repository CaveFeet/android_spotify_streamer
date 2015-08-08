package com.n8.n8droid;

import android.support.annotation.IntDef;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class AnnotationUtils {

  @IntDef({ View.VISIBLE, View.INVISIBLE, View.GONE})
  @Retention(RetentionPolicy.SOURCE)
  public @interface ViewVisibility {}

}
