package com.n8.spotifystreamer.events;

import android.support.annotation.NonNull;

/**
 * Indicates that the user has changed the country code setting that determines which country code to pass in to
 * search requests.
 */
public class CountryCodeSettingChangedEvent {

  private String mCountryCode;

  public CountryCodeSettingChangedEvent(@NonNull String countryCode) {
    mCountryCode = countryCode;
  }

  public String getCountryCode() {
    return mCountryCode;
  }
}
