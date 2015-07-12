package com.n8.spotifystreamer.playback;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.n8.n8droid.BaseViewControllerFragment;
import com.n8.spotifystreamer.BusProvider;
import com.n8.spotifystreamer.R;
import com.n8.spotifystreamer.events.TrackPausedEvent;
import com.n8.spotifystreamer.events.TrackPlaybackCompleteEvent;
import com.n8.spotifystreamer.events.TrackStartedEvent;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

public class PlaybackFragment extends BaseViewControllerFragment<PlaybackFragmentView> implements PlaybackController {

  private static final String TAG = PlaybackFragment.class.getSimpleName();

  private List<Track> mTracks;

  private Track mTrack;

  private enum PlaybackState{
    PLAYING, PAUSED
  }

  private final long FLING_ANIMATION_DURATION = 200;

  private float mYOffset;

  boolean mFullScreen;

  public static PlaybackFragment getInstance(List<Track> tracks, Track track) {
    PlaybackFragment fragment = new PlaybackFragment();
    fragment.mTracks = tracks;
    fragment.mTrack = track;

    return fragment;
  }

  public PlaybackFragment() { }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
  }

  @Override
  protected int getLayoutId() {
    return R.layout.fragment_playback;
  }

  @Override
  protected void setViewController() {
    mView.setController(getActivity(), this);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    BusProvider.getInstance().register(this);

    TypedValue tv = new TypedValue();
    mView.getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
    mYOffset = mView.getContext().getResources().getDimensionPixelSize(tv.resourceId);

//    mView.setVisibility(View.INVISIBLE);
//    mView.setY(mView.getMeasuredHeight() - mYOffset);
//    mView.setVisibility(View.VISIBLE);

    mView.mPauseButton.setVisibility(View.GONE);
    mView.mPlayButton.setVisibility(View.GONE);

    hideHeaderMediaControls();

    final MyGestureDetector gestureDetector = new MyGestureDetector(mView.getContext(), new MyGestureListener());
    mView.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
      }
    });

    String thumbnailUrl = mTrack.album.images.get(0).url;

    Picasso.with(mView.getContext()).load(thumbnailUrl).into(mView.mHeaderThumbnail);
    Picasso.with(mView.getContext()).load(thumbnailUrl).into(mView.mAlbumArtImageView);

    mView.mHeaderTrackTitleTextView.setText(mTrack.name);
    mView.mHeaderArtistNameTextView.setText(mTrack.artists.get(0).name);

    return mView;
  }

  @Override
  public void onDetach() {
    super.onDetach();
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
    mView.mBufferProgressBar.setVisibility(View.GONE);

    updateHeaderMediaControls(PlaybackState.PAUSED);
  }

  @Subscribe
  public void onTrackPlaybackComplete(TrackPlaybackCompleteEvent event) {
    mView.mPlayButton.setVisibility(View.VISIBLE);
    mView.mPauseButton.setVisibility(View.GONE);
    mView.mBufferProgressBar.setVisibility(View.GONE);

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

  private float getMaxYScroll(){
    return mView.getHeight() - mYOffset;
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

    private float mDeltaY;

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
  }
}
