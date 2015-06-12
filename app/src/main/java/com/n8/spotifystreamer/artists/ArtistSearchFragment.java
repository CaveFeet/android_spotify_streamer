package com.n8.spotifystreamer.artists;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.common.base.Strings;
import com.n8.spotifystreamer.AndroidUtils;
import com.n8.spotifystreamer.DividerItemDecoration;
import com.n8.spotifystreamer.R;
import com.n8.spotifystreamer.SpotifyStreamerApplication;
import com.n8.spotifystreamer.tracks.TopTracksActivityFragment;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Fragment that allows user to serach for, and view, artists
 */
public class ArtistSearchFragment extends Fragment implements ArtistsRecyclerAdapter.ArtistClickListener {

    private static final String TAG = ArtistSearchFragment.class.getSimpleName();

    private static final String TRACK_FRAGMENT_TAG = "track_fragment_tag";

    public static final String SUGGESTIONS_PREFERENCES_KEY = "suggestions_preferences_key";

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

    private List<Artist> mArtists;

    private ArtistsRecyclerAdapter mAdapter;

    public ArtistSearchFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_artist_search, container, false);
        ButterKnife.inject(this, view);

        mToolbar.inflateMenu(R.menu.fragment_artist_search_menu);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.fragment_artist_search_menu_clear_suggestions:
                        clearSearchSuggestions();
                        return true;
                    default:
                }
                return false;
            }
        });
        setupSearchView();

        mArtistRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        mArtistRecyclerView.setLayoutManager(getLinearLayoutManager());
        mArtistRecyclerView.setHasFixedSize(true);

        mInitialContentView.setVisibility(View.VISIBLE);

        // If view is being recreated after a rotation, there may be existing artist data to view
        if (mArtists != null && mArtists.size() > 0) {
            bindArtists();
        }

        return view;
    }

    @Override
    public void onArtistViewClicked(Artist artist, ImageView sharedImage) {
        // If using api 22 or better, use a shared element transition.  For some reason api 21
        // devices are displaying some odd behavior with the transition element.
        //
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Fragment fragment = TopTracksActivityFragment.getInstance(artist);
            fragment.setSharedElementEnterTransition(TransitionInflater.from(getActivity())
                .inflateTransition(R.transition.artists_to_tracks_transition));
            fragment.setSharedElementReturnTransition(TransitionInflater.from(getActivity())
                .inflateTransition(R.transition.artists_to_tracks_transition));

            // Add Fragment B
            FragmentTransaction ft = getFragmentManager().beginTransaction()
                .replace(R.id.main_activity_fragment_frame, fragment, TRACK_FRAGMENT_TAG)
                .addToBackStack(null)
                .addSharedElement(sharedImage, getString(R.string.artist_thumbnail_transition_name));
            ft.commit();

        }
        else {
            Fragment fragment = TopTracksActivityFragment.getInstance(artist);

            // Add Fragment B
            FragmentTransaction ft = getFragmentManager().beginTransaction()
                .replace(R.id.main_activity_fragment_frame, fragment, TRACK_FRAGMENT_TAG)
                .addToBackStack(null);
            ft.commit();
        }
    }

    @NonNull
    private LinearLayoutManager getLinearLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    private void setupSwipeToDeleteHelper(ViewGroup view) {
        ArtistRecyclerViewTouchHelperCallback callback = new ArtistRecyclerViewTouchHelperCallback(
            view, mAdapter, mArtists);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(mArtistRecyclerView);
    }

    private void setupSearchView() {
        mSearchView.setIconifiedByDefault(false);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(
            Context.SEARCH_SERVICE);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

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
                if (Strings.isNullOrEmpty(query)) {
                    AndroidUtils
                        .showToast(getActivity(), getString(R.string.error_searching_no_query));
                } else {
                    submitQuery(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Set<String> suggestions = getSharedPreferences().getStringSet(SUGGESTIONS_PREFERENCES_KEY, null);
                if (suggestions != null) {
                    submitQuery((String)suggestions.toArray()[position]);
                }
                return true;
            }
        });
    }

    private void submitQuery(String query) {
        // Leverage provider to populate suggestions adapter
        //
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
            ArtistSuggestionProvider.AUTHORITY, ArtistSuggestionProvider.MODE);
        suggestions.saveRecentQuery(query, null);

        // Use shared preferences to track suggestions so I can handle suggestion clicks without
        // going through activity.
        //
        SharedPreferences sharedPreferences = getSharedPreferences();
        Set<String> savedQueries = sharedPreferences.getStringSet(SUGGESTIONS_PREFERENCES_KEY, new HashSet<String>());
        savedQueries.add(query);
        sharedPreferences.edit().putStringSet(SUGGESTIONS_PREFERENCES_KEY, savedQueries).apply();

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(getString(R.string.searching_for) + query);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        final Handler handler = new Handler();
        SpotifyStreamerApplication.getSpotifyService().searchArtists(query,
            new Callback<ArtistsPager>() {
                @Override
                public void success(final ArtistsPager artistsPager, Response response) {
                    progressDialog.cancel();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            handleQueryResponse(artistsPager);
                        }
                    });
                }

                @Override
                public void failure(RetrofitError error) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();
                            AndroidUtils.showToast(getActivity(),
                                getString(R.string.error_searching_for_artist));
                        }
                    });
                }
            });
    }

    private void clearSearchSuggestions(){
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(), ArtistSuggestionProvider.AUTHORITY,
            ArtistSuggestionProvider.MODE);
        suggestions.clearHistory();

        SharedPreferences sharedPreferences = getSharedPreferences();
        sharedPreferences.edit().remove(SUGGESTIONS_PREFERENCES_KEY).apply();
    }

    private SharedPreferences getSharedPreferences() {
        return getActivity()
            .getSharedPreferences(ArtistSearchFragment.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    private void handleQueryResponse(ArtistsPager artistsPager){
        mArtists = artistsPager.artists.items;
        bindArtists();
        mSearchView.clearFocus();
    }

    private void bindArtists() {
        if (mArtists == null || mArtists.size() == 0) {
            mInitialContentView.setVisibility(View.GONE);
            mArtistRecyclerView.setVisibility(View.GONE);
            mNoContentView.setVisibility(View.VISIBLE);
            return;
        }

        mAdapter = new ArtistsRecyclerAdapter(mArtists, this);
        mArtistRecyclerView.setAdapter(mAdapter);
        setupSwipeToDeleteHelper(mArtistRecyclerView);
        mArtistRecyclerView.setVisibility(View.VISIBLE);
        mNoContentView.setVisibility(View.GONE);
        mInitialContentView.setVisibility(View.GONE);
    }

}
