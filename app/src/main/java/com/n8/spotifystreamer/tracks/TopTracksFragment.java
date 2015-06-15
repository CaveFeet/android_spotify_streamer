package com.n8.spotifystreamer.tracks;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.n8.spotifystreamer.R;

import kaaes.spotify.webapi.android.models.Artist;

/**
 * Fragment that displays the top ten spotify tracks for an artist.
 */
public class TopTracksFragment extends Fragment {

  private static final String TAG = TopTracksFragment.class.getSimpleName();

  private TopTracksFragmentController mController;

  private Artist mArtist;

  public static TopTracksFragment getInstance(Artist artist) {
    TopTracksFragment fragment = new TopTracksFragment();
    fragment.mArtist = artist;

    return fragment;
  }

  public TopTracksFragment() { }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    TopTracksFragmentView view = (TopTracksFragmentView) inflater.inflate(R.layout.fragment_top_tracks, container, false);

    if (mController == null) {
      mController = new TopTracksFragmentController(mArtist);
    }
    mController.setActivity((AppCompatActivity)getActivity());

    view.setController((AppCompatActivity)getActivity(), mController);
    mController.onCreateView(view);

    return view;
  }
}
