package com.n8.spotifystreamer.models;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.models.LinkedTrack;

public class ParcelableLinkedTrack implements Parcelable{
  public Map<String, String> external_urls;
  public String href;
  public String id;
  public String type;
  public String uri;

  public ParcelableLinkedTrack(@NonNull LinkedTrack track) {
    external_urls = track.external_urls;
    href = track.href;
    id = track.id;
    type = track.type;
    uri = track.uri;
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
    dest.writeString(type);
    dest.writeString(uri);
  }

  public static final Parcelable.Creator<ParcelableLinkedTrack> CREATOR = new Parcelable.Creator<ParcelableLinkedTrack>() {
    public ParcelableLinkedTrack createFromParcel(Parcel in) {
      return new ParcelableLinkedTrack(in);
    }

    public ParcelableLinkedTrack[] newArray(int size) {
      return new ParcelableLinkedTrack[size];
    }
  };

  private ParcelableLinkedTrack(Parcel in) {
    List<String> bundleKeys = new ArrayList<>();
    in.readStringList(bundleKeys);

    external_urls = new HashMap<>();
    Bundle urlsBundle = in.readBundle();
    for (String key : bundleKeys) {
      external_urls.put(key, urlsBundle.getString(key));
    }

    href = in.readString();
    id = in.readString();
    type = in.readString();
    uri = in.readString();
  }
}
