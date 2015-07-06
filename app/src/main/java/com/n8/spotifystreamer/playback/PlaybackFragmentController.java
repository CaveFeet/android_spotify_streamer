package com.n8.spotifystreamer.playback;

import android.animation.Animator;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.MediaController;

import com.n8.spotifystreamer.BaseFragmentController;
import com.n8.spotifystreamer.BusProvider;
import com.n8.spotifystreamer.events.TrackPausedEvent;
import com.n8.spotifystreamer.events.TrackStartedEvent;
import com.squareup.otto.Subscribe;

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
  }

  @Subscribe
  public void onTrackPaused(TrackPausedEvent event) {
    mView.mPlayButton.setVisibility(View.VISIBLE);
    mView.mPauseButton.setVisibility(View.GONE);
  }

  class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
    private static final String DEBUG_TAG = "Gestures";

    boolean mAnimating;

    @Override
    public boolean onDown(MotionEvent event) {
      //Log.d(DEBUG_TAG, "onDown: " + event.toString());
      return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

      float delta = e2.getY() - e1.getY();
      if (mView.getY() + delta < 0) {
        float overscroll = mView.getY() + delta;
        delta -= overscroll;
      }

      Log.d(TAG, "delta = " + delta);

      if (!mAnimating) {
        mView.setY(mView.getY() + delta);
//        mView.animate().translationYBy(delta).setListener(new Animator.AnimatorListener() {
//          @Override
//          public void onAnimationStart(Animator animation) {
//            mAnimating = true;
//          }
//
//          @Override
//          public void onAnimationEnd(Animator animation) {
//            mAnimating = false;
//          }
//
//          @Override
//          public void onAnimationCancel(Animator animation) {
//            mAnimating = false;
//          }
//
//          @Override
//          public void onAnimationRepeat(Animator animation) {
//
//          }
//        });
      }
      return super.onScroll(e1, e2, distanceX, distanceY);
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                           float velocityX, float velocityY) {
      Log.d(DEBUG_TAG, "onFling: " + event1.toString()+event2.toString());
      return true;
    }
  }
}
