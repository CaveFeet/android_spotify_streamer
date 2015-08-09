package com.n8.spotifystreamer.events;

/**
 * Posted when a search intent is received from the Activity
 */
public class SearchIntentReceivedEvent {
    private String mQuery;

    public SearchIntentReceivedEvent(String query) {
        mQuery = query;
    }

    public String getQuery() {
        return mQuery;
    }
}
