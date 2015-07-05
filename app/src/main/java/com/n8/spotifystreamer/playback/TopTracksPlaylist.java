package com.n8.spotifystreamer.playback;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

public class TopTracksPlaylist implements Parcelable{

  private String mArtistName;

  private List<String> mTrackNames;

  private List<String> mTrackUrls;

  private List<String> mTrackImageUrls;

  private List<String> mAlbumNames;

  public TopTracksPlaylist(@NonNull Artist artist, @NonNull List<Track> tracks) {
    if (artist == null || tracks == null) {
      throw new IllegalArgumentException("Argument to " + TopTracksPlaylist.class.getSimpleName() + " constructor was null");
    }

    mArtistName = artist.name;
    mTrackNames = getTrackNamesFromTracksList(tracks);
    mTrackUrls = getTrackPreviewUrlsFromTracksList(tracks);
    mTrackImageUrls = getTrackThumbnailUrlsFromTracksList(tracks);
    mAlbumNames = getTrackAlbumNamesFromTracksList(tracks);
  }

  public String getArtistName() {
    return mArtistName;
  }

  public List<String> getTrackUrls() {
    return mTrackUrls;
  }

  public List<String> getTrackImageUrls() {
    return mTrackImageUrls;
  }

  public String getTrackName(@IntRange(from = 0) int index) {
    return mTrackNames.get(index);
  }

  public String getTrackPreviewUrl(@IntRange(from=0)int index) {
    return mTrackUrls.get(index);
  }

  public String getTrackThumbnailUrl(@IntRange(from = 0) int index) {
    return mTrackImageUrls.get(index);
  }

  public String getTrackAlbumName(@IntRange(from = 0) int index) {
    return mAlbumNames.get(index);
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
      trackThumbnailUrls.add(images.get(images.size() - 1).url);
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
    dest.writeStringList(mTrackNames);
    dest.writeStringList(mTrackUrls);
    dest.writeStringList(mTrackImageUrls);
    dest.writeStringList(mAlbumNames);
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

    mTrackNames = new ArrayList<>();
    in.readStringList(mTrackNames);

    mTrackUrls = new ArrayList<>();
    in.readStringList(mTrackUrls);

    mTrackImageUrls = new ArrayList<>();
    in.readStringList(mTrackImageUrls);

    mAlbumNames = new ArrayList<>();
    in.readStringList(mAlbumNames);
  }
}
