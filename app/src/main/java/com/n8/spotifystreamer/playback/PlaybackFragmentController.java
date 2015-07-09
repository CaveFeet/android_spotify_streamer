package com.n8.spotifystreamer.playback;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.MediaController;

import com.n8.spotifystreamer.BaseFragmentController;
import com.n8.spotifystreamer.BusProvider;
import com.n8.spotifystreamer.R;
import com.n8.spotifystreamer.UiUtils;
import com.n8.spotifystreamer.events.TrackPausedEvent;
import com.n8.spotifystreamer.events.TrackStartedEvent;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

public class PlaybackFragmentController extends BaseFragmentController<PlaybackFragmentView> implements PlaybackController {

  private static final String TAG = PlaybackFragmentController.class.getSimpleName();

  private List<Track> mTracks;

  private Track mCurrentTrack;

  public PlaybackFragmentController(List<Track> tracks, Track track) {
    mTracks = tracks;
    mCurrentTrack = track;
  }

  @Override
  public void onCreateView(@NonNull PlaybackFragmentView view) {
    super.onCreateView(view);
    BusProvider.getInstance().register(this);

    mView.mPauseButton.setVisibility(View.GONE);
    mView.mPlayButton.setVisibility(View.GONE);

    final GestureDetectorCompat mDetector = new GestureDetectorCompat(mView.getContext(), new MyGestureListener());
    view.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        return mDetector.onTouchEvent(event);
      }
    });
  }

  @Override
  public void onDetachView() {
    super.onDetachView();
    BusProvider.getInstance().unregister(this);
  }

  @Override
  public void onPlayClicked() {
    Intent playbackIntent = new Intent(mView.getContext(), PlaybackService.class);
    playbackIntent.setAction(PlaybackService.ACTION_PLAY);
    mView.getContext().startService(playbackIntent);
  }

  @Override
  public void onPauseClicked() {
    Intent playbackIntent = new Intent(mView.getContext(), PlaybackService.class);
    playbackIntent.setAction(PlaybackService.ACTION_PAUSE);
    mView.getContext().startService(playbackIntent);
  }

  @Subscribe
  public void onTrackStarted(TrackStartedEvent event) {
    mView.mPauseButton.setVisibility(View.VISIBLE);
    mView.mPlayButton.setVisibility(View.GONE);
    mView.mBufferProgressBar.setVisibility(View.GONE);
    Picasso.with(mView.getContext()).load(event.getThumbnailUrl()).into(mView.mAlbumArtImageView);
  }

  @Subscribe
  public void onTrackPaused(TrackPausedEvent event) {
    mView.mPlayButton.setVisibility(View.VISIBLE);
    mView.mPauseButton.setVisibility(View.GONE);
  }

  class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

    private final String TAG = MyGestureListener.class.getSimpleName();

    private final int FLING_THRESHOLD = 250;

    // TODO update

    private final long FLING_ANIMATION_DURATION = 200;

    // header
    // based on mView's

    private float mYOffset;

    public MyGestureListener() {
      TypedValue tv = new TypedValue();
      mView.getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
      mYOffset = mView.getContext().getResources().getDimensionPixelSize(tv.resourceId);
    }

    public boolean onDown(MotionEvent event) {
      return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
      float delta = e2.getY() - e1.getY();

      if (mView.getY() + delta < 0) {
        float overscroll = mView.getY() + delta;
        delta -= overscroll;
      }else if (mView.getY() + delta > getMaxYScroll()) {
        float overscroll = mView.getY() - delta;
        delta += overscroll;
      }

      float newY = mView.getY() + delta;
      mView.setY(newY);

      return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
      Log.d(TAG, "onFling: velocityY = " + velocityY);

      if (Math.abs(velocityY) < FLING_THRESHOLD) {

        // Dragging up from bottom
        //
        if (velocityY < 0 && mView.getY() < (mView.getHeight() * .85)) {
          animateDown();
        }else if (velocityY < 0 && mView.getY() > (mView.getHeight() * .85)) {
          animateUp();
        }

        // Dragging down from top
        if (velocityY > 0 && mView.getY() > (mView.getHeight() * .15)) {
          animateUp();
        }else if (velocityY > 0 && mView.getY() < (mView.getHeight() * .15)) {
          animateDown();
        }
        return false;
      }

      if (velocityY < 0) {
        animateUp();
      } else {
        animateDown();
      }

      return true;
    }

    private float getMaxYScroll(){
      return mView.getHeight() - mYOffset;
    }

    private void animateUp(){
      mView.animate()
          .y(0)
          .setDuration(FLING_ANIMATION_DURATION)
          .setInterpolator(new AccelerateDecelerateInterpolator())
          .start();

    }

    private void animateDown(){
      mView.animate()
          .y(getMaxYScroll())
          .setDuration(FLING_ANIMATION_DURATION)
          .setInterpolator(new AccelerateDecelerateInterpolator())
          .start();
    }
  }
}
