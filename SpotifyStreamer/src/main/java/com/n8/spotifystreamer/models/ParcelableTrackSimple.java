/*
COPYRIGHT 1995-2015 ESRI

TRADE SECRETS: ESRI PROPRIETARY AND CONFIDENTIAL 
Unpublished material - all rights reserved under the Copyright Laws of the United States.

For additional information, contact: Environmental Systems Research Institute, Inc. 
Attn: Contracts Dept 380 New York Street Redlands, California, USA 92373

email: contracts@esri.com
*/
package com.n8.spotifystreamer.models;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.LinkedTrack;
import kaaes.spotify.webapi.android.models.Track;

public class ParcelableTrackSimple implements Parcelable{
  public List<ParcelableArtistSimple> artists;
  public List<String> available_markets;
  public boolean is_playable;
  public ParcelableLinkedTrack linked_from;
  public int disc_number;
  public long duration_ms;
  public boolean explicit;
  public Map<String, String> external_urls;
  public String href;
  public String id;
  public String name;
  public String preview_url;
  public int track_number;
  public String type;
  public String uri;

  public ParcelableTrackSimple(@NonNull Track track) {
    artists = new ArrayList<>();
    for (ArtistSimple artist : track.artists) {
      artists.add(new ParcelableArtistSimple(artist));
    }

    available_markets = track.available_markets;
    is_playable = track.is_playable != null && track.is_playable;
    linked_from = track.linked_from == null ? null : new ParcelableLinkedTrack(track.linked_from);

    disc_number = track.disc_number;
    duration_ms = track.duration_ms;
    explicit = track.explicit;
    external_urls = track.external_urls;
    href = track.href;
    id = track.id;
    name = track.name;
    preview_url = track.preview_url;
    track_number = track.track_number;
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
    dest.writeTypedList(artists);
    dest.writeStringList(available_markets);
    dest.writeByte((byte) (is_playable ? 1 : 0));
    dest.writeParcelable(linked_from, flags);
    dest.writeInt(disc_number);
    dest.writeLong(duration_ms);
    dest.writeByte((byte) (explicit ? 1 : 0));

    dest.writeStringList(new ArrayList<>(external_urls.keySet()));
    Bundle urlsBundle = new Bundle();
    for (String key : external_urls.keySet()) {
      urlsBundle.putString(key, external_urls.get(key));
    }
    dest.writeBundle(urlsBundle);

    dest.writeString(href);
    dest.writeString(id);
    dest.writeString(name);
    dest.writeString(preview_url);
    dest.writeInt(track_number);
    dest.writeString(type);
    dest.writeString(uri);
  }

  public static final Parcelable.Creator<ParcelableTrackSimple> CREATOR = new Parcelable.Creator<ParcelableTrackSimple>() {
    public ParcelableTrackSimple createFromParcel(Parcel in) {
      return new ParcelableTrackSimple(in);
    }

    public ParcelableTrackSimple[] newArray(int size) {
      return new ParcelableTrackSimple[size];
    }
  };

  ParcelableTrackSimple(Parcel in) {
    artists = new ArrayList<>();
    in.readTypedList(artists, ParcelableArtistSimple.CREATOR);

    available_markets = new ArrayList<>();
    in.readStringList(available_markets);

    is_playable = in.readByte() != 0;
    linked_from = in.readParcelable(ParcelableLinkedTrack.class.getClassLoader());
    disc_number = in.readInt();
    duration_ms = in.readLong();
    explicit = in.readByte() != 0;

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
    preview_url = in.readString();
    track_number = in.readInt();
    type = in.readString();
    uri = in.readString();
  }
}
