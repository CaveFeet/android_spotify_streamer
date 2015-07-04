package com.n8.spotifystreamer.playback;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.util.Log;

import com.n8.spotifystreamer.BaseFragmentController;

import java.io.IOException;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

public class PlaybackFragmentController extends BaseFragmentController<PlaybackFragmentView> implements PlaybackController {

  private static final String TAG = PlaybackFragmentController.class.getSimpleName();

  private List<Track> mTracks;

  private Track mCurrentTrack;
  private MediaPlayer mMediaPlayer;

  public PlaybackFragmentController(List<Track> tracks, Track track) {
    mTracks = tracks;
    mCurrentTrack = track;
  }

  @Override
  public void onCreateView(@NonNull PlaybackFragmentView view) {
    super.onCreateView(view);

    mView.mPlayButton.setEnabled(false);

    mMediaPlayer = new MediaPlayer();
    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    try {
      mMediaPlayer.setDataSource(mCurrentTrack.preview_url);
      mMediaPlayer.prepareAsync();
      mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {

        }
      });
      mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
          mView.mPlayButton.setEnabled(true);mMediaPlayer.set
        }
      });
    } catch (IOException e) {
      Log.d(TAG, "Failed to prepare media playter " + e.getMessage());
    }
  }

  @Override
  public void onDetachView() {
    super.onDetachView();

    mMediaPlayer.stop();
  }

  @Override
  public void onPlayClicked() {
    mMediaPlayer.start();
  }

  @Override
  public void onPauseClicked() {
    mMediaPlayer.pause();
  }
}
