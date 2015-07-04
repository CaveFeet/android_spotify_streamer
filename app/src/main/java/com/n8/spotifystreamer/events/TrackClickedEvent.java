package com.n8.spotifystreamer.events;

import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Track;

public class TrackClickedEvent {

  private Artist mArtist;

  private List<Track> mTracks;

  private Track mClickedTrack;

  public TrackClickedEvent(Artist artist, List<Track> tracks, Track clickedTrack) {
    mArtist = artist;
    mTracks = tracks;
    mClickedTrack = clickedTrack;
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
}
