package com.n8.spotifystreamer.models;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Image;

public class ParcelableAlbumSimple implements Parcelable {
  public String album_type;
  public List<String> available_markets;
  public Map<String, String> external_urls;
  public String href;
  public String id;
  public List<ParcelableImage> images;
  public String name;
  public String type;
  public String uri;

  public ParcelableAlbumSimple(@NonNull AlbumSimple albumSimple) {
    album_type = albumSimple.album_type;
    available_markets = albumSimple.available_markets;
    external_urls = albumSimple.external_urls;
    href = albumSimple.href;
    id = albumSimple.id;

    images = new ArrayList<>();
    for (Image image : albumSimple.images) {
      images.add(new ParcelableImage(image));
    }

    name = albumSimple.name;
    type = albumSimple.type;
    uri = albumSimple.uri;
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
    dest.writeString(album_type);
    dest.writeStringList(available_markets);

    dest.writeStringList(new ArrayList<>(external_urls.keySet()));
    Bundle urlsBundle = new Bundle();
    for (String key : external_urls.keySet()) {
      urlsBundle.putString(key, external_urls.get(key));
    }
    dest.writeBundle(urlsBundle);

    dest.writeString(href);
    dest.writeString(id);

    dest.writeTypedList(images);

    dest.writeString(name);
    dest.writeString(type);
    dest.writeString(uri);
  }

  public static final Parcelable.Creator<ParcelableAlbumSimple> CREATOR = new Parcelable.Creator<ParcelableAlbumSimple>() {
    public ParcelableAlbumSimple createFromParcel(Parcel in) {
      return new ParcelableAlbumSimple(in);
    }

    public ParcelableAlbumSimple[] newArray(int size) {
      return new ParcelableAlbumSimple[size];
    }
  };

  private ParcelableAlbumSimple(Parcel in) {
    album_type = in.readString();

    available_markets = new ArrayList<>();
    in.readStringList(available_markets);

    List<String> bundleKeys = new ArrayList<>();
    in.readStringList(bundleKeys);

    external_urls = new HashMap<>();
    Bundle urlsBundle = in.readBundle();
    for (String key : bundleKeys) {
      external_urls.put(key, urlsBundle.getString(key));
    }

    href = in.readString();
    id = in.readString();

    images = new ArrayList<>();
    in.readTypedList(images, ParcelableImage.CREATOR);

    name = in.readString();
    type = in.readString();
    uri = in.readString();
  }
}