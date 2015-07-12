package com.n8.spotifystreamer.events;

import android.support.annotation.NonNull;

public class CountryCodeSettingChangedEvent {

  private String mCountryCode;

  public CountryCodeSettingChangedEvent(@NonNull String countryCode) {
    mCountryCode = countryCode;
  }

  public String getCountryCode() {
    return mCountryCode;
  }
}
