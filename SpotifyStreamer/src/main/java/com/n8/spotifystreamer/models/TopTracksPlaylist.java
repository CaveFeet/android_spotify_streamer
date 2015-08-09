package com.n8.spotifystreamer.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Represents a collection of an Artist's top songs that can be played as a group.
 */
public class TopTracksPlaylist implements Parcelable{

  private String mArtistName;

  private ParcelableTracks mTracks;

  public TopTracksPlaylist(@NonNull ParcelableArtist artist, @NonNull ParcelableTracks tracks) {
    if (artist == null || tracks == null) {
      throw new IllegalArgumentException("Argument to " + TopTracksPlaylist.class.getSimpleName() + " constructor was null");
    }

    mTracks = tracks;
    mArtistName = artist.name;
  }

  public ParcelableTracks getTracks() {
    return mTracks;
  }

  public int size(){
    return mTracks == null ? 0 : mTracks.tracks.size();
  }

  public String getArtistName() {
    return mArtistName;
  }

  public String getTrackName(@IntRange(from = 0) int index) {
    return mTracks.tracks.get(index).name;
  }

  public String getTrackPreviewUrl(@IntRange(from=0)int index) {
    return mTracks.tracks.get(index).preview_url;
  }

  public String getTrackThumbnailUrl(@IntRange(from = 0) int index, @IntRange(from = 0) int trackIndex) {
    ParcelableAlbumSimple album = getAlbum(trackIndex);

    int imageIndex = index;
    if (imageIndex >= album.images.size()) {
      imageIndex = album.images.size() - 1;
    }

    return album.images.get(imageIndex).url;
  }

  public String getTrackAlbumName(@IntRange(from = 0) int index) {
    return getAlbum(index).name;
  }

  private ParcelableAlbumSimple getAlbum(@IntRange(from = 0) int index) {
    return mTracks.tracks.get(index).album;
  }

  /*
   *  Private methods
   */

  private List<String> getTrackNamesFromTracksList(@NonNull List<Track> tracks) {
    List<String> trackNames = new ArrayList<>(tracks.size());
    for (Track track : tracks) {
      trackNames.add(track.name);
    }

    return trackNames;
  }

  private List<String> getTrackPreviewUrlsFromTracksList(@NonNull List<Track> tracks) {
    List<String> trackUrls = new ArrayList<>(tracks.size());
    for (Track track : tracks) {
      trackUrls.add(track.preview_url);
    }

    return trackUrls;
  }

  /**
   * Returns a list of the largest thumbnail url for each track album.
   *
   * @param tracks
   * @return
   */
  private List<String> getTrackThumbnailUrlsFromTracksList(@NonNull List<Track> tracks) {
    List<String> trackThumbnailUrls = new ArrayList<>(tracks.size());
    for (Track track : tracks) {
      List<Image> images = track.album.images;
      trackThumbnailUrls.add(images.get(0).url);
    }

    return trackThumbnailUrls;
  }

  private List<String> getTrackAlbumNamesFromTracksList(@NonNull List<Track> tracks) {
    List<String> trackAlbumNames = new ArrayList<>(tracks.size());
    for (Track track : tracks) {
      trackAlbumNames.add(track.album.name);
    }

    return trackAlbumNames;
  }

  /*
   * Implements Parcelable
   */

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(mArtistName);
    dest.writeParcelable(mTracks, flags);
  }

  public static final Parcelable.Creator<TopTracksPlaylist> CREATOR = new Parcelable.Creator<TopTracksPlaylist>() {
    public TopTracksPlaylist createFromParcel(Parcel in) {
      return new TopTracksPlaylist(in);
    }

    public TopTracksPlaylist[] newArray(int size) {
      return new TopTracksPlaylist[size];
    }
  };

  private TopTracksPlaylist(Parcel in) {
    mArtistName = in.readString();
    mTracks = in.readParcelable(ParcelableTracks.class.getClassLoader());
  }
}
