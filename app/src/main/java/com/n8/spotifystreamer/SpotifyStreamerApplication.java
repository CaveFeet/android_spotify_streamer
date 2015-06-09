/*
COPYRIGHT 1995-2015 ESRI

TRADE SECRETS: ESRI PROPRIETARY AND CONFIDENTIAL 
Unpublished material - all rights reserved under the Copyright Laws of the United States.

For additional information, contact: Environmental Systems Research Institute, Inc. 
Attn: Contracts Dept 380 New York Street Redlands, California, USA 92373

email: contracts@esri.com
*/
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
