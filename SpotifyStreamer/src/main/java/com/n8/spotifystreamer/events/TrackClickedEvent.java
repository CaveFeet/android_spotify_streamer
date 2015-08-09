package com.n8.spotifystreamer.events;

import com.n8.spotifystreamer.models.ParcelableArtist;
import com.n8.spotifystreamer.models.ParcelableTrack;
import com.n8.spotifystreamer.models.ParcelableTracks;

/**
 * Indicates that a track has been clicked.
 */
public class TrackClickedEvent {

  private ParcelableArtist mArtist;

  private ParcelableTracks mTracks;

  private ParcelableTrack mClickedTrack;

  private boolean mPlayInDialog;

  public TrackClickedEvent(ParcelableArtist artist, ParcelableTracks tracks, ParcelableTrack clickedTrack, boolean playInDialog) {
    mArtist = artist;
    mTracks = tracks;
    mClickedTrack = clickedTrack;
    mPlayInDialog = playInDialog;
  }

  public ParcelableArtist getArtist() {
    return mArtist;
  }

  public ParcelableTracks getTracks() {
    return mTracks;
  }

  public ParcelableTrack getClickedTrack() {
    return mClickedTrack;
  }

  public boolean isPlayInDialog() {
    return mPlayInDialog;
  }
}
