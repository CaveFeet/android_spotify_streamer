package com.n8.spotifystreamer.events;

import android.support.annotation.NonNull;

import com.n8.spotifystreamer.models.ParcelableTrack;

/**
 * Indicates that a new track has been started.
 */
public class TrackStartedEvent {

  private ParcelableTrack mTrack;

  private int mDuration;

  public TrackStartedEvent(@NonNull ParcelableTrack track, int duration) {
    mTrack = track;
    mDuration = duration;
  }

  public String getTrackName() {
    return mTrack.name;
  }

  public String getAlbumName() {
    return mTrack.album.name;
  }

  public String getArtistName() {
    return mTrack.artists.get(0).name;
  }

  public String getThumbnailUrl() {
    return mTrack.album.images.get(0).url;
  }

  public int getDuration() {
    return mDuration;
  }
}
