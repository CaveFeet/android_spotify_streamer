package com.n8.spotifystreamer.playback;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.n8.n8droid.BaseFragmentView;
import com.n8.spotifystreamer.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class PlaybackFragmentView extends BaseFragmentView<PlaybackController> {

  private final long FLING_ANIMATION_DURATION = 200;

  @InjectView(R.id.fragment_playback_media_controls_container)
  View mFooterView;

  @InjectView(R.id.fragment_playback_header_container)
  View mHeaderView;

  @InjectView(R.id.fragment_playback_play_imageButton)
  ImageButton mPlayButton;

  @InjectView(R.id.fragment_playback_pause_imageButton)
  ImageButton mPauseButton;

  @InjectView(R.id.fragment_playback_next_imageButton)
  ImageButton mNextButton;

  @InjectView(R.id.fragment_playback_prev_imageButton)
  ImageButton mPrevButton;

  @InjectView(R.id.fragment_playback_buffer_progressBar)
  ProgressBar mBufferProgressBar;

  @InjectView(R.id.fragment_playback_progress_seekbar)
  SeekBar mProgressSeekBar;

  @InjectView(R.id.fragment_playback_current_progress_textView)
  TextView mCurrentProgressTextView;

  @InjectView(R.id.fragment_playback_duration_textView)
  TextView mDurationTextView;

  @InjectView(R.id.fragment_playback_album_image)
  ImageView mAlbumArtImageView;

  @InjectView(R.id.fragment_playback_header_shadow)
  ImageView mHeaderShadowImageView;

  @InjectView(R.id.fragment_playback_header_track_title_textView)
  TextView mHeaderTrackTitleTextView;

  @InjectView(R.id.fragment_playback_header_album_name_textView)
  TextView mHeaderAlbumNameTextView;

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

  @InjectView(R.id.fragment_playback_spotify_badge_imageView)
  ImageView mSpotifyBadgeImageView;

  private float mYOffset;

  private boolean mFullScreen;

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
  protected void onFinishInflate() {
    super.onFinishInflate();
    mYOffset = getContext().getResources().getDimensionPixelSize(R.dimen.playback_fragment_header_height);
  }

  @Override
  public void setController(@NonNull FragmentActivity activity, @NonNull PlaybackController controller) {
    super.setController(activity, controller);

    if (mController.isExpandable()) {
      final MyGestureDetector gestureDetector = new MyGestureDetector(getContext(), new MyGestureListener());
      setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
          if (mController == null || !mController.isExpansionEnabled()) {
            return false;
          }

          return gestureDetector.onTouchEvent(event);
        }
      });
    }
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

  @OnClick(R.id.fragment_playback_next_imageButton)
  void onNextClicked(){
    mController.onNextClicked();
  }

  @OnClick(R.id.fragment_playback_prev_imageButton)
  void onPrevClicked(){
    mController.onPrevClicked();
  }

  void setCollapsed(){
    setY(getMaxYScroll());
  }

  void setPlayVisibility(int visibility) {
    mPlayButton.setVisibility(visibility);

    // If view is expanded to fill screen, don't duplicate the media controls in the header view
    //
    if (!isFullScreen()) {
      mHeaderPlayImageView.setVisibility(visibility);
    } else {
      mHeaderPlayImageView.setVisibility(View.GONE);
    }
  }

  void setPauseVisibility(int visibility) {
    mPauseButton.setVisibility(visibility);

    // If view is expanded to fill screen, don't duplicate the media controls in the header view
    //
    if (!isFullScreen()) {
      mHeaderPauseImageView.setVisibility(visibility);
    } else {
      mHeaderPauseImageView.setVisibility(View.GONE);
    }
  }

  void setBufferVisibility(int visibility) {
    mBufferProgressBar.setVisibility(visibility);

    // If view is expanded to fill screen, don't duplicate the media controls in the header view
    //
    if (!isFullScreen()) {
      mHeaderProgressBar.setVisibility(visibility);
    } else {
      mHeaderProgressBar.setVisibility(View.GONE);
    }
  }

  void hideHeaderMediaControls(){
    mHeaderPauseImageView.setVisibility(View.GONE);
    mHeaderPlayImageView.setVisibility(View.GONE);
    mHeaderProgressBar.setVisibility(View.GONE);
    mHeaderShadowImageView.setVisibility(View.GONE);
  }

  void showHeaderMediaControls() {
    mHeaderPauseImageView.setVisibility(mPauseButton.getVisibility());
    mHeaderPlayImageView.setVisibility(mPlayButton.getVisibility());
    mHeaderProgressBar.setVisibility(mBufferProgressBar.getVisibility());
    mHeaderShadowImageView.setVisibility(View.VISIBLE);
  }

  private boolean isFullScreen() {
    if (getY() == 0) {
      mFullScreen = true;
    }else if (getY() == (getHeight() - mYOffset)) {
      mFullScreen = false;
    }
    return mFullScreen;
  }

  private void animateUp(){
    mFullScreen = true;
    hideHeaderMediaControls();
    animate()
        .y(0)
        .setDuration(FLING_ANIMATION_DURATION)
        .setInterpolator(new AccelerateDecelerateInterpolator())
        .setListener(new Animator.AnimatorListener() {
          @Override
          public void onAnimationStart(Animator animation) {
            mAlbumArtImageView.setVisibility(View.VISIBLE);
          }

          @Override
          public void onAnimationEnd(Animator animation) {

          }

          @Override
          public void onAnimationCancel(Animator animation) {

          }

          @Override
          public void onAnimationRepeat(Animator animation) {

          }
        })
        .start();

  }

  private void animateDown(){
    mFullScreen = false;
    showHeaderMediaControls();
    animate()
        .y(getMaxYScroll())
        .setDuration(FLING_ANIMATION_DURATION)
        .setInterpolator(new AccelerateDecelerateInterpolator())
        .setListener(new Animator.AnimatorListener() {
          @Override
          public void onAnimationStart(Animator animation) {

          }

          @Override
          public void onAnimationEnd(Animator animation) {
            mAlbumArtImageView.setVisibility(View.GONE);
          }

          @Override
          public void onAnimationCancel(Animator animation) {

          }

          @Override
          public void onAnimationRepeat(Animator animation) {

          }
        })
        .start();
  }

  private float getMaxYScroll(){
    return getHeight() - mYOffset;
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

      if (mDeltaY < 0 && getY() < (getHeight() * .85)) { // Dragging up from bottom
        animateUp();
      } else if (mDeltaY < 0 && getY() > (getHeight() * .85)) {
        animateDown();
      } else if (mDeltaY > 0 && getY() > (getHeight() * .15)) { // Dragging down from top
        animateDown();
      } else if (mDeltaY > 0 && getY() < (getHeight() * .15)) {
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

      if (getY() + mDeltaY < 0) {
        float overscroll = getY() + mDeltaY;
        mDeltaY -= overscroll;
      }else if (getY() + mDeltaY > getMaxYScroll()) {
        float overscroll = getY() - mDeltaY;
        mDeltaY += overscroll;
      }

      float newY = getY() + mDeltaY;
      setY(newY);

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
