package com.n8.spotifystreamer.playback;

import com.n8.spotifystreamer.BaseFragmentView;


public interface PlaybackController extends BaseFragmentView.Controller {
  void onPlayClicked();

  void onPauseClicked();
}
