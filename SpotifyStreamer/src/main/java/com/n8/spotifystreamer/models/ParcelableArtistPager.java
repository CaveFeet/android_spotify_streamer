/*
COPYRIGHT 1995-2015 ESRI

TRADE SECRETS: ESRI PROPRIETARY AND CONFIDENTIAL 
Unpublished material - all rights reserved under the Copyright Laws of the United States.

For additional information, contact: Environmental Systems Research Institute, Inc. 
Attn: Contracts Dept 380 New York Street Redlands, California, USA 92373

email: contracts@esri.com
*/
package com.n8.spotifystreamer.models;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Pager;

public class ParcelableArtistPager {
  public Pager<ParcelableArtist> artists;

  public ParcelableArtistPager(@NonNull ArtistsPager pager) {
    artists = new Pager<>();
    artists.href = pager.artists.href;
    artists.limit = pager.artists.limit;
    artists.next = pager.artists.next;
    artists.offset = pager.artists.offset;
    artists.previous = pager.artists.previous;
    artists.total = pager.artists.total;

    artists.items = new ArrayList<>();
    for (Artist artist : pager.artists.items) {
      artists.items.add(new ParcelableArtist(artist));
    }
  }
}
