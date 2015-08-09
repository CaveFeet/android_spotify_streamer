package com.n8.spotifystreamer;

import android.app.Application;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

public class SpotifyStreamerApplication extends Application {

  private static class LazyHolder{
    private static SpotifyApi sSpotifyApi = new SpotifyApi();
  }

  public static SpotifyService getSpotifyService() {
    return LazyHolder.sSpotifyApi.getService();
  }

}
