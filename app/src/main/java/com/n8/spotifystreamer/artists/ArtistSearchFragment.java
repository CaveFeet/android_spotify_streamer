package com.n8.spotifystreamer.artists;

import android.app.SearchManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.n8.spotifystreamer.BusProvider;
import com.n8.spotifystreamer.R;
import com.n8.spotifystreamer.tracks.TopTracksActivityFragment;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.models.Artist;

/**
 * Fragment that allows user to serach for, and view, artists
 */
public class ArtistSearchFragment extends Fragment implements ArtistsRecyclerAdapter.ArtistClickListener {

    private static final String TAG = ArtistSearchFragment.class.getSimpleName();

    @InjectView(R.id.fragment_artist_search_no_content_layout)
    View mNoContentView;

    @InjectView(R.id.fragment_artist_search_recyclerView)
    RecyclerView mArtistRecyclerView;

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

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(getActivity().SEARCH_SERVICE);

        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        mSearchView.setIconifiedByDefault(false);

        mArtistRecyclerView.setLayoutManager(getLinearLayoutManager());
        mArtistRecyclerView.setHasFixedSize(true);
        mArtistRecyclerView.setVisibility(View.GONE);

        // If view is being recreated after a rotation, there may be existing artist data to view
        if (mArtists != null) {
            bindArtists();
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSearchView.clearFocus();
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

    @Subscribe
    public void onArtistSearchCompleted(ArtistSearchCompletedEvent event) {
        mArtists = event.getArtistPager().artists.items;
        bindArtists();
        mSearchView.clearFocus();
    }

    private void bindArtists() {
        mArtistRecyclerView.setAdapter(new ArtistsRecyclerAdapter(mArtists, this));
        mArtistRecyclerView.setVisibility(View.VISIBLE);
        mNoContentView.setVisibility(View.GONE);
    }

}
