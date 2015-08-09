/*
COPYRIGHT 1995-2015 ESRI

TRADE SECRETS: ESRI PROPRIETARY AND CONFIDENTIAL 
Unpublished material - all rights reserved under the Copyright Laws of the United States.

For additional information, contact: Environmental Systems Research Institute, Inc. 
Attn: Contracts Dept 380 New York Street Redlands, California, USA 92373

email: contracts@esri.com
*/
package com.n8.spotifystreamer.artists;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;

import com.n8.n8droid.BaseFragmentView;
import com.n8.spotifystreamer.DividerItemDecoration;
import com.n8.spotifystreamer.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Custom view to encapsulate view setup/logic for {@link com.n8.spotifystreamer.tracks.TopTracksFragment}.
 */
public class ArtistSearchFragmentView extends BaseFragmentView<ArtistSearchController> {

  @InjectView(R.id.fragment_artist_search_initial_content_layout)
  View mInitialContentView;

  @InjectView(R.id.fragment_artist_search_no_content_layout)
  View mNoContentView;

  @InjectView(R.id.fragment_artist_search_recyclerView)
  RecyclerView mArtistRecyclerView;

  @InjectView(R.id.fragment_artist_search_toolbar)
  Toolbar mToolbar;

  @InjectView(R.id.fragment_artis_search_searchView)
  SearchView mSearchView;

  @InjectView(R.id.fragment_artist_search_progressBar)
  ProgressBar mProgressBar;

  public ArtistSearchFragmentView(Context context) {
    super(context);
  }

  public ArtistSearchFragmentView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ArtistSearchFragmentView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public ArtistSearchFragmentView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override
  protected void setupView() {
    ButterKnife.inject(this);
    setuptToolbar();
    setupSearchView();
    setupRecyclerView();
  }

  public ProgressBar getProgressBar() {
    return mProgressBar;
  }

  public SearchView getSearchView() {
    return mSearchView;
  }

  public Toolbar getToolbar() {
    return mToolbar;
  }

  public RecyclerView getArtistRecyclerView() {
    return mArtistRecyclerView;
  }

  public void showNoContentView(){
    mInitialContentView.setVisibility(View.GONE);
    mArtistRecyclerView.setVisibility(View.GONE);
    mNoContentView.setVisibility(View.VISIBLE);
  }

  public void showContentView(){
    mArtistRecyclerView.setVisibility(View.VISIBLE);
    mNoContentView.setVisibility(View.GONE);
    mInitialContentView.setVisibility(View.GONE);
  }

  private void setuptToolbar() {
    mToolbar.inflateMenu(R.menu.fragment_artist_search_menu);
    mToolbar.inflateMenu(R.menu.settings_menu);
    mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
          case R.id.fragment_artist_search_menu_clear_suggestions:
            mController.onClearSuggestions();
            return true;
          case R.id.main_menu_settings:
            mController.onSettingsMenuOptionClicked();
            return true;
          default:
        }
        return false;
      }
    });
  }

  private void setupSearchView() {
    mSearchView.setIconifiedByDefault(false);

    SearchManager searchManager = (SearchManager) getContext().getSystemService(
        Context.SEARCH_SERVICE);
    mSearchView.setSearchableInfo(searchManager.getSearchableInfo(mActivity.getComponentName()));

    mSearchView.setImeOptions(mSearchView.getImeOptions() | EditorInfo.IME_ACTION_SEARCH | EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN);

    // Get the SearchView close button so I can set a custom click listener
    View closeButton = mSearchView.findViewById(R.id.search_close_btn);

    // Set own click listener because calling setOnCloseListener on the SearchView doesn't work
    closeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mSearchView.setQuery("", false);
        mSearchView.clearFocus();
      }
    });

    mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        mController.onSubmitQuery(query);
        return true;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        return false;
      }
    });
  }

  private void setupRecyclerView() {
    // Set a default layout manager
    mArtistRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

    mArtistRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        mController.onRecyclerViewScrolled(recyclerView, dx, dy);
      }
    });
  }
}
