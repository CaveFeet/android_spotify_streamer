package com.n8.spotifystreamer.artists;

import android.support.v7.widget.RecyclerView;
import com.n8.n8droid.viewcontroller.ViewController;

public interface ArtistSearchController extends ViewController {

  void onNowPlayingMenuOptionClicked();

  void onSettingsMenuOptionClicked();

  void onClearSuggestions();

  void onSubmitQuery(String query);

  void onRecyclerViewScrolled(RecyclerView recyclerView, int dx, int dy);
}
