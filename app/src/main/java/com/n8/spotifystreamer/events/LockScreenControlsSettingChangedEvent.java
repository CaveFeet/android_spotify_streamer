package com.n8.spotifystreamer.events;

public class LockScreenControlsSettingChangedEvent {
  private boolean mLockScreenControlsEnabled;

  public LockScreenControlsSettingChangedEvent(boolean lockScreenControlsEnabled) {
    mLockScreenControlsEnabled = lockScreenControlsEnabled;
  }

  public boolean isLockScreenControlsEnabled() {
    return mLockScreenControlsEnabled;
  }
}
