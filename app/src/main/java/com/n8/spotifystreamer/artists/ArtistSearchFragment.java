package com.n8.spotifystreamer.artists;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.n8.spotifystreamer.R;

/**
 * Fragment that allows user to serach for, and view, artists
 */
public class ArtistSearchFragment extends Fragment {

  private static final String TAG = ArtistSearchFragment.class.getSimpleName();

  private ArtistSearchFragmentController mController;

  public ArtistSearchFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    ArtistSearchFragmentView view = (ArtistSearchFragmentView) inflater.inflate(R.layout.fragment_artist_search, container, false);

    if (mController == null) {
      mController = new ArtistSearchFragmentController(getActivity());
    }

    view.setController(getActivity(), mController);
    mController.onCreateView(view);

    return view;
  }
}
