package com.n8.spotifystreamer.events;

import com.n8.spotifystreamer.models.ParcelableArtist;
import com.n8.spotifystreamer.models.ParcelableTrack;
import com.n8.spotifystreamer.models.ParcelableTracks;

public class PlayAllEvent {
  private ParcelableArtist mArtist;

  private ParcelableTracks mTracks;

  private boolean mPlayInDialog;

  public PlayAllEvent(ParcelableArtist artist, ParcelableTracks tracks, boolean playInDialog) {
    mArtist = artist;
    mTracks = tracks;
    mPlayInDialog = playInDialog;
  }

  public ParcelableArtist getArtist() {
    return mArtist;
  }

  public ParcelableTracks getTracks() {
    return mTracks;
  }

  public boolean isPlayInDialog() {
    return mPlayInDialog;
  }
}
