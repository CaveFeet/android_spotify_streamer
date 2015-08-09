package com.n8.spotifystreamer.events;

/**
 * Indicates that the playback progress of a track has been updated
 */
public class PlaybackProgressEvent {

  int mProgress;

  public PlaybackProgressEvent(int progress) {
    mProgress = progress;
  }

  public int getProgress() {
    return mProgress;
  }
}
