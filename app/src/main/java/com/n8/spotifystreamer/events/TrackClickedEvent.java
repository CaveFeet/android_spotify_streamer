package com.n8.spotifystreamer.events;

import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Track;

public class TrackClickedEvent {

  private Artist mArtist;

  private List<Track> mTracks;

  private Track mClickedTrack;
  private boolean mPlayInDialog;

  public TrackClickedEvent(Artist artist, List<Track> tracks, Track clickedTrack, boolean playInDialog) {
    mArtist = artist;
    mTracks = tracks;
    mClickedTrack = clickedTrack;
    mPlayInDialog = playInDialog;
  }

  public Artist getArtist() {
    return mArtist;
  }

  public List<Track> getTracks() {
    return mTracks;
  }

  public Track getClickedTrack() {
    return mClickedTrack;
  }

  public boolean isPlayInDialog() {
    return mPlayInDialog;
  }
}
