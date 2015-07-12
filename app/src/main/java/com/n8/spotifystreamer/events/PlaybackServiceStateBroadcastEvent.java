package com.n8.spotifystreamer.events;

import android.media.MediaPlayer;

public class PlaybackServiceStateBroadcastEvent {

  private MediaPlayer mMediaPlayer;

  public PlaybackServiceStateBroadcastEvent(MediaPlayer mediaPlayer) {
    mMediaPlayer = mediaPlayer;
  }

  public boolean isPlaying(){
    return mMediaPlayer != null && mMediaPlayer.isPlaying();
  }
}
