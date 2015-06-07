package com.n8.spotifystreamer;

import android.app.Activity;
import android.app.SearchManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.models.Artist;

/**
 * Fragment that allows user to serach for, and view, artists
 */
public class ArtistSearchFragment extends Fragment {

    public interface OnFragmentInteractionListener {
        void onArtistSelected();
    }

    private static final String TAG = ArtistSearchFragment.class.getSimpleName();

    @InjectView(R.id.fragment_artist_search_recyclerView)
    RecyclerView mArtistRecyclerView;

    @InjectView(R.id.fragment_artis_search_searchView)
    SearchView mSearchView;

    private OnFragmentInteractionListener mListener;

    private List<Artist> mArtists;

    public ArtistSearchFragment() { }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

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

        // If view is being recreated after a rotation, there may be existing artist data to view
        if (mArtists != null) {
            bindArtists();
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void onArtistSearchCompleted(ArtistSearchCompletedEvent event) {
        mArtists = event.getArtistPager().artists.items;
        bindArtists();
    }

    private void bindArtists() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mArtistRecyclerView.setLayoutManager(layoutManager);
        mArtistRecyclerView.setAdapter(new ArtistsRecyclerAdapter(mArtists));
    }

}
