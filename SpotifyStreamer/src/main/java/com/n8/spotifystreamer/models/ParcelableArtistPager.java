package com.n8.spotifystreamer.models;

import android.support.annotation.NonNull;

import java.util.ArrayList;

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
