package com.n8.spotifystreamer.events;

/**
 * Indicates that the progress of the seekbar has been changed.
 */
public class SeekbarChangedEvent {

  private int mProgress;

  public SeekbarChangedEvent(int progress) {
    mProgress = progress;
  }

  public int getProgress() {
    return mProgress;
  }
}
