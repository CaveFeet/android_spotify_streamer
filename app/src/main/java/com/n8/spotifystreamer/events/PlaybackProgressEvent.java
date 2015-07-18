package com.n8.spotifystreamer.events;

public class PlaybackProgressEvent {

  int mProgress;

  public PlaybackProgressEvent(int progress) {
    mProgress = progress;
  }

  public int getProgress() {
    return mProgress;
  }
}
