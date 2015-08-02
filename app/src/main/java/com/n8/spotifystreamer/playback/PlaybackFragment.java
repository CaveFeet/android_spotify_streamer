package com.n8.spotifystreamer.playback;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.SeekBar;

import com.n8.n8droid.BaseViewControllerDialogFragment;
import com.n8.n8droid.BaseViewControllerFragment;
import com.n8.n8droid.TimeUtils;
import com.n8.spotifystreamer.BusProvider;
import com.n8.spotifystreamer.R;
import com.n8.spotifystreamer.events.NextTrackEvent;
import com.n8.spotifystreamer.events.PlaybackProgressEvent;
import com.n8.spotifystreamer.events.PlaybackServiceStateBroadcastEvent;
import com.n8.spotifystreamer.events.PlaybackServiceStateRequestEvent;
import com.n8.spotifystreamer.events.PrevTrackEvent;
import com.n8.spotifystreamer.events.SeekbarChangedEvent;
import com.n8.spotifystreamer.events.TrackPausedEvent;
import com.n8.spotifystreamer.events.TrackPlaybackCompleteEvent;
import com.n8.spotifystreamer.events.TrackStartedEvent;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

public class PlaybackFragment extends BaseViewControllerDialogFragment<PlaybackFragmentView> implements PlaybackController,
    SeekBar.OnSeekBarChangeListener {

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
    fragment.setPlaybackInfo(tracks, track);

    return fragment;
  }

  public PlaybackFragment() { }

  public void setPlaybackInfo(@NonNull List<Track> tracks, @NonNull Track track) {
    mTracks = tracks;
    mTrack = track;

    if (mView != null) {
      bindTrackInfo();
    }
  }

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

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Dialog dialog = super.onCreateDialog(savedInstanceState);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    return dialog;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    // When view is layed out, start it at the bottom if not being shown in dialog.
    //
    mView.getViewTreeObserver().addOnGlobalLayoutListener(new     ViewTreeObserver.OnGlobalLayoutListener() {
      public void onGlobalLayout() {
        mView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        if (!getShowsDialog()) {
          mView.setY(getMaxYScroll());
        }
        mView.setVisibility(View.VISIBLE);
        bindTrackInfo();
      }
    });
    BusProvider.getInstance().register(this);

    mView.setVisibility(View.INVISIBLE);

    mYOffset = mView.getContext().getResources().getDimensionPixelSize(R.dimen.playback_fragment_header_height);

    // If isn't being shown in dialog, allow to be swiped up or down
    //
    if (!getShowsDialog()) {
      final MyGestureDetector gestureDetector = new MyGestureDetector(mView.getContext(), new MyGestureListener());
      mView.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
          return gestureDetector.onTouchEvent(event);
        }
      });
    }

    return mView;
  }

  private void bindTrackInfo() {
    setPlayVisibility(View.GONE);
    setPauseVisibility(View.GONE);
    setBufferVisibility(View.VISIBLE);

    if (mTracks.indexOf(mTrack) == 0) {
      mView.mPrevButton.setEnabled(true);
    }else if (mTracks.indexOf(mTrack) == mTracks.size() - 1) {
      mView.mNextButton.setEnabled(false);
    }

    String thumbnailUrl = mTrack.album.images.get(0).url;

    Picasso.with(mView.getContext()).load(thumbnailUrl).into(mView.mHeaderThumbnail);
    Picasso.with(mView.getContext()).load(thumbnailUrl).into(mView.mAlbumArtImageView);

    mView.mHeaderTrackTitleTextView.setText(mTrack.name);
    mView.mHeaderAlbumNameTextView.setText(mTrack.album.name);
    mView.mHeaderArtistNameTextView.setText(mTrack.artists.get(0).name);

    mView.mSpotifyBadgeImageView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mTrack.external_urls.get("spotify")));
        startActivity(intent);
      }
    });
  }

  @Override
  public void onDetach() {
    super.onDetach();
    BusProvider.getInstance().unregister(this);
  }

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {

  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
    BusProvider.getInstance().post(new SeekbarChangedEvent(seekBar.getProgress()));
  }

  @Override
  public void onPlayClicked() {
    sendServiceAction(PlaybackService.ACTION_PLAY);
  }

  @Override
  public void onPauseClicked() {
    sendServiceAction(PlaybackService.ACTION_PAUSE);
  }

  @Override
  public void onNextClicked() {
    sendServiceAction(PlaybackService.ACTION_NEXT);
  }

  @Override
  public void onPrevClicked() {
    sendServiceAction(PlaybackService.ACTION_PREVIOUS);
  }

  @Subscribe
  public void onPlaybackProgressEventReceived(final PlaybackProgressEvent event) {
    mView.mProgressSeekBar.setProgress((int)event.getProgress());

    // Format and set current duration text
    //
    mView.mCurrentProgressTextView.setText(TimeUtils.getFormattedTime(event.getProgress()));
  }

  @Subscribe
  public void onPlaybackServiceStateBroadcastEventReceived(PlaybackServiceStateBroadcastEvent event) {
    if (event.isPlaying()) {
      mView.mPauseButton.setVisibility(View.VISIBLE);
      mView.mPlayButton.setVisibility(View.GONE);
      mView.mBufferProgressBar.setVisibility(View.GONE);
    }
  }

  @Subscribe
  public void onTrackStarted(TrackStartedEvent event) {
    setPauseVisibility(View.VISIBLE);
    setPlayVisibility(View.GONE);
    setBufferVisibility(View.GONE);

    mView.mProgressSeekBar.setMax((int) event.getDuration());
    mView.mProgressSeekBar.setOnSeekBarChangeListener(this);

    // Format and set duration text
    mView.mDurationTextView.setText(TimeUtils.getFormattedTime(event.getDuration()));

    // Format and set current duration text to be 0
    mView.mCurrentProgressTextView.setText(TimeUtils.getFormattedTime(0));
  }

  @Subscribe
  public void onTrackPaused(TrackPausedEvent event) {
    setPlayVisibility(View.VISIBLE);
    setPauseVisibility(View.GONE);
    setBufferVisibility(View.GONE);
  }

  @Subscribe
  public void onNextTrack(NextTrackEvent event) {
    mView.mPrevButton.setEnabled(true);

    mTrack = mTracks.get(event.getTrackIndex());
    bindTrackInfo();
  }

  @Subscribe
  public void onPrevTrack(PrevTrackEvent event) {
    mView.mNextButton.setEnabled(true);

    mTrack = mTracks.get(event.getTrackIndex());
    bindTrackInfo();
  }

  @Subscribe
  public void onTrackPlaybackComplete(TrackPlaybackCompleteEvent event) {
    setPlayVisibility(View.VISIBLE);
    setPauseVisibility(View.GONE);
    setBufferVisibility(View.GONE);
      mView.mProgressSeekBar.setProgress(0);
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

  private void sendServiceAction(String actionPlay) {
    Intent playbackIntent = new Intent(mView.getContext(), PlaybackService.class);
    playbackIntent.setAction(actionPlay);
    mView.getContext().startService(playbackIntent);
  }

  private boolean isFullScreen() {
    if (mView.getY() == 0) {
      mFullScreen = true;
    }else if (mView.getY() == (mView.getHeight() - mYOffset)) {
      mFullScreen = false;
    }
    return mFullScreen;
  }

  private void setPlayVisibility(int visibility) {
    mView.mPlayButton.setVisibility(visibility);

    // If view is expanded to fill screen, don't duplicate the media controls in the header view
    //
    if (!isFullScreen()) {
      mView.mHeaderPlayImageView.setVisibility(visibility);
    } else {
      mView.mHeaderPlayImageView.setVisibility(View.GONE);
    }
  }

  private void setPauseVisibility(int visibility) {
    mView.mPauseButton.setVisibility(visibility);

    // If view is expanded to fill screen, don't duplicate the media controls in the header view
    //
    if (!isFullScreen()) {
      mView.mHeaderPauseImageView.setVisibility(visibility);
    } else {
      mView.mHeaderPauseImageView.setVisibility(View.GONE);
    }
  }

  private void setBufferVisibility(int visibility) {
    mView.mBufferProgressBar.setVisibility(visibility);

    // If view is expanded to fill screen, don't duplicate the media controls in the header view
    //
    if (!isFullScreen()) {
      mView.mHeaderProgressBar.setVisibility(visibility);
    } else {
      mView.mHeaderProgressBar.setVisibility(View.GONE);
    }
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

  /**
   * Detects swipe up and swipe down gestures on the view.  This allows the the fragment to be expanded or
   * collapsed on a swipe gesture.
   */
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

  /**
   * Listens for detected gestures, and responds apporpriatley be animating up or animating down the view.
   */
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

      isFullScreen();

      mDeltaY = 0;

      return true;
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

      isFullScreen();

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
