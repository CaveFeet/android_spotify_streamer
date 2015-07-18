package com.n8.spotifystreamer.events;

public class SeekbarChangedEvent {

  private int mProgress;

  public SeekbarChangedEvent(int progress) {
    mProgress = progress;
  }

  public int getProgress() {
    return mProgress;
  }
}
