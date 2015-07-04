package com.n8.spotifystreamer.playback;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaControllerCompat;
import android.util.Log;
import android.widget.MediaController;

import com.n8.spotifystreamer.BaseFragmentController;

import java.io.IOException;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

public class PlaybackFragmentController extends BaseFragmentController<PlaybackFragmentView> implements PlaybackController {

  private static final String TAG = PlaybackFragmentController.class.getSimpleName();

  private List<Track> mTracks;

  private Track mCurrentTrack;

  public PlaybackFragmentController(List<Track> tracks, Track track) {
    mTracks = tracks;
    mCurrentTrack = track;
  }

  @Override
  public void onCreateView(@NonNull PlaybackFragmentView view) {
    super.onCreateView(view);

    //mView.mPlayButton.setEnabled(false);

    //mView.mPauseButton.setEnabled(false);
  }

  @Override
  public void onPlayClicked() {
    Intent playbackIntent = new Intent(mView.getContext(), PlaybackService.class);
    playbackIntent.setAction(PlaybackService.ACTION_PLAY);
    mView.getContext().startService(playbackIntent);
  }

  @Override
  public void onPauseClicked() {
    Intent playbackIntent = new Intent(mView.getContext(), PlaybackService.class);
    playbackIntent.setAction(PlaybackService.ACTION_PAUSE);
    mView.getContext().startService(playbackIntent);
  }
}
