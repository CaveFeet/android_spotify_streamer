package com.n8.spotifystreamer.playback;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.n8.spotifystreamer.R;

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

public class PlaybackFragment extends DialogFragment {

  private static final String TAG = PlaybackFragment.class.getSimpleName();

  private PlaybackFragmentController mController;

  private List<Track> mTracks;

  private Track mTrack;

  public static PlaybackFragment getInstance(List<Track> tracks, Track track) {
    PlaybackFragment fragment = new PlaybackFragment();
    fragment.mTracks = tracks;
    fragment.mTrack = track;

    return fragment;
  }

  public PlaybackFragment() { }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    PlaybackFragmentView view = (PlaybackFragmentView) inflater.inflate(R.layout.fragment_playback, container, false);

    if (mController == null) {
      mController = new PlaybackFragmentController(mTracks, mTrack);
    }
    mController.setActivity((AppCompatActivity)getActivity());

    view.setController((AppCompatActivity)getActivity(), mController);
    mController.onCreateView(view);

    return view;
  }

  @Override
  public void onDetach() {
    super.onDetach();
    if (mController != null) {
      mController.onDetachView();
    }
  }

  @Override
  public void onDismiss(DialogInterface dialog) {
    super.onDismiss(dialog);
    if (mController != null) {
      mController.onDetachView();
    }
  }
}
