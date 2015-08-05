package com.n8.spotifystreamer.playback;

import android.app.Dialog;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.n8.spotifystreamer.R;

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlaybackDialogFragment extends DialogFragment {

  public static final String TAG_PLAYBACK_FRAGMENT = "playback_fragment";
  private static List<Track> mTracks;

  private static Track mTrack;

  private PlaybackFragment mPlaybackFragment;

  public static PlaybackDialogFragment getInstance(List<Track> tracks, Track track) {
    PlaybackDialogFragment fragment = new PlaybackDialogFragment();
    fragment.setPlaybackInfo(tracks, track);

    return fragment;
  }

  public PlaybackDialogFragment() { }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Dialog dialog = super.onCreateDialog(savedInstanceState);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    return dialog;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_playback_dialog, container, false);

    if (getChildFragmentManager().findFragmentByTag(TAG_PLAYBACK_FRAGMENT) == null) {
      mPlaybackFragment = PlaybackFragment.getInstance(mTracks, mTrack);
      mPlaybackFragment.setIsRetained(false);
      mPlaybackFragment.setExpandable(false);
      getChildFragmentManager().beginTransaction().add(R.id.fragment_playback_dialog_frameLayout, mPlaybackFragment,
          TAG_PLAYBACK_FRAGMENT).commit();
    }

    return view;
  }

  public void setPlaybackInfo(@NonNull List<Track> tracks, @NonNull Track track) {
    mTracks = tracks;
    mTrack = track;

    if (mPlaybackFragment != null) {
      mPlaybackFragment.setPlaybackInfo(mTracks, mTrack);
    }
  }


}
