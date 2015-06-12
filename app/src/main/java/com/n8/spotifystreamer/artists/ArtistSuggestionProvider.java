/*
COPYRIGHT 1995-2015 ESRI

TRADE SECRETS: ESRI PROPRIETARY AND CONFIDENTIAL 
Unpublished material - all rights reserved under the Copyright Laws of the United States.

For additional information, contact: Environmental Systems Research Institute, Inc. 
Attn: Contracts Dept 380 New York Street Redlands, California, USA 92373

email: contracts@esri.com
*/
package com.n8.spotifystreamer.artists;

import android.content.SearchRecentSuggestionsProvider;

public class ArtistSuggestionProvider extends SearchRecentSuggestionsProvider {
  public final static String AUTHORITY = "com.n8.ArtistSuggestionProvider";
  public final static int MODE = DATABASE_MODE_QUERIES;

  public ArtistSuggestionProvider() {
    setupSuggestions(AUTHORITY, MODE);
  }
}
