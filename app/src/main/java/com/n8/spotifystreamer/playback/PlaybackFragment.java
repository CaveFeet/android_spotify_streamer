package com.n8.spotifystreamer.playback;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.SeekBar;

import com.n8.n8droid.BaseViewControllerFragment;
import com.n8.n8droid.TimeUtils;
import com.n8.spotifystreamer.BusProvider;
import com.n8.spotifystreamer.R;
import com.n8.spotifystreamer.events.NextTrackEvent;
import com.n8.spotifystreamer.events.PlaybackProgressEvent;
import com.n8.spotifystreamer.events.PlaybackServiceStateBroadcastEvent;
import com.n8.spotifystreamer.events.PrevTrackEvent;
import com.n8.spotifystreamer.events.SeekbarChangedEvent;
import com.n8.spotifystreamer.events.TrackPausedEvent;
import com.n8.spotifystreamer.events.TrackPlaybackCompleteEvent;
import com.n8.spotifystreamer.events.TrackStartedEvent;
import com.n8.spotifystreamer.models.ParcelableTrack;
import com.n8.spotifystreamer.models.ParcelableTracks;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class PlaybackFragment extends BaseViewControllerFragment<PlaybackFragmentView> implements PlaybackController,
    SeekBar.OnSeekBarChangeListener {

  private static final String TAG = PlaybackFragment.class.getSimpleName();

  private ParcelableTracks mTracks;

  private ParcelableTrack mTrack;

  private boolean mIsRetained = true;

  private boolean mIsExpandable = true;

  public static PlaybackFragment getInstance() {
    PlaybackFragment fragment = new PlaybackFragment();

    return fragment;
  }

  public PlaybackFragment() { }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(mIsRetained);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    // When view is layed out, start it at the bottom if not being shown in dialog.
    //
    mView.getViewTreeObserver().addOnGlobalLayoutListener(new     ViewTreeObserver.OnGlobalLayoutListener() {
      public void onGlobalLayout() {
        mView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        if (mIsExpandable) {
          mView.setCollapsed();
        }
        mView.setVisibility(View.VISIBLE);
        bindTrackInfo();
      }
    });
    BusProvider.getInstance().register(this);

    mView.setVisibility(View.INVISIBLE);

    return mView;
  }

  public void setIsRetained(boolean isRetained) { mIsRetained = isRetained; }

  public void setExpandable(boolean expandable) { mIsExpandable = expandable; }

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
  public boolean isExpandable() {
    return mIsExpandable;
  }

  @Override
  public boolean isExpansionEnabled() {
    return mTracks != null && mTrack != null;
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
    setPlaybackInfo(event.getTracks(), event.getCurrentTrack());

    if (event.isPlaying()) {
      mView.mPauseButton.setVisibility(View.VISIBLE);
      mView.mPlayButton.setVisibility(View.GONE);
      mView.mBufferProgressBar.setVisibility(View.GONE);
    }
  }

  @Subscribe
  public void onTrackStarted(TrackStartedEvent event) {
    mView.setPauseVisibility(View.VISIBLE);
    mView.setPlayVisibility(View.GONE);
    mView.setBufferVisibility(View.GONE);

    mView.mProgressSeekBar.setMax((int) event.getDuration());
    mView.mProgressSeekBar.setOnSeekBarChangeListener(this);

    // Format and set duration text
    mView.mDurationTextView.setText(TimeUtils.getFormattedTime(event.getDuration()));

    // Format and set current duration text to be 0
    mView.mCurrentProgressTextView.setText(TimeUtils.getFormattedTime(0));
  }

  @Subscribe
  public void onTrackPaused(TrackPausedEvent event) {
    mView.setPlayVisibility(View.VISIBLE);
    mView.setPauseVisibility(View.GONE);
    mView.setBufferVisibility(View.GONE);
  }

  @Subscribe
  public void onNextTrack(NextTrackEvent event) {
    mView.mPrevButton.setEnabled(true);

    mTrack = mTracks.tracks.get(event.getTrackIndex());
    bindTrackInfo();
  }

  @Subscribe
  public void onPrevTrack(PrevTrackEvent event) {
    mView.mNextButton.setEnabled(true);

    mTrack = mTracks.tracks.get(event.getTrackIndex());
    bindTrackInfo();
  }

  @Subscribe
  public void onTrackPlaybackComplete(TrackPlaybackCompleteEvent event) {
    mView.setPlayVisibility(View.VISIBLE);
    mView.setPauseVisibility(View.GONE);
    mView.setBufferVisibility(View.GONE);
      mView.mProgressSeekBar.setProgress(0);
  }

  @Override
  protected int getLayoutId() {
    return R.layout.fragment_playback;
  }

  @Override
  protected void setViewController() {
    mView.setController(getActivity(), this);
  }

  private void setPlaybackInfo(@NonNull ParcelableTracks tracks, @NonNull ParcelableTrack track) {
    mTracks = tracks;
    mTrack = track;

    if (mView != null) {
      bindTrackInfo();
    }
  }

  private void bindTrackInfo() {
    if (mTracks == null || mTrack == null) {
      return;
    }

    mView.setPlayVisibility(View.GONE);
    mView.setPauseVisibility(View.GONE);
    mView.setBufferVisibility(View.VISIBLE);

    if (mTracks.tracks.indexOf(mTrack) == 0) {
      mView.mPrevButton.setEnabled(true);
    }else if (mTracks.tracks.indexOf(mTrack) == mTracks.tracks.size() - 1) {
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

    // Ensure shadow is gone, and album art visible when not expandable/collapsable
    //
    if (!mIsExpandable) {
      mView.hideHeaderMediaControls();
      mView.mAlbumArtImageView.setVisibility(View.VISIBLE);
    }
  }

  private void sendServiceAction(String actionPlay) {
    Intent playbackIntent = new Intent(mView.getContext(), PlaybackService.class);
    playbackIntent.setAction(actionPlay);
    mView.getContext().startService(playbackIntent);
  }
}
