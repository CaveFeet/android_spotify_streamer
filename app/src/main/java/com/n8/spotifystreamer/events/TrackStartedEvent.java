package com.n8.spotifystreamer.events;

import android.support.annotation.NonNull;

public class TrackStartedEvent {

  private String mTrackName;
  private String mAlbumName;
  private String mArtistName;
  private String mThumbnailUrl;
  private int mDuration;

  public TrackStartedEvent(@NonNull String trackName, @NonNull String albumName, @NonNull String artistName, @NonNull String
      thumbnailUrl, int duration) {

    mTrackName = trackName;
    mAlbumName = albumName;
    mArtistName = artistName;
    mThumbnailUrl = thumbnailUrl;
    mDuration = duration;
  }

  public String getTrackName() {
    return mTrackName;
  }

  public String getAlbumName() {
    return mAlbumName;
  }

  public String getArtistName() {
    return mArtistName;
  }

  public String getThumbnailUrl() {
    return mThumbnailUrl;
  }

  public int getDuration() {
    return mDuration;
  }
}
