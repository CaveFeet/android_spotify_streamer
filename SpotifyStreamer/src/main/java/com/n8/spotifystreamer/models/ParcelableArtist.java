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
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by nate7313 on 8/5/15.
 */
public class ParcelableArtist extends ParcelableArtistSimple implements Parcelable {

  public ParcelableFollowers followers;
  public List<String> genres;
  public List<ParcelableImage> images;
  public Integer popularity;

  public ParcelableArtist(@NonNull Artist artist) {
    super(artist);
    followers = new ParcelableFollowers(artist.followers);
    genres = artist.genres;

    images = new ArrayList<>();
    for (Image image : artist.images) {
      images.add(new ParcelableImage(image));
    }

    popularity = artist.popularity;
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
    dest.writeParcelable(followers, flags);
    dest.writeStringList(genres);
    dest.writeTypedList(images);
    dest.writeInt(popularity);
  }

  public static final Parcelable.Creator<ParcelableArtist> CREATOR = new Parcelable.Creator<ParcelableArtist>() {
    public ParcelableArtist createFromParcel(Parcel in) {
      return new ParcelableArtist(in);
    }

    public ParcelableArtist[] newArray(int size) {
      return new ParcelableArtist[size];
    }
  };

  private ParcelableArtist(Parcel in) {
    super(in);
    followers = in.readParcelable(ParcelableFollowers.class.getClassLoader());

    genres = new ArrayList<>();
    in.readStringList(genres);

    images = new ArrayList<>();
    in.readTypedList(images, ParcelableImage.CREATOR);

    popularity = in.readInt();
  }
}
