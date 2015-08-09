package com.n8.spotifystreamer.models;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.models.ArtistSimple;

public class ParcelableArtistSimple implements Parcelable {
  public Map<String, String> external_urls;
  public String href;
  public String id;
  public String name;
  public String type;
  public String uri;

  public ParcelableArtistSimple(@NonNull ArtistSimple artist) {
    external_urls = artist.external_urls;
    href = artist.href;
    id = artist.id;
    name = artist.name;
    type = artist.type;
    uri = artist.uri;
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
    dest.writeStringList(new ArrayList<>(external_urls.keySet()));
    Bundle urlsBundle = new Bundle();
    for (String key : external_urls.keySet()) {
      urlsBundle.putString(key, external_urls.get(key));
    }
    dest.writeBundle(urlsBundle);

    dest.writeString(href);
    dest.writeString(id);
    dest.writeString(name);
    dest.writeString(type);
    dest.writeString(uri);
  }

  public static final Parcelable.Creator<ParcelableArtistSimple> CREATOR = new Parcelable.Creator<ParcelableArtistSimple>() {
    public ParcelableArtistSimple createFromParcel(Parcel in) {
      return new ParcelableArtistSimple(in);
    }

    public ParcelableArtistSimple[] newArray(int size) {
      return new ParcelableArtistSimple[size];
    }
  };

  ParcelableArtistSimple(Parcel in) {
    List<String> bundleKeys = new ArrayList<>();
    in.readStringList(bundleKeys);

    external_urls = new HashMap<>();
    Bundle urlsBundle = in.readBundle();
    for (String key : bundleKeys) {
      external_urls.put(key, urlsBundle.getString(key));
    }

    href = in.readString();
    id = in.readString();
    name = in.readString();
    type = in.readString();
    uri = in.readString();
  }
}