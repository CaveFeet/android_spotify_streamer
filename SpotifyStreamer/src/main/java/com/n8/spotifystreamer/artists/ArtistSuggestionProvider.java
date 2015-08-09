package com.n8.spotifystreamer.artists;

import android.content.SearchRecentSuggestionsProvider;

public class ArtistSuggestionProvider extends SearchRecentSuggestionsProvider {
  public final static String AUTHORITY = "com.n8.ArtistSuggestionProvider";
  public final static int MODE = DATABASE_MODE_QUERIES;

  public ArtistSuggestionProvider() {
    setupSuggestions(AUTHORITY, MODE);
  }
}
