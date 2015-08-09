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

/**
 * A simple {@link Fragment} subclass.
 */
public class PlaybackDialogFragment extends DialogFragment {

  public static final String TAG_PLAYBACK_FRAGMENT = "playback_fragment";

  private PlaybackFragment mPlaybackFragment;

  public static PlaybackDialogFragment getInstance() {
    PlaybackDialogFragment fragment = new PlaybackDialogFragment();

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
      mPlaybackFragment = PlaybackFragment.getInstance();
      mPlaybackFragment.setIsRetained(false);
      mPlaybackFragment.setExpandable(false);
      getChildFragmentManager().beginTransaction().add(R.id.fragment_playback_dialog_frameLayout, mPlaybackFragment,
          TAG_PLAYBACK_FRAGMENT).commit();
    }

    return view;
  }


}
