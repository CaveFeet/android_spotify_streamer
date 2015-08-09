package com.n8.spotifystreamer.models;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.models.Track;

public class ParcelableTrack extends ParcelableTrackSimple implements Parcelable{
  public ParcelableAlbumSimple album;
  public Map<String, String> external_ids;
  public Integer popularity;

  public ParcelableTrack(@NonNull Track track) {
    super(track);
    album = new ParcelableAlbumSimple(track.album);
    external_ids = track.external_ids;
    popularity = track.popularity;
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
    super.writeToParcel(dest, flags);
    dest.writeParcelable(album, flags);

    dest.writeStringList(new ArrayList<>(external_urls.keySet()));
    Bundle urlsBundle = new Bundle();
    for (String key : external_urls.keySet()) {
      urlsBundle.putString(key, external_urls.get(key));
    }
    dest.writeBundle(urlsBundle);

    dest.writeInt(popularity);
  }

  public static final Parcelable.Creator<ParcelableTrack> CREATOR = new Parcelable.Creator<ParcelableTrack>() {
    public ParcelableTrack createFromParcel(Parcel in) {
      return new ParcelableTrack(in);
    }

    public ParcelableTrack[] newArray(int size) {
      return new ParcelableTrack[size];
    }
  };

  private ParcelableTrack(Parcel in) {
    super(in);
    album = in.readParcelable(ParcelableAlbumSimple.class.getClassLoader());

    List<String> bundleKeys = new ArrayList<>();
    in.readStringList(bundleKeys);

    external_urls = new HashMap<>();
    Bundle urlsBundle = in.readBundle();
    for (String key : bundleKeys) {
      external_urls.put(key, urlsBundle.getString(key));
    }

    popularity = in.readInt();
  }
}
