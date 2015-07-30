package com.n8.spotifystreamer.playback;


import com.n8.n8droid.ViewController;

public interface PlaybackController extends ViewController {
  void onPlayClicked();

  void onPauseClicked();

  void onNextClicked();

  void onPrevClicked();
}
