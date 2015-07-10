package com.n8.spotifystreamer.playback;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;

import com.n8.spotifystreamer.BaseFragmentController;
import com.n8.spotifystreamer.BusProvider;
import com.n8.spotifystreamer.events.TrackPausedEvent;
import com.n8.spotifystreamer.events.TrackStartedEvent;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

public class PlaybackFragmentController extends BaseFragmentController<PlaybackFragmentView> implements PlaybackController {

  private static final String TAG = PlaybackFragmentController.class.getSimpleName();

  private enum PlaybackState{
    PLAYING, PAUSED
  }

  private List<Track> mTracks;

  private Track mCurrentTrack;

  boolean mFullScreen;

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

    hideHeaderMediaControls();

    final MyGestureDetector gestureDetector = new MyGestureDetector(mView.getContext(), new MyGestureListener());
    view.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
      }
    });

    String thumbnailUrl = mCurrentTrack.album.images.get(0).url;

    Picasso.with(mView.getContext()).load(thumbnailUrl).into(mView.mHeaderThumbnail);
    Picasso.with(mView.getContext()).load(thumbnailUrl).into(mView.mAlbumArtImageView);

    mView.mHeaderTrackTitleTextView.setText(mCurrentTrack.name);
    mView.mHeaderArtistNameTextView.setText(mCurrentTrack.artists.get(0).name);
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

    updateHeaderMediaControls(PlaybackState.PLAYING);
  }

  private void updateHeaderMediaControls(PlaybackState state) {
    mView.mHeaderProgressBar.setVisibility(View.GONE);

    if (mView.getY() == 0) {
      return;
    }

    if (state == PlaybackState.PLAYING) {
      mView.mHeaderPauseImageView.setVisibility(View.VISIBLE);
      mView.mHeaderPlayImageView.setVisibility(View.GONE);
    } else if (state == PlaybackState.PAUSED) {
      mView.mHeaderPlayImageView.setVisibility(View.VISIBLE);
      mView.mHeaderPauseImageView.setVisibility(View.GONE);
    }
  }

  @Subscribe
  public void onTrackPaused(TrackPausedEvent event) {
    mView.mPlayButton.setVisibility(View.VISIBLE);
    mView.mPauseButton.setVisibility(View.GONE);

    updateHeaderMediaControls(PlaybackState.PAUSED);
  }

  private void hideHeaderMediaControls(){
    mView.mHeaderPauseImageView.setVisibility(View.GONE);
    mView.mHeaderPlayImageView.setVisibility(View.GONE);
    mView.mHeaderProgressBar.setVisibility(View.GONE);
  }

  private void showHeaderMediaControls(){
    mView.mHeaderPauseImageView.setVisibility(mView.mPauseButton.getVisibility());
    mView.mHeaderPlayImageView.setVisibility(mView.mPlayButton.getVisibility());
    mView.mHeaderProgressBar.setVisibility(mView.mBufferProgressBar.getVisibility());
  }

  class MyGestureDetector extends GestureDetectorCompat {

    private MyGestureListener mMyGestureListener;

    public MyGestureDetector(Context context, MyGestureListener listener) {
      super(context, listener);
      mMyGestureListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
      boolean gestureHandled = super.onTouchEvent(event);
      boolean upHandled = false;

      if (event.getAction() == MotionEvent.ACTION_UP) {
        upHandled = mMyGestureListener.onUp(event);
      }

      return gestureHandled || upHandled;
    }
  }

  class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

    private final String TAG = MyGestureListener.class.getSimpleName();

    private final long FLING_ANIMATION_DURATION = 200;

    private float mYOffset;

    private float mDeltaY;

    public MyGestureListener() {
      TypedValue tv = new TypedValue();
      mView.getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
      mYOffset = mView.getContext().getResources().getDimensionPixelSize(tv.resourceId);
    }

    public boolean onDown(MotionEvent event) {
      return true;
    }

    public boolean onUp(MotionEvent event) {

      if (mDeltaY < 0 && mView.getY() < (mView.getHeight() * .85)) { // Dragging up from bottom
        animateUp();
      } else if (mDeltaY < 0 && mView.getY() > (mView.getHeight() * .85)) {
        animateDown();
      } else if (mDeltaY > 0 && mView.getY() > (mView.getHeight() * .15)) { // Dragging down from top
        animateDown();
      } else if (mDeltaY > 0 && mView.getY() < (mView.getHeight() * .15)) {
        animateUp();
      } else {
        animateUp();
      }

      checkIfFullScreen();

      mDeltaY = 0;

      return true;
    }

    private void checkIfFullScreen() {
      if (mView.getY() == 0) {
        mFullScreen = true;
      }else if (mView.getY() == (mView.getHeight() - mYOffset)) {
        mFullScreen = false;
      }
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
      mDeltaY = e2.getY() - e1.getY();

      if (mView.getY() + mDeltaY < 0) {
        float overscroll = mView.getY() + mDeltaY;
        mDeltaY -= overscroll;
      }else if (mView.getY() + mDeltaY > getMaxYScroll()) {
        float overscroll = mView.getY() - mDeltaY;
        mDeltaY += overscroll;
      }

      float newY = mView.getY() + mDeltaY;
      mView.setY(newY);

      checkIfFullScreen();

      return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
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
      mFullScreen = true;
      hideHeaderMediaControls();
      mView.animate()
          .y(0)
          .setDuration(FLING_ANIMATION_DURATION)
          .setInterpolator(new AccelerateDecelerateInterpolator())
          .start();

    }

    private void animateDown(){
      mFullScreen = false;
      showHeaderMediaControls();
      mView.animate()
          .y(getMaxYScroll())
          .setDuration(FLING_ANIMATION_DURATION)
          .setInterpolator(new AccelerateDecelerateInterpolator())
          .start();
    }
  }
}
