package com.n8.spotifystreamer.events;

/**
 * Indicates that the setting for showing lock-screen controls has been changed.
 */
public class LockScreenControlsSettingChangedEvent {

  private boolean mLockScreenControlsEnabled;

  public LockScreenControlsSettingChangedEvent(boolean lockScreenControlsEnabled) {
    mLockScreenControlsEnabled = lockScreenControlsEnabled;
  }

  public boolean isLockScreenControlsEnabled() {
    return mLockScreenControlsEnabled;
  }
}
