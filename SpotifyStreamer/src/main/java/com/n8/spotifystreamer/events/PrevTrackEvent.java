package com.n8.spotifystreamer.events;

/**
 * Indicates that the previous track is starting to play.
 */
public class PrevTrackEvent {

  private int mTrackIndex;

  public PrevTrackEvent(int trackIndex) {
    mTrackIndex = trackIndex;
  }

  public int getTrackIndex() {
    return mTrackIndex;
  }
}
