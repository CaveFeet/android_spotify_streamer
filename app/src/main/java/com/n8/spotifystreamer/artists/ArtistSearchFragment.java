package com.n8.spotifystreamer.artists;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.common.base.Strings;
import com.n8.spotifystreamer.AndroidUtils;
import com.n8.spotifystreamer.BusProvider;
import com.n8.spotifystreamer.R;
import com.n8.spotifystreamer.SpotifyStreamerApplication;
import com.n8.spotifystreamer.tracks.TopTracksActivityFragment;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
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

    @InjectView(R.id.fragment_artist_search_no_content_layout)
    View mNoContentView;

    @InjectView(R.id.fragment_artist_search_recyclerView)
    RecyclerView mArtistRecyclerView;

    @InjectView(R.id.fragment_top_tracks_appBarLayout)
    AppBarLayout mAppBarLayout;

    @InjectView(R.id.fragment_artist_search_toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.fragment_artis_search_searchView)
    SearchView mSearchView;

    private List<Artist> mArtists;

    public ArtistSearchFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        BusProvider.getInstance().register(this);

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

        mArtistRecyclerView.setLayoutManager(getLinearLayoutManager());
        mArtistRecyclerView.setHasFixedSize(true);
        mArtistRecyclerView.setVisibility(View.GONE);

        // If view is being recreated after a rotation, there may be existing artist data to view
        if (mArtists != null) {
            bindArtists();
        }

        return view;
    }

    @NonNull
    private LinearLayoutManager getLinearLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onArtistViewClicked(Artist artist, ImageView sharedImage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Fragment fragment = TopTracksActivityFragment.getInstance(artist);
            fragment.setSharedElementEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.artists_to_tracks_transition));
            fragment.setSharedElementReturnTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.artists_to_tracks_transition));

            // Add Fragment B
            FragmentTransaction ft = getFragmentManager().beginTransaction()
                .replace(R.id.main_activity_fragment_frame, fragment)
                .addToBackStack("transaction")
                .addSharedElement(sharedImage, "MyTransition");
            ft.commit();
        }
        else {
            // Code to run on older devices
        }
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
    }

    private void submitQuery(String query) {
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
            ArtistSuggestionProvider.AUTHORITY, ArtistSuggestionProvider.MODE);
        suggestions.saveRecentQuery(query, null);

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Searching for " + query);
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
    }

    private void handleQueryResponse(ArtistsPager artistsPager){
        mArtists = artistsPager.artists.items;
        bindArtists();
        mSearchView.clearFocus();
    }

    private void bindArtists() {
        mArtistRecyclerView.setAdapter(new ArtistsRecyclerAdapter(mArtists, this));
        mArtistRecyclerView.setVisibility(View.VISIBLE);
        mNoContentView.setVisibility(View.GONE);
    }

}
