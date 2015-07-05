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

import com.n8.spotifystreamer.MainActivity;
import com.n8.spotifystreamer.R;

import java.io.IOException;

public class PlaybackService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer
    .OnBufferingUpdateListener, MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener {

  private static final String TAG = PlaybackService.class.getSimpleName();

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
  private BroadcastReceiver mMusicIntentReceiver;
  private Intent mIntent;

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onCreate() {
    super.onCreate();

    mMusicIntentReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {

      }
    };
    registerReceiver(mMusicIntentReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
  }

  @Override
  public void onDestroy() {
    if (mMediaPlayer != null) {
      mMediaPlayer.release();
    }
    if (mWifiLock != null) {
      if (mWifiLock.isHeld()) {
        mWifiLock.release();
      }
      mWifiLock = null;
    }
    unregisterReceiver(mMusicIntentReceiver);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(TAG, "onStartCommand");
    mIntent = intent;
    if (mIntent.getAction().equals(ACTION_PLAY)) {
      Log.d(TAG, "Action_Play");
      if (mMediaPlayer == null) {
        initMediaPlayer(mIntent);
      } else {
        play();
      }
    } else if (mIntent.getAction().equals(ACTION_PAUSE)) {
      Log.d(TAG, "Action_Pause");
      pause();
    } else if (mIntent.getAction().equals(ACTION_STOP)) {
      stop();
    }

    return super.onStartCommand(intent, flags, startId);
  }

  @Override
  public void onPrepared(MediaPlayer mp) {
    play();
  }

  @Override
  public void onBufferingUpdate(MediaPlayer mp, int percent) {

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
          initMediaPlayer(mIntent);
        }
        else if (!mMediaPlayer.isPlaying()) mMediaPlayer.start();
        mMediaPlayer.setVolume(1.0f, 1.0f);
        break;

      case AudioManager.AUDIOFOCUS_LOSS:
        // Lost focus for an unbounded amount of time: stop playback and release media player
        if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayer = null;
        break;

      case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
        // Lost focus for a short time, but we have to stop
        // playback. We don't release the media player because playback
        // is likely to resume
        if (mMediaPlayer.isPlaying()) mMediaPlayer.pause();
        break;

      case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
        // Lost focus for a short time, but it's ok to keep playing
        // at an attenuated level
        if (mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(0.1f, 0.1f);
        break;
    }
  }

  private void initMediaPlayer(Intent intent) {
    String url = intent.getStringExtra("url");

    if (mMediaPlayer == null) {
      mMediaPlayer = new MediaPlayer();
      mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
      mMediaPlayer.setOnPreparedListener(this);
      mMediaPlayer.setOnBufferingUpdateListener(this);
      mMediaPlayer.setOnErrorListener(this);

      // Set wake lock to ensue cpu doesn't sleep while playing.
      // Is managed by media player so when music is paused or stopped, the lock is removed
      mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

      // Get a wifi lock that we can set when music is playing
      mWifiLock = ((WifiManager) getSystemService(WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, TAG_WIFI_LOCK);

      try {
        mMediaPlayer.setDataSource(url);
        mMediaPlayer.prepareAsync();
      } catch (IOException e) {
        Log.d(TAG, "Failed to prepare media playter " + e.getMessage());
      }
    }
  }

  private void play(){
    AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
    int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
        AudioManager.AUDIOFOCUS_GAIN);

    if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
      Log.d(TAG, "Failed to get audio focus");
    } else {
      mWifiLock.acquire();
      showPlayNotification();
      mMediaPlayer.start();
    }
  }

  private void pause(){
    if (mWifiLock.isHeld()) {
      mWifiLock.release();
    }
    stopForeground(false);
    mMediaPlayer.pause();
    showPauseNotification();
  }

  private void stop() {
    if (mWifiLock.isHeld()) {
      mWifiLock.release();
    }
    stopForeground(true);
    mMediaPlayer.stop();
    mMediaPlayer.release();
    mMediaPlayer = null;

    stopSelf();
  }

  private void showPauseNotification(){

    Notification notification = new NotificationCompat.Builder(this)
        // show controls on lockscreen even when user hides sensitive content.
        .setVisibility(Notification.VISIBILITY_PUBLIC)
        .setSmallIcon(R.mipmap.ic_launcher)
            // Add media control buttons that invoke intents in your media service
        .addAction(android.R.drawable.ic_media_previous, "Previous", null) // #0
        .addAction(android.R.drawable.ic_media_play, "Play", createNotificationPlayPendingIntent())  // #1
        .addAction(android.R.drawable.ic_media_next, "Next", null)     // #2
            // Apply the media style template
        .setStyle(new NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(1 /* #1: pause button */)
        )
        .setContentTitle("Wonderful music")
        .setContentText("My Awesome Band")
        .setContentIntent(createNotificationContentPendingIntent())
        .setOngoing(false)
        .build();

    startForeground(NOTIFICATION_ID, notification);
  }

  private void showPlayNotification(){

    Notification notification = new NotificationCompat.Builder(this)
        // show controls on lockscreen even when user hides sensitive content.
        .setVisibility(Notification.VISIBILITY_PUBLIC)
        .setSmallIcon(R.mipmap.ic_launcher)
            // Add media control buttons that invoke intents in your media service
        .addAction(android.R.drawable.ic_media_previous, "Previous", null) // #0
        .addAction(android.R.drawable.ic_media_pause, "Pause", createNotificationPausePendingIntent())  // #1
        .addAction(android.R.drawable.ic_media_next, "Next", null)     // #2
            // Apply the media style template
        .setStyle(new NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(1 /* #1: pause button */)
        )
        .setContentTitle("Wonderful music")
        .setContentText("My Awesome Band")
        .setContentIntent(createNotificationContentPendingIntent())
        .setOngoing(true)
        .build();

    startForeground(NOTIFICATION_ID, notification);
  }

  private PendingIntent createNotificationContentPendingIntent() {
    Intent resultIntent = new Intent(this, MainActivity.class);

// Because clicking the notification opens a new ("special") activity, there's
// no need to create an artificial back stack.
    return PendingIntent.getActivity(
        this,
        0,
        resultIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    );
  }

  private PendingIntent createNotificationPausePendingIntent(){
    Intent resultIntent = new Intent(this, PlaybackService.class);
    resultIntent.setAction(ACTION_PAUSE);

// Because clicking the notification opens a new ("special") activity, there's
// no need to create an artificial back stack.
    return PendingIntent.getService(
        this,
        0,
        resultIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    );
  }

  private PendingIntent createNotificationPlayPendingIntent(){
    Intent resultIntent = new Intent(this, PlaybackService.class);
    resultIntent.setAction(ACTION_PLAY);

// Because clicking the notification opens a new ("special") activity, there's
// no need to create an artificial back stack.
    return PendingIntent.getService(
        this,
        0,
        resultIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    );
  }
}
