package com.n8.spotifystreamer.playback;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.ImageView;

import com.n8.spotifystreamer.BusProvider;
import com.n8.spotifystreamer.ImageUtils;
import com.n8.spotifystreamer.MainActivity;
import com.n8.spotifystreamer.R;
import com.n8.spotifystreamer.events.PlaybackServiceStateBroadcastEvent;
import com.n8.spotifystreamer.events.PlaybackServiceStateRequestEvent;
import com.n8.spotifystreamer.events.TrackPausedEvent;
import com.n8.spotifystreamer.events.TrackPlaybackCompleteEvent;
import com.n8.spotifystreamer.events.TrackStartedEvent;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class PlaybackService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer
    .OnBufferingUpdateListener, MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnCompletionListener {

  private static final String TAG = PlaybackService.class.getSimpleName();

  public static final String KEY_PLAYLIST = "key_playlist";

  public static final String KEY_TRACK_INDEX = "key_track_index";

  public static final String ACTION_PLAY = "com.n8.spotifystreamer.PLAY";

  public static final String ACTION_PAUSE = "com.n8.spotifystreamer.PAUSE";

  public static final String ACTION_STOP = "com.n8.spotifystreamer.STOP";

  public static final String ACTION_REWIND = "com.n8.spotifystreamer.REWIND";

  public static final String ACTION_FAST_FORWARD = "com.n8.spotifystreamer.FAST_FORWARD";

  public static final String ACTION_NEXT = "com.n8.spotifystreamer.NEXT";

  public static final String ACTION_PREVIOUS = "com.n8.spotifystreamer.PREVIOUS";

  public static final String ACTION_MEDIA_BUTTONS = "com.n8.spotifystreamer.MEDIA_BUTTONS";

  public static final String TAG_WIFI_LOCK = "mylock";

  private static final int NOTIFICATION_ID = 001;

  private MediaPlayer mMediaPlayer;

  private WifiManager.WifiLock mWifiLock;

  private TopTracksPlaylist mTopTracksPlaylist;

  private int mTrackIndex;

  private Bitmap mNotificationImage;

  @Override
  public void onCreate() {
    super.onCreate();
    BusProvider.getInstance().register(this);
  }

  @Override
  public void onDestroy() {
    BusProvider.getInstance().unregister(this);
    cleanupMediaPlayer();
    cleanupWifiLock();
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onTaskRemoved(Intent rootIntent) {
    super.onTaskRemoved(rootIntent);
    Log.d(TAG, "ONTaskRemoved");
    stop();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(TAG, "onStartCommand");

    if (intent != null ) {
      String action = intent.getAction();

      if (action != null) {
        if (action.equals(ACTION_PLAY)) {
          handlePlayIntent(intent);
        } else if (action.equals(ACTION_PAUSE)) {
          pause();
        } else if (action.equals(ACTION_STOP)) {
          stop();
        }
      }
    }

    return START_STICKY;
  }

  @Override
  public void onPrepared(MediaPlayer mp) {
    play();
  }

  @Override
  public void onBufferingUpdate(MediaPlayer mp, int percent) {

  }

  @Override
  public void onCompletion(MediaPlayer mp) {
    pause();
  }

  @Override
  public boolean onError(MediaPlayer mp, int what, int extra) {
    return false;
  }

  // http://developer.android.com/guide/topics/media/mediaplayer.html#preparingasync
  @Override
  public void onAudioFocusChange(int focusChange) {
    switch (focusChange) {
      case AudioManager.AUDIOFOCUS_GAIN:
        // resume playback
        if (mMediaPlayer == null) {
          initMediaPlayer();
        }
        else if (!mMediaPlayer.isPlaying()) {
          mMediaPlayer.start();
        }
        mMediaPlayer.setVolume(1.0f, 1.0f);
        break;

      case AudioManager.AUDIOFOCUS_LOSS:
        // Lost focus for an unbounded amount of time: stop playback and release media player
        stop();
        cleanupMediaPlayer();
        break;

      case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
        // Lost focus for a short time, but we have to stop playback. We don't release the media player because playback is
        // likely to resume
        pause();
        break;

      case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
        // Lost focus for a short time, but it's ok to keep playing at an attenuated level
        if (mMediaPlayer.isPlaying()) {
          mMediaPlayer.setVolume(0.1f, 0.1f);
        }
        break;
    }
  }

  @Subscribe
  public void onPlaybackServiceStateRequestEventReceived(PlaybackServiceStateRequestEvent event) {
    BusProvider.getInstance().post(new PlaybackServiceStateBroadcastEvent(mMediaPlayer));
  }

  private void initMediaPlayer() {
    if (mMediaPlayer == null) {
      mMediaPlayer = new MediaPlayer();
      mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
      mMediaPlayer.setOnPreparedListener(this);
      mMediaPlayer.setOnBufferingUpdateListener(this);
      mMediaPlayer.setOnErrorListener(this);
      mMediaPlayer.setOnCompletionListener(this);

      // Set wake lock to ensue cpu doesn't sleep while playing.
      // Is managed by media player so when music is paused or stopped, the lock is removed
      mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

      // Get a wifi lock that we can set when music is playing
      cleanupWifiLock();
      mWifiLock = ((WifiManager) getSystemService(WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, TAG_WIFI_LOCK);

      try {
        String url = mTopTracksPlaylist.getTrackUrls().get(mTrackIndex);
        if (url == null) {
          return;
        }

        mMediaPlayer.setDataSource(url);
        mMediaPlayer.prepareAsync();
      } catch (IOException e) {
        Log.d(TAG, "Failed to prepare media playter " + e.getMessage());
      }
    } else {
      stop();
      initMediaPlayer();
    }
  }

  private void cleanupMediaPlayer(){
    if (mMediaPlayer != null) {
      mMediaPlayer.release();
    }
    mMediaPlayer = null;
  }

  private void cleanupWifiLock(){
    if (mWifiLock != null && mWifiLock.isHeld()) {
      mWifiLock.release();
    }
    mWifiLock = null;
  }

  private void handlePlayIntent(Intent intent) {
    Log.d(TAG, "Action_Play");

    // Check for new playlist info.  If it exists in the bundle, update the service's members
    //
    TopTracksPlaylist playlist = intent.getParcelableExtra(KEY_PLAYLIST);
    if (playlist != null) {
      mTopTracksPlaylist = playlist;
      mTrackIndex = intent.getIntExtra(KEY_TRACK_INDEX, 0);
      mNotificationImage = null;
      initMediaPlayer();
      return;
    }

    play();
  }

  private void play(){
    AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
    int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

    if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
      Log.d(TAG, "Failed to get audio focus");
    } else {
      mWifiLock.acquire();
      showPlayNotification();
      mMediaPlayer.start();
      BusProvider.getInstance().post(
          new TrackStartedEvent(
              mTopTracksPlaylist.getTrackName(mTrackIndex),
              mTopTracksPlaylist.getTrackAlbumName(mTrackIndex),
              mTopTracksPlaylist.getArtistName(),
              mTopTracksPlaylist.getTrackThumbnailUrl(mTrackIndex)
          )
      );
    }
  }

  private void pause(){
    Log.d(TAG, "Action_Pause");
    if (mWifiLock.isHeld()) {
      mWifiLock.release();
    }

    if (mMediaPlayer.isPlaying()) {
      mMediaPlayer.pause();
      BusProvider.getInstance().post(new TrackPausedEvent());
    }

    stopForeground(false);
    showPauseNotification();
  }

  private void complete(){
    Log.d(TAG, "Playback complete");
    if (mWifiLock.isHeld()) {
      mWifiLock.release();
    }

    BusProvider.getInstance().post(new TrackPlaybackCompleteEvent());

    stopForeground(false);
    showPauseNotification();
  }

  private void stop() {
    Log.d(TAG, "Action_Stop");

    if (mMediaPlayer != null &&  mMediaPlayer.isPlaying()) {
      mMediaPlayer.stop();
    }

    stopForeground(true);
    cleanupMediaPlayer();
    cleanupWifiLock();

    stopSelf();
  }

  private void showPauseNotification() {
    if (mNotificationImage != null) {
      showPauseNotification(mNotificationImage);
      return;
    }

    final ImageView imageView = new ImageView(this);
    Picasso.with(this).load(mTopTracksPlaylist.getTrackThumbnailUrl(mTrackIndex)).into(imageView, new Callback() {
      @Override
      public void onSuccess() {
        mNotificationImage = ImageUtils.drawableToBitmap(imageView.getDrawable());
        showPauseNotification(mNotificationImage);
      }

      @Override
      public void onError() {

      }
    });

    showPauseNotification(null);
  }

  private void showPauseNotification(Bitmap bitmap){

    Notification notification = new NotificationCompat.Builder(this)
        // show controls on lockscreen even when user hides sensitive content.
        .setVisibility(Notification.VISIBILITY_PUBLIC)
        .setSmallIcon(R.drawable.notification_icon)
        .setLargeIcon(bitmap)
            // Add media control buttons that invoke intents in your media service
        .addAction(android.R.drawable.ic_media_previous, "Previous", createNotificationPendingIntent(ACTION_PREVIOUS))
        .addAction(android.R.drawable.ic_media_play, "Play", createNotificationPendingIntent(ACTION_PLAY))
        .addAction(android.R.drawable.ic_media_next, "Next", createNotificationPendingIntent(ACTION_NEXT))
            // Apply the media style template
        .setStyle(new NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(1)
        )
        .setContentTitle(mTopTracksPlaylist.getTrackName(mTrackIndex))
        .setContentText(mTopTracksPlaylist.getArtistName())
        .setSubText(mTopTracksPlaylist.getTrackAlbumName(mTrackIndex))
        .setContentIntent(createNotificationContentPendingIntent())
        .setOngoing(false)
        .build();

    NotificationManager notificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
    notificationManager.notify(NOTIFICATION_ID, notification);
  }

  private void showPlayNotification() {
    if (mNotificationImage != null) {
      showPlayNotification(mNotificationImage);
      return;
    }

    final ImageView imageView = new ImageView(this);
    Picasso.with(this).load(mTopTracksPlaylist.getTrackThumbnailUrl(mTrackIndex)).into(imageView, new Callback() {
      @Override
      public void onSuccess() {
        mNotificationImage = ImageUtils.drawableToBitmap(imageView.getDrawable());
        showPlayNotification(mNotificationImage);
      }

      @Override
      public void onError() {

      }
    });

    showPlayNotification(null);
  }

  private void showPlayNotification(Bitmap bitmap) {
    Notification notification = new NotificationCompat.Builder(this)
        // show controls on lockscreen even when user hides sensitive content.
        .setVisibility(Notification.VISIBILITY_PUBLIC)
        .setSmallIcon(R.drawable.notification_icon)
        .setLargeIcon(bitmap)
            // Add media control buttons that invoke intents in your media service
        .addAction(android.R.drawable.ic_media_previous, "Previous", createNotificationPendingIntent(ACTION_PREVIOUS))
        .addAction(android.R.drawable.ic_media_pause, "Pause", createNotificationPendingIntent(ACTION_PAUSE))
        .addAction(android.R.drawable.ic_media_next, "Next", createNotificationPendingIntent(ACTION_NEXT))
            // Apply the media style template
        .setStyle(new NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(1)
        )
        .setContentTitle(mTopTracksPlaylist.getTrackName(mTrackIndex))
        .setContentText(mTopTracksPlaylist.getArtistName())
        .setSubText(mTopTracksPlaylist.getTrackAlbumName(mTrackIndex))
        .setContentIntent(createNotificationContentPendingIntent())
        .setOngoing(true)
        .build();

    startForeground(NOTIFICATION_ID, notification);
  }

  private PendingIntent createNotificationContentPendingIntent() {
    Intent resultIntent = new Intent(this, MainActivity.class);

    return PendingIntent.getActivity(
        this,
        0,
        resultIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    );
  }

  private PendingIntent createNotificationPendingIntent(String action) {
    Intent resultIntent = new Intent(this, PlaybackService.class);
    resultIntent.setAction(action);

    return PendingIntent.getService(
        this,
        0,
        resultIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    );
  }
}
