package com.n8.spotifystreamer.events;

import android.media.MediaPlayer;

import com.n8.spotifystreamer.models.ParcelableTrack;
import com.n8.spotifystreamer.models.ParcelableTracks;
import com.n8.spotifystreamer.models.TopTracksPlaylist;

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Indicates the current state of the playback service.
 */
public class PlaybackServiceStateBroadcastEvent {

  private MediaPlayer mMediaPlayer;

  private TopTracksPlaylist mPlaylist;

  private int mCurrentTrackIndex;

  public PlaybackServiceStateBroadcastEvent(MediaPlayer mediaPlayer, TopTracksPlaylist playlist, int currentIndex) {
    mMediaPlayer = mediaPlayer;
    mPlaylist = playlist;
    mCurrentTrackIndex = currentIndex;
  }

  public boolean isPlaying(){
    return mMediaPlayer != null && mMediaPlayer.isPlaying();
  }

  public ParcelableTracks getTracks(){
    return mPlaylist.getTracks();
  }

  public ParcelableTrack getCurrentTrack() {
    return getTracks().tracks.get(mCurrentTrackIndex);
  }
}
