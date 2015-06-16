package com.n8.spotifystreamer.events;

import android.widget.ImageView;

import kaaes.spotify.webapi.android.models.Artist;

/**
 * Event posted when the user clicks on an Artist search result
 */
public class ArtistClickedEvent {

    public ImageView mThumbnailView;

    public Artist mArtist;

    public ArtistClickedEvent(Artist artist, ImageView thumbnailView) {
        mArtist = artist;
        mThumbnailView = thumbnailView;
    }
}
