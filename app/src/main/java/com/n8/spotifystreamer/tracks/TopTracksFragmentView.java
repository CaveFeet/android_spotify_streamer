/*
COPYRIGHT 1995-2015 ESRI

TRADE SECRETS: ESRI PROPRIETARY AND CONFIDENTIAL 
Unpublished material - all rights reserved under the Copyright Laws of the United States.

For additional information, contact: Environmental Systems Research Institute, Inc. 
Attn: Contracts Dept 380 New York Street Redlands, California, USA 92373

email: contracts@esri.com
*/
package com.n8.spotifystreamer.tracks;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.n8.spotifystreamer.DividerItemDecoration;
import com.n8.spotifystreamer.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class TopTracksFragmentView extends FrameLayout {

  public interface Controller {
    void onNavIconClicked();

    LinearLayoutManager getLinearLayoutManager();

    String getArtistName();
  }

  @InjectView(R.id.fragment_top_tracks_toolbar)
  Toolbar mToolbar;

  @InjectView(R.id.fragment_top_tracks_collapsingToolbarLayout)
  CollapsingToolbarLayout mCollapsingToolbarLayout;

  @InjectView(R.id.fragment_top_tracks_recyclerView)
  RecyclerView mTopTracksRecyclerView;

  @InjectView(R.id.fragment_top_tracks_artist_image_thumbnail)
  ImageView mArtistThumbnailImageView;

  @InjectView(R.id.fragment_top_tracks_artist_image_header_background)
  ImageView mArtistHeaderBackgroundImageView;

  private Activity mActivity;

  private Controller mController;

  public TopTracksFragmentView(Context context) {
    super(context);
  }

  public TopTracksFragmentView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public TopTracksFragmentView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public TopTracksFragmentView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  public void setController(@NonNull Activity activity, @NonNull Controller controller) {
    mActivity = activity;
    mController = controller;
    ButterKnife.inject(this);

    mToolbar.setNavigationIcon(R.drawable.ic_menu_back);
    mToolbar.getNavigationIcon().setColorFilter(getResources().getColor(android.R.color.white),
        PorterDuff.Mode.SRC_ATOP);
    mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        //getActivity().onBackPressed();
        mController.onNavIconClicked();
      }
    });
    mToolbar.setSubtitle(R.string.top_ten_tracks);

    mTopTracksRecyclerView.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL_LIST));
    mTopTracksRecyclerView.setLayoutManager(mController.getLinearLayoutManager());
    mTopTracksRecyclerView.setHasFixedSize(true);

    mCollapsingToolbarLayout.setTitle(mController.getArtistName());
    mCollapsingToolbarLayout.setCollapsedTitleTextColor(getResources().getColor(android.R.color.black));
    mCollapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.white));
  }

  public Toolbar getToolbar() {
    return mToolbar;
  }

  public CollapsingToolbarLayout getCollapsingToolbarLayout() {
    return mCollapsingToolbarLayout;
  }

  public RecyclerView getTopTracksRecyclerView() {
    return mTopTracksRecyclerView;
  }

  public ImageView getArtistThumbnailImageView() {
    return mArtistThumbnailImageView;
  }

  public ImageView getArtistHeaderBackgroundImageView() {
    return mArtistHeaderBackgroundImageView;
  }
}
