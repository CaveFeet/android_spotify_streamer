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
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.n8.n8droid.BaseFragmentView;
import com.n8.n8droid.UiUtils;
import com.n8.spotifystreamer.DividerItemDecoration;
import com.n8.spotifystreamer.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Custom view that encapsulates view specific setup/logic for {@link TopTracksFragment}
 */
public class TopTracksFragmentView extends BaseFragmentView<TopTracksController> {

  @InjectView(R.id.fragment_top_tracks_toolbar)
  Toolbar mToolbar;

  @InjectView(R.id.fragment_top_tracks_no_content_layout)
  View mNoContentView;

  @InjectView(R.id.fragment_top_tracks_collapsingToolbarLayout)
  CollapsingToolbarLayout mCollapsingToolbarLayout;

  @InjectView(R.id.fragment_top_tracks_recyclerView)
  RecyclerView mTopTracksRecyclerView;

  @InjectView(R.id.fragment_top_tracks_artist_image_thumbnail)
  ImageView mArtistThumbnailImageView;

  @InjectView(R.id.fragment_top_tracks_play_all_floatingActionButton)
  FloatingActionButton mPlayAllButton;

  @InjectView(R.id.fragment_top_tracks_artist_image_header_background)
  ImageView mArtistHeaderBackgroundImageView;
  private ShareActionProvider mShareActionProvider;

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

  @Override
  protected void setupView() {
    ButterKnife.inject(this);

    if (!UiUtils.isTablet(mActivity)) {
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
      mToolbar.inflateMenu(R.menu.settings_menu);
      mToolbar.inflateMenu(R.menu.share_menu);
      mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          switch (item.getItemId()) {
            case R.id.main_menu_settings:
              mController.onSettingsMenuOptionClicked();
              return true;
            case R.id.action_share:
              mController.onShareClicked();
              return true;
            case R.id.main_menu_now_playing:
              mController.onNowPlayingMenuOptionClicked();
              return true;
            default:
          }
          return false;
        }
      });

    }

    mTopTracksRecyclerView.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL_LIST));
    mTopTracksRecyclerView.setLayoutManager(mController.getLayoutManager());
    mTopTracksRecyclerView.setHasFixedSize(true);

    mCollapsingToolbarLayout.setCollapsedTitleTextColor(getDefaultCollapsedTitleColor());
    mCollapsingToolbarLayout.setExpandedTitleColor(getDefaultExpandedTitleColor());

    mPlayAllButton.setEnabled(false);
  }

  @OnClick(R.id.fragment_top_tracks_play_all_floatingActionButton)
  void onPlayAllClicked(){
    mController.onPlayAllClicked();
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

  public View getNoContentView(){
    return mNoContentView;
  }

  public int getDefaultCollapsedTitleColor(){
    return getResources().getColor(android.R.color.black);
  }

  public int getDefaultExpandedTitleColor(){
    return getResources().getColor(android.R.color.white);
  }
}
