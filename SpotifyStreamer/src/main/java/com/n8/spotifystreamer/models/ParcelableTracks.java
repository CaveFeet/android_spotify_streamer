/*
COPYRIGHT 1995-2015 ESRI

TRADE SECRETS: ESRI PROPRIETARY AND CONFIDENTIAL 
Unpublished material - all rights reserved under the Copyright Laws of the United States.

For additional information, contact: Environmental Systems Research Institute, Inc. 
Attn: Contracts Dept 380 New York Street Redlands, California, USA 92373

email: contracts@esri.com
*/
package com.n8.spotifystreamer.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

public class ParcelableTracks implements Parcelable{
  public List<ParcelableTrack> tracks;

  public ParcelableTracks(Tracks ts) {
    tracks = new ArrayList<>();

    for (Track track : ts.tracks) {
      tracks.add(new ParcelableTrack(track));
    }
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
    dest.writeTypedList(tracks);
  }

  public static final Parcelable.Creator<ParcelableTracks> CREATOR = new Parcelable.Creator<ParcelableTracks>() {
    public ParcelableTracks createFromParcel(Parcel in) {
      return new ParcelableTracks(in);
    }

    public ParcelableTracks[] newArray(int size) {
      return new ParcelableTracks[size];
    }
  };

  private ParcelableTracks(Parcel in) {
    tracks = new ArrayList<>();
    in.readTypedList(tracks, ParcelableTrack.CREATOR);
  }
}
