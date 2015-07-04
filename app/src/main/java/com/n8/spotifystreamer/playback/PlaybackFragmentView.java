package com.n8.spotifystreamer.playback;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;
import android.widget.MediaController;

import com.n8.spotifystreamer.BaseFragmentView;
import com.n8.spotifystreamer.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class PlaybackFragmentView extends BaseFragmentView<PlaybackController> {

  @InjectView(R.id.fragment_playback_play_imageButton)
  ImageButton mPlayButton;

  @InjectView(R.id.fragment_playback_pause_imageButton)
  ImageButton mPauseButton;

  public PlaybackFragmentView(Context context) {
    super(context);
  }

  public PlaybackFragmentView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public PlaybackFragmentView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @TargetApi(21)
  public PlaybackFragmentView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override
  protected void setupView() {
    ButterKnife.inject(this);
  }

  @OnClick(R.id.fragment_playback_play_imageButton)
  void onPlayClicked(ImageButton button){
    mController.onPlayClicked();
  }

  @OnClick(R.id.fragment_playback_pause_imageButton)
  void onPauseClicked(ImageButton button) {
    mController.onPauseClicked();
  }
}
