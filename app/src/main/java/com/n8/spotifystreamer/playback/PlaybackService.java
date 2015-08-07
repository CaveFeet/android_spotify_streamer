package com.n8.spotifystreamer.playback;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.ImageView;

import com.n8.spotifystreamer.BusProvider;
import com.n8.spotifystreamer.ImageUtils;
import com.n8.spotifystreamer.MainActivity;
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
import com.n8.spotifystreamer.models.TopTracksPlaylist;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class PlaybackService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer
    .OnBufferingUpdateListener, MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnCompletionListener {

  private static final String TAG = PlaybackService.class.getSimpleName();

  public static final String KEY_PLAYLIST = "key_playlist";

  public static final String KEY_TRACK_INDEX = "key_track_index";

  public static final String ACTION_START = "com.n8.spotifystreamer.START";

  public static final String ACTION_PLAY = "com.n8.spotifystreamer.PLAY";

  public static final String ACTION_PAUSE = "com.n8.spotifystreamer.PAUSE";

  public static final String ACTION_STOP = "com.n8.spotifystreamer.STOP";

  public static final String ACTION_NEXT = "com.n8.spotifystreamer.NEXT";

  public static final String ACTION_PREVIOUS = "com.n8.spotifystreamer.PREVIOUS";

  public static final String TAG_WIFI_LOCK = "mylock";

  private static final int NOTIFICATION_ID = 001;

  public static final int PROGRESS_REPORTING_DELAY = 1000;

  private MediaPlayer mMediaPlayer;

  private WifiManager.WifiLock mWifiLock;

  private TopTracksPlaylist mTopTracksPlaylist;

  private int mTrackIndex;

  private Bitmap mNotificationImage;

  private boolean mLockScreenControlsEnabled;

  private BroadcastReceiver mLockScreenReceiver;

  private Thread mProgressMonitorThread;

  @Override
  public void onCreate() {
    super.onCreate();
    BusProvider.getInstance().register(this);

    mLockScreenReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        boolean locked = km.inKeyguardRestrictedInputMode();

        mLockScreenControlsEnabled = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(getString
            (R.string.pref_enable_notification_media_controls_key), false);

        if (locked) {
          if (mMediaPlayer == null) {
            return;
          }

          if (mMediaPlayer.isPlaying()) {
            showPauseNotification(mLockScreenControlsEnabled);
          } else {
            showPlayNotification(mLockScreenControlsEnabled);
          }
        } else {
          if (mMediaPlayer == null) {
            return;
          }

          if (mMediaPlayer.isPlaying()) {
            showPauseNotification(true);
          } else {
            showPlayNotification(true);
          }
        }
      }
    };

    registerReceiver(mLockScreenReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
    registerReceiver(mLockScreenReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
    registerReceiver(mLockScreenReceiver, new IntentFilter(Intent.ACTION_USER_PRESENT));
  }

  @Override
  public void onDestroy() {
    BusProvider.getInstance().unregister(this);
    unregisterReceiver(mLockScreenReceiver);
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
    stop();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(TAG, "onStartCommand");

    if (intent != null ) {
      String action = intent.getAction();
      Log.d(TAG, action);

      if (action != null) {
        if (action.equals(ACTION_START)) {
          handleStartIntent(intent);
        } else if (action.equals(ACTION_PLAY)) {
          play();
        } else if (action.equals(ACTION_PAUSE)) {
          pause();
        } else if (action.equals(ACTION_STOP)) {
          stop();
        } else if (action.equals(ACTION_NEXT)) {
          next();
        } else if (action.equals(ACTION_PREVIOUS)) {
          prev();
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
    complete();
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
    broadcastState();
  }

  @Subscribe
  public void onSeekbarChangedEventReceived(SeekbarChangedEvent event) {
    if (mMediaPlayer != null) {
      mMediaPlayer.seekTo(event.getProgress());
    }
  }

  private void broadcastState() {
    BusProvider.getInstance().post(new PlaybackServiceStateBroadcastEvent(mMediaPlayer, mTopTracksPlaylist, mTrackIndex));
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
    }else{
      mNotificationImage = null;
      mMediaPlayer.reset();
    }

    try {
      String url = mTopTracksPlaylist.getTrackPreviewUrl(mTrackIndex);
      if (url == null) {
        return;
      }

      mMediaPlayer.setDataSource(url);
      mMediaPlayer.prepareAsync();
    } catch (IOException e) {
      Log.d(TAG, "Failed to prepare media playter " + e.getMessage());
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

  private void handleStartIntent(Intent intent) {
    Log.d(TAG, "handleStartIntent()");

    // Check for new playlist info.  If it exists in the bundle, update the service's members
    //
    mTopTracksPlaylist = intent.getParcelableExtra(KEY_PLAYLIST);

    if (mTopTracksPlaylist != null) {
      mTrackIndex = intent.getIntExtra(KEY_TRACK_INDEX, 0);
      mNotificationImage = null;

      initMediaPlayer();
      broadcastState();
    }
  }

  private void play(){
    AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
    int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

    if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
      Log.d(TAG, "Failed to get audio focus");
    } else {
      mWifiLock.acquire();
      showPlayNotification(true);
      mMediaPlayer.start();
      BusProvider.getInstance().post(
          new TrackStartedEvent(
              mTopTracksPlaylist.getTracks().tracks.get(mTrackIndex),
              mMediaPlayer.getDuration()
          )
      );

      mProgressMonitorThread = createProgressMonitorThread();
      mProgressMonitorThread.start();

    }
  }

  private Thread createProgressMonitorThread() {
    return new Thread(){
      @Override
      public void run() {
        while (mMediaPlayer.getCurrentPosition() < mMediaPlayer.getDuration()) {
          if (isInterrupted()) {
            Log.d(TAG, "Interrupted while monitoring progress");
            return;
          }

          BusProvider.getInstance().post(new PlaybackProgressEvent(mMediaPlayer.getCurrentPosition()));
          try {
            sleep(PROGRESS_REPORTING_DELAY);
          } catch (InterruptedException e) {
            Log.d(TAG, "Failed to sleep while monitoring progress. " + e.getMessage());
          }
        }
      }
    };
  }

  private void pause(){
    Log.d(TAG, "pause()");
    if (mWifiLock.isHeld()) {
      mWifiLock.release();
    }

    if (mMediaPlayer.isPlaying()) {
      mMediaPlayer.pause();
      mProgressMonitorThread.interrupt();
      BusProvider.getInstance().post(new TrackPausedEvent());
    }

    stopForeground(false);
    showPauseNotification(true);
  }

  private void next() {
    if (mTrackIndex < mTopTracksPlaylist.size() - 1) {
      pause();
      mTrackIndex++;
      initMediaPlayer();

      BusProvider.getInstance().post(new NextTrackEvent(mTrackIndex));
    }
  }

  private void prev(){
    if (mTrackIndex > 0) {
      pause();
      mTrackIndex--;
      initMediaPlayer();

      BusProvider.getInstance().post(new PrevTrackEvent(mTrackIndex));
    }
  }

  private void complete(){
    Log.d(TAG, "Playback complete");
    if (mWifiLock.isHeld()) {
      mWifiLock.release();
    }

    BusProvider.getInstance().post(new TrackPlaybackCompleteEvent());

    stopForeground(false);
    showPauseNotification(true);
  }

  private void stop() {
    Log.d(TAG, "stop()");

    if (mMediaPlayer != null &&  mMediaPlayer.isPlaying()) {
      mMediaPlayer.stop();

      if (mProgressMonitorThread != null) {
        mProgressMonitorThread.interrupt();
      }
    }

    stopForeground(true);
    cleanupMediaPlayer();
    cleanupWifiLock();

    stopSelf();
  }

  private void showPauseNotification(final boolean showLockScreenControls) {
    if (mNotificationImage != null) {
      showPauseNotification(mNotificationImage, showLockScreenControls);
      return;
    }

    final ImageView imageView = new ImageView(this);
    Picasso.with(this).load(mTopTracksPlaylist.getTrackThumbnailUrl(0, mTrackIndex)).into(imageView, new Callback() {
      @Override
      public void onSuccess() {
        mNotificationImage = ImageUtils.drawableToBitmap(imageView.getDrawable());
        showPauseNotification(mNotificationImage, showLockScreenControls);
      }

      @Override
      public void onError() {

      }
    });

    showPauseNotification(null, showLockScreenControls);
  }

  private void showPauseNotification(Bitmap bitmap, boolean showLockScreenControls){

    NotificationCompat.Builder builder = createBaseNotificationBuilder(bitmap);

    if (showLockScreenControls) {
      // Add media control buttons that invoke intents in your media service
      builder.addAction(android.R.drawable.ic_media_previous, "Previous", createNotificationPendingIntent(ACTION_PREVIOUS))
          .addAction(android.R.drawable.ic_media_play, "Play", createNotificationPendingIntent(ACTION_PLAY))
          .addAction(android.R.drawable.ic_media_next, "Next", createNotificationPendingIntent(ACTION_NEXT))
              // Apply the media style template
          .setStyle(new NotificationCompat.MediaStyle()
                  .setShowActionsInCompactView(1)
          );
    }

    Notification notification = builder.build();

    NotificationManager notificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
    notificationManager.notify(NOTIFICATION_ID, notification);
  }

  private NotificationCompat.Builder createBaseNotificationBuilder(Bitmap bitmap){
    NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

    // show controls on lockscreen even when user hides sensitive content.
    builder.setVisibility(Notification.VISIBILITY_PUBLIC)
    .setSmallIcon(R.drawable.notification_icon)
    .setLargeIcon(bitmap)
    .setContentTitle(mTopTracksPlaylist.getTrackName(mTrackIndex))
    .setContentText(mTopTracksPlaylist.getArtistName())
    .setSubText(mTopTracksPlaylist.getTrackAlbumName(mTrackIndex))
    .setContentIntent(createNotificationContentPendingIntent())
    .setOngoing(false);

    return builder;
  }

  private void showPlayNotification(final boolean showLockScreenControls) {
    if (mNotificationImage != null) {
      showPlayNotification(mNotificationImage, showLockScreenControls);
      return;
    }

    final ImageView imageView = new ImageView(this);
    Picasso.with(this).load(mTopTracksPlaylist.getTrackThumbnailUrl(0, mTrackIndex)).into(imageView, new Callback() {
      @Override
      public void onSuccess() {
        mNotificationImage = ImageUtils.drawableToBitmap(imageView.getDrawable());
        showPlayNotification(mNotificationImage, showLockScreenControls);
      }

      @Override
      public void onError() {

      }
    });

    showPlayNotification(null, showLockScreenControls);
  }

  private void showPlayNotification(Bitmap bitmap, boolean showLockScreenControls) {
    NotificationCompat.Builder builder = createBaseNotificationBuilder(bitmap);

    if (showLockScreenControls) {
      builder
          .addAction(android.R.drawable.ic_media_previous, "Previous", createNotificationPendingIntent(ACTION_PREVIOUS))
          .addAction(android.R.drawable.ic_media_pause, "Pause", createNotificationPendingIntent(ACTION_PAUSE))
          .addAction(android.R.drawable.ic_media_next, "Next", createNotificationPendingIntent(ACTION_NEXT))
              // Apply the media style template
          .setStyle(new NotificationCompat.MediaStyle()
                  .setShowActionsInCompactView(1)
          );
    }

    Notification notification = builder.build();

    startForeground(NOTIFICATION_ID, notification);
    NotificationManager notificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
    notificationManager.notify(NOTIFICATION_ID, notification);
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
