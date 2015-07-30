package com.n8.spotifystreamer.artists;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.n8.n8droid.AndroidUtils;
import com.n8.n8droid.BaseViewControllerFragment;
import com.n8.spotifystreamer.BusProvider;
import com.n8.spotifystreamer.R;
import com.n8.spotifystreamer.SettingsActivity;
import com.n8.spotifystreamer.SpotifyStreamerApplication;
import com.n8.spotifystreamer.events.ArtistClickedEvent;
import com.n8.spotifystreamer.events.CountryCodeSettingChangedEvent;
import com.n8.spotifystreamer.events.SearchIntentReceivedEvent;
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
public class ArtistSearchFragment extends BaseViewControllerFragment<ArtistSearchFragmentView> implements
    ArtistSearchController, ArtistsRecyclerAdapter.ArtistClickListener{

  private static final String TAG = ArtistSearchFragment.class.getSimpleName();

  private static final int REQUEST_LIMIT = 20;

  public static final String SUGGESTIONS_PREFERENCES_KEY = "suggestions_preferences_key";

  private List<Artist> mArtists;

  private ArtistsRecyclerAdapter mAdapter;

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
  public LinearLayoutManager getLinearLayoutManager() {
    return new LinearLayoutManager(getActivity());
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
    if (mAdapter.getItemCount() != mTotalCurrentSearchResults) {
      LinearLayoutManager manager = (LinearLayoutManager) mView.getArtistRecyclerView().getLayoutManager();
      if (manager.findLastVisibleItemPosition() == mAdapter.getItemCount() - 1 && !mPagingNewResults) {

        mView.getProgressBar().setVisibility(View.VISIBLE);
        final Handler handler = new Handler();

        mPagingNewResults = true;
        searchForArtists(null, handler);
      }
    }
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
    mCurrentQuery = query;
    mTotalCurrentSearchResults = 0;
    mCurrentQueryOffset = 0;
    mArtists = null;
    mAdapter = null;

    FragmentActivity activity = getActivity();

    // Leverage provider to populate suggestions adapter
    //
    SearchRecentSuggestions suggestions = new SearchRecentSuggestions(activity,
        ArtistSuggestionProvider.AUTHORITY, ArtistSuggestionProvider.MODE);
    suggestions.saveRecentQuery(query, null);

    final ProgressDialog progressDialog = new ProgressDialog(activity);
    progressDialog.setTitle(activity.getString(R.string.searching_for) + query);
    progressDialog.setCancelable(false);
    progressDialog.setCanceledOnTouchOutside(false);
    progressDialog.show();

    final Handler handler = new Handler();
    searchForArtists(progressDialog, handler);
  }

  private void searchForArtists(final ProgressDialog progressDialog,
                                final Handler handler) {

    Map<String, Object> queryMap = new HashMap<>();
    queryMap.put("offset", mCurrentQueryOffset);
    queryMap.put("limit", REQUEST_LIMIT);
    queryMap.put("country", mCountryCode);

    SpotifyStreamerApplication.getSpotifyService().searchArtists(mCurrentQuery, queryMap,
        new Callback<ArtistsPager>() {
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

                handleQueryResponse(artistsPager);
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
                AndroidUtils.showToast(activity, activity.getString(R.string.error_searching_for_artist));
              }
            });
          }
        });
  }

  private SharedPreferences getSharedPreferences() {
    return getActivity().getSharedPreferences(ArtistSearchFragment.class.getSimpleName(), Context.MODE_PRIVATE);
  }

  public void onArtistViewClicked(Artist artist, ImageView sharedImageView) {
    BusProvider.getInstance().post(new ArtistClickedEvent(artist, sharedImageView));
  }

  private void clearSearchSuggestions(){
    SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(), ArtistSuggestionProvider.AUTHORITY,
        ArtistSuggestionProvider.MODE);
    suggestions.clearHistory();

    SharedPreferences sharedPreferences = getSharedPreferences();
    sharedPreferences.edit().remove(SUGGESTIONS_PREFERENCES_KEY).apply();
  }

  private void handleQueryResponse(ArtistsPager artistsPager) {
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
      mAdapter = new ArtistsRecyclerAdapter(mArtists, this);
    }
    mView.getArtistRecyclerView().setAdapter(mAdapter);
    setupSwipeToDeleteHelper(mView.getArtistRecyclerView());
    mView.showContentView();
  }

  private void setupSwipeToDeleteHelper(ViewGroup view) {
    ArtistRecyclerViewTouchHelperCallback callback = new ArtistRecyclerViewTouchHelperCallback(
        view, mAdapter, mArtists);
    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
    itemTouchHelper.attachToRecyclerView(mView.getArtistRecyclerView());
  }
}
