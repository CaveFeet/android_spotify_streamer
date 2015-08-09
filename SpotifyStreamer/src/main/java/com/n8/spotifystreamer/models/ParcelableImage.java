package com.n8.spotifystreamer.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import kaaes.spotify.webapi.android.models.Image;

public class ParcelableImage implements Parcelable{
  public Integer width;
  public Integer height;
  public String url;

  public ParcelableImage(@NonNull Image image) {
    width = image.width;
    height = image.height;
    url = image.url;
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
    dest.writeInt(width);
    dest.writeInt(height);
    dest.writeString(url);
  }

  public static final Parcelable.Creator<ParcelableImage> CREATOR = new Parcelable.Creator<ParcelableImage>() {
    public ParcelableImage createFromParcel(Parcel in) {
      return new ParcelableImage(in);
    }

    public ParcelableImage[] newArray(int size) {
      return new ParcelableImage[size];
    }
  };

  private ParcelableImage(Parcel in) {
    width = in.readInt();
    height = in.readInt();
    url = in.readString();
  }
}
