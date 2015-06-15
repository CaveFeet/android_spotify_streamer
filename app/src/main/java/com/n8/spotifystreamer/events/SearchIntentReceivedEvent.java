package com.n8.spotifystreamer.events;

public class SearchIntentReceivedEvent {
    private String mQuery;

    public SearchIntentReceivedEvent(String query) {
        mQuery = query;
    }

    public String getQuery() {
        return mQuery;
    }
}
