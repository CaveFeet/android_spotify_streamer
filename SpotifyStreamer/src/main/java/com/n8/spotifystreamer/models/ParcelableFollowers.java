package com.n8.spotifystreamer.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import kaaes.spotify.webapi.android.models.Followers;

public class ParcelableFollowers implements Parcelable{
  public String href;

  public int total;

  public ParcelableFollowers(@NonNull Followers followers) {
    href = followers.href;
    total = followers.total;
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
    dest.writeString(href);
    dest.writeInt(total);
  }

  public static final Parcelable.Creator<ParcelableFollowers> CREATOR = new Parcelable.Creator<ParcelableFollowers>() {
    public ParcelableFollowers createFromParcel(Parcel in) {
      return new ParcelableFollowers(in);
    }

    public ParcelableFollowers[] newArray(int size) {
      return new ParcelableFollowers[size];
    }
  };

  private ParcelableFollowers(Parcel in) {
    href = in.readString();
    total = in.readInt();
  }
}
