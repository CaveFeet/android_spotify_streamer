package com.n8.spotifystreamer.events;

import android.widget.ImageView;

import com.n8.spotifystreamer.models.ParcelableArtist;

/**
 * Event posted when the user clicks on an Artist search result
 */
public class ArtistClickedEvent {

    public ImageView mThumbnailView;

    public ParcelableArtist mArtist;

    public ArtistClickedEvent(ParcelableArtist artist, ImageView thumbnailView) {
        mArtist = artist;
        mThumbnailView = thumbnailView;
    }
}
