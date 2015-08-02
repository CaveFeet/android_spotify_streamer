package com.n8.spotifystreamer.events;

/**
 * Indicates that the next track is starting to play.
 */
public class NextTrackEvent {

  private int mTrackIndex;

  public NextTrackEvent(int trackIndex) {
    mTrackIndex = trackIndex;
  }

  public int getTrackIndex() {
    return mTrackIndex;
  }
}
