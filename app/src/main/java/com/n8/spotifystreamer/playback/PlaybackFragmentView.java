package com.n8.spotifystreamer.playback;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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

  @InjectView(R.id.fragment_playback_buffer_progressBar)
  ProgressBar mBufferProgressBar;

  @InjectView(R.id.fragment_playback_album_image)
  ImageView mAlbumArtImageView;

  @InjectView(R.id.fragment_playback_header_track_title_textView)
  TextView mHeaderTrackTitleTextView;

  @InjectView(R.id.fragment_playback_header_artist_name_textView)
  TextView mHeaderArtistNameTextView;

  @InjectView(R.id.fragment_playback_header_thumbnail)
  ImageView mHeaderThumbnail;

  @InjectView(R.id.fragment_playback_header_play_imageView)
  ImageView mHeaderPlayImageView;

  @InjectView(R.id.fragment_playback_header_pause_imageView)
  ImageView mHeaderPauseImageView;

  @InjectView(R.id.fragment_playback_header_buffer_progressBar)
  ProgressBar mHeaderProgressBar;

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

  @OnClick({R.id.fragment_playback_play_imageButton, R.id.fragment_playback_header_play_imageView})
  void onPlayClicked(){
    mController.onPlayClicked();
  }

  @OnClick({R.id.fragment_playback_pause_imageButton, R.id.fragment_playback_header_pause_imageView})
  void onPauseClicked() {
    mController.onPauseClicked();
  }
}
