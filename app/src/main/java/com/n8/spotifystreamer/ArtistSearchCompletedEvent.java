package com.n8.spotifystreamer;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;

/**
 * Event to pass through Otto event bus indicating that an {@link Artist} search request
 * was completed.
 */
public class ArtistSearchCompletedEvent {

    private ArtistsPager mArtistPager;

    public ArtistSearchCompletedEvent(ArtistsPager artistsPager) {
        mArtistPager = artistsPager;
    }

    public ArtistsPager getArtistPager() {
        return mArtistPager;
    }
}
