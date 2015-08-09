package com.n8.spotifystreamer.artists;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.n8.n8droid.util.AndroidUtils;
import com.n8.n8droid.util.UiUtils;
import com.n8.n8droid.viewcontroller.BaseViewControllerFragment;
import com.n8.spotifystreamer.BusProvider;
import com.n8.spotifystreamer.DividerItemDecoration;
import com.n8.spotifystreamer.R;
import com.n8.spotifystreamer.SettingsActivity;
import com.n8.spotifystreamer.SpotifyStreamerApplication;
import com.n8.spotifystreamer.events.ArtistClickedEvent;
import com.n8.spotifystreamer.events.CountryCodeSettingChangedEvent;
import com.n8.spotifystreamer.events.SearchIntentReceivedEvent;
import com.n8.spotifystreamer.events.ShowPlaybackFragmentEvent;
import com.n8.spotifystreamer.models.ParcelableArtist;
import com.n8.spotifystreamer.models.ParcelableArtistPager;
import com.squareup.otto.Subscribe;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Fragment that allows user to serach for, and view, artists
 */
public class ArtistSearchFragment extends BaseViewControllerFragment<ArtistSearchFragmentView>
    implements
    ArtistSearchController, ArtistsRecyclerAdapter.ArtistClickListener{

  private static final String TAG = ArtistSearchFragment.class.getSimpleName();

  private static final int REQUEST_LIMIT = 20;

  public static final String SUGGESTIONS_PREFERENCES_KEY = "suggestions_preferences_key";
  public static final int GRID_SPAN_COUNT = 2;

  private List<ParcelableArtist> mArtists;

  private RecyclerView.Adapter mAdapter;

  private String mCurrentQuery;

  private int mCurrentQueryOffset = 0;

  private int mTotalCurrentSearchResults;

  private boolean mPagingNewResults;

  private String mCountryCode;

  public ArtistSearchFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
  }

  @Override
  protected int getLayoutId() {
    return R.layout.fragment_artist_search;
  }

  @Override
  protected void setViewController() {
    mView.setController(getActivity(), this);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    BusProvider.getInstance().register(this);

    mCountryCode = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getString(R.string
        .pref_country_code_key), Locale.getDefault().getCountry());

    // If view is being recreated after a rotation, there may be existing artist data to view
    if (mArtists != null && mArtists.size() > 0) {
      bindArtists(false);
    }

    return mView;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    BusProvider.getInstance().unregister(this);
  }

  @Override
  public void onNowPlayingMenuOptionClicked(){
    BusProvider.getInstance().post(new ShowPlaybackFragmentEvent());
  }

  @Override
  public void onSettingsMenuOptionClicked() {
    Intent intent = new Intent(getActivity(), SettingsActivity.class);
    startActivity(intent);
  }

  @Override
  public void onClearSuggestions() {
    clearSearchSuggestions();
  }

  @Override
  public void onSubmitQuery(String query) {
    submitQuery(query);
  }

  @Override
  public void onRecyclerViewScrolled(RecyclerView recyclerView, int dx, int dy) {
    if (mAdapter == null) {
      return;
    }

    if (mAdapter.getItemCount() != mTotalCurrentSearchResults) {
      RecyclerView.LayoutManager manager = mView.getArtistRecyclerView().getLayoutManager();
      if (manager instanceof LinearLayoutManager) {
        if (((LinearLayoutManager)manager).findLastVisibleItemPosition() == mAdapter.getItemCount() - 1 &&
            !mPagingNewResults) {
          showPagingProgress();
        }
      }else if (manager instanceof StaggeredGridLayoutManager) {
        int[] lastPositions = ((StaggeredGridLayoutManager) manager).findLastCompletelyVisibleItemPositions(null);

        for (Integer position : lastPositions) {
          if (position == mAdapter.getItemCount() - 1 && !mPagingNewResults) {
            showPagingProgress();
            break;
          }
        }
      }
    }
  }

  private void showPagingProgress() {
    mView.getProgressBar().setVisibility(View.VISIBLE);
    final Handler handler = new Handler();

    mPagingNewResults = true;
    searchForArtists(null, handler);
  }

  @Subscribe
  public void onSearchIntentReceived(SearchIntentReceivedEvent event) {
    submitQuery(event.getQuery());
  }

  @Subscribe
  public void onCountryCodeSettingChangeEventReceived(CountryCodeSettingChangedEvent event) {
    mCountryCode = event.getCountryCode();
    if (mCurrentQuery != null && mCurrentQuery.length() > 0) {
      submitQuery(mCurrentQuery);
    }
  }

  private void submitQuery(String query) {
    // Clear existing query state
    //
    mCurrentQuery = query;
    mTotalCurrentSearchResults = 0;
    mCurrentQueryOffset = 0;
    mArtists = null;
    mAdapter = null;

    // Leverage provider to populate suggestions adapter
    //
    SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
        ArtistSuggestionProvider.AUTHORITY, ArtistSuggestionProvider.MODE);
    suggestions.saveRecentQuery(query, null);

    final ProgressDialog progressDialog = createSearchProgressDialog(query);

    final Handler handler = new Handler();
    searchForArtists(progressDialog, handler);
  }

  @NonNull
  private ProgressDialog createSearchProgressDialog(String query) {
    final ProgressDialog progressDialog = new ProgressDialog(getActivity());
    progressDialog.setTitle(getActivity().getString(R.string.searching_for) + query);
    progressDialog.setCancelable(false);
    progressDialog.setCanceledOnTouchOutside(false);
    progressDialog.show();
    return progressDialog;
  }

  private void searchForArtists(final ProgressDialog progressDialog, final Handler handler) {

    Map<String, Object> queryMap = new HashMap<>();
    queryMap.put("offset", mCurrentQueryOffset);
    queryMap.put("limit", REQUEST_LIMIT);
    queryMap.put("country", mCountryCode);

    SpotifyStreamerApplication.getSpotifyService().searchArtists(mCurrentQuery, queryMap, new Callback<ArtistsPager>() {
          @Override
          public void success(final ArtistsPager artistsPager, Response response) {
            if (progressDialog != null) {
              progressDialog.cancel();
            }

            handler.post(new Runnable() {
              @Override
              public void run() {
                mCurrentQueryOffset += REQUEST_LIMIT;
                mTotalCurrentSearchResults = artistsPager.artists.total;
                if (mPagingNewResults) {
                  mPagingNewResults = false;
                }
                mView.getProgressBar().setVisibility(View.GONE);

                handleQueryResponse(new ParcelableArtistPager(artistsPager));
              }
            });
          }

          @Override
          public void failure(RetrofitError error) {
            handler.post(new Runnable() {
              @Override
              public void run() {
                if (progressDialog != null) {
                  progressDialog.cancel();
                }
                mView.getProgressBar().setVisibility(View.GONE);

                FragmentActivity activity = getActivity();
                UiUtils.showToast(activity, activity.getString(R.string.error_searching_for_artist));
              }
            });
          }
        });
  }

  private SharedPreferences getSharedPreferences() {
    return getActivity().getSharedPreferences(ArtistSearchFragment.class.getSimpleName(), Context.MODE_PRIVATE);
  }

  public void onArtistViewClicked(ParcelableArtist artist, ImageView sharedImageView) {
    BusProvider.getInstance().post(new ArtistClickedEvent(artist, sharedImageView));
  }

  private void clearSearchSuggestions(){
    SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(), ArtistSuggestionProvider.AUTHORITY,
        ArtistSuggestionProvider.MODE);
    suggestions.clearHistory();

    SharedPreferences sharedPreferences = getSharedPreferences();
    sharedPreferences.edit().remove(SUGGESTIONS_PREFERENCES_KEY).apply();
  }

  private void handleQueryResponse(ParcelableArtistPager artistsPager) {
    if (mArtists == null) {
      mArtists = artistsPager.artists.items;
    } else {
      int startIndex = mArtists.size();
      mArtists.addAll(artistsPager.artists.items);
      mAdapter.notifyItemRangeInserted(startIndex, startIndex + REQUEST_LIMIT);
      return;
    }
    bindArtists(true);
    mView.getSearchView().clearFocus();
  }

  private void bindArtists(boolean bindFromScratch) {
    if (mArtists == null || mArtists.size() == 0) {
      mView.showNoContentView();
      return;
    }

    if (bindFromScratch) {
      mAdapter = createRecyclerAdapter();
    }

    setupRecyclerView();
    setupSwipeToDeleteHelper(mView.getArtistRecyclerView());
    mView.showContentView();
  }

  private RecyclerView.Adapter createRecyclerAdapter() {
    return new ArtistsRecyclerAdapter(mArtists, this);
  }

  private RecyclerView.LayoutManager createLayoutManager() {
    return new GridLayoutManager(getActivity(), 2);
  }

  private void setupRecyclerView(){
    mAdapter = createRecyclerAdapter();

    RecyclerView recyclerView = mView.getArtistRecyclerView();
    recyclerView.setLayoutManager(createLayoutManager());

    recyclerView.setHasFixedSize(true);

    mView.getArtistRecyclerView().setAdapter(mAdapter);
  }

  private void setupSwipeToDeleteHelper(ViewGroup view) {
    ArtistRecyclerViewTouchHelperCallback callback =
        new ArtistRecyclerViewTouchHelperCallback(view, mAdapter, mArtists);
    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
    itemTouchHelper.attachToRecyclerView(mView.getArtistRecyclerView());
  }
}
