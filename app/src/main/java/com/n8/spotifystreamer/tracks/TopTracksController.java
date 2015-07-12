/*
COPYRIGHT 1995-2015 ESRI

TRADE SECRETS: ESRI PROPRIETARY AND CONFIDENTIAL 
Unpublished material - all rights reserved under the Copyright Laws of the United States.

For additional information, contact: Environmental Systems Research Institute, Inc. 
Attn: Contracts Dept 380 New York Street Redlands, California, USA 92373

email: contracts@esri.com
*/
package com.n8.spotifystreamer.tracks;

import android.support.v7.widget.LinearLayoutManager;

import com.n8.n8droid.ViewController;

public interface TopTracksController extends ViewController {
  void onNavIconClicked();

  void onSettingsMenuOptionClicked();

  LinearLayoutManager getLinearLayoutManager();

  String getArtistName();
}
