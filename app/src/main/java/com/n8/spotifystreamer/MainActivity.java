package com.n8.spotifystreamer;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.n8.spotifystreamer.artists.ArtistSearchFragment;
import com.n8.spotifystreamer.artists.ArtistSuggestionProvider;
import com.n8.spotifystreamer.coachmarks.CoachmarkFragment;
import com.n8.spotifystreamer.events.ArtistClickedEvent;
import com.n8.spotifystreamer.events.CoachmarkShowAgainEvent;
import com.n8.spotifystreamer.events.CoachmarksDoneEvent;
import com.n8.spotifystreamer.events.SearchIntentReceivedEvent;
import com.n8.spotifystreamer.events.TrackClickedEvent;
import com.n8.spotifystreamer.playback.PlaybackFragment;
import com.n8.spotifystreamer.playback.PlaybackService;
import com.n8.spotifystreamer.playback.TopTracksPlaylist;
import com.n8.spotifystreamer.tracks.TopTracksFragment;
import com.squareup.otto.Subscribe;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String COACHMARK_FRAGMENT_TAG = "coachmark_fragment_tag";

    private static final String ARTIST_FRAGMENT_TAG = "artist_fragment_tag";

    private static final String TRACK_FRAGMENT_TAG = "track_fragment_tag";

    public static final String PLAYBACK_FRAGMENT_TAG = "playback_fragment_tag";

    private static final String PREFS_COACHMARK_KEY = "prefs_key_coachmarks";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences();
        boolean showCoachmarks = sharedPreferences.getBoolean(PREFS_COACHMARK_KEY, true);

        if (showCoachmarks) {
            CoachmarkFragment coachmarkFragment = new CoachmarkFragment();
            showFragment(R.id.main_activity_coachmark_frame, coachmarkFragment, COACHMARK_FRAGMENT_TAG);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            BusProvider.getInstance().post(new SearchIntentReceivedEvent(query));
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        BusProvider.getInstance().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onCoachmarksDone(CoachmarksDoneEvent event) {
        FragmentManager fragmentmanager = getSupportFragmentManager();
        Fragment coachmarkFragment = fragmentmanager.findFragmentByTag(COACHMARK_FRAGMENT_TAG);

        fragmentmanager.beginTransaction().remove(coachmarkFragment).commit();
    }

    @Subscribe
    public void onCoachmarkShowAgainButtonChecked(CoachmarkShowAgainEvent event) {
        getSharedPreferences().edit().putBoolean(PREFS_COACHMARK_KEY, event.isShowCoachmarksAgain()).apply();
    }

    @Subscribe
    public void onArtistClicked(ArtistClickedEvent event) {
        if (getSupportFragmentManager().findFragmentById(R.id.top_tracks_fragment) == null) {
            showTopTracksFragment(event);
        }
    }

    private void showFragment(@IdRes int layoutId, Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if(fragmentManager.findFragmentByTag(tag) == null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(layoutId,
                fragment,
                tag)
                .commit();
        }
    }

    private SharedPreferences getSharedPreferences() {
        return getSharedPreferences(MainActivity.class.getSimpleName(), MODE_PRIVATE);
    }

    private void showTopTracksFragment(ArtistClickedEvent event) {
        // If using api 22 or better, use a shared element transition.  For some reason api 21
        // devices are displaying some odd behavior with the transition element.
        //
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Fragment fragment = TopTracksFragment.getInstance(event.mArtist);
            fragment.setSharedElementEnterTransition(TransitionInflater.from(this)
                .inflateTransition(R.transition.artists_to_tracks_transition));
            fragment.setSharedElementReturnTransition(TransitionInflater.from(this)
                .inflateTransition(R.transition.artists_to_tracks_transition));

            // Add Fragment B
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_activity_fragment_frame, fragment, TRACK_FRAGMENT_TAG)
                .addToBackStack(null)
                .addSharedElement(event.mThumbnailView, getString(R.string.artist_thumbnail_transition_name));
            ft.commit();

        } else {
            Fragment fragment = TopTracksFragment.getInstance(event.mArtist);

            // Add Fragment B
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_activity_fragment_frame, fragment, TRACK_FRAGMENT_TAG)
                .addToBackStack(null);
            ft.commit();
        }
    }

    @Subscribe
    public void onTrackClicked(TrackClickedEvent event) {

        TopTracksPlaylist playlist = new TopTracksPlaylist(event.getArtist(), event.getTracks());

        Intent playbackIntent = new Intent(this, PlaybackService.class);
        playbackIntent.setAction(PlaybackService.ACTION_PLAY);
        playbackIntent.putExtra(PlaybackService.KEY_PLAYLIST, playlist);
        playbackIntent.putExtra(PlaybackService.KEY_TRACK_INDEX, event.getTracks().indexOf(event.getClickedTrack()));

        startService(playbackIntent);

        PlaybackFragment playbackFragment = null;

        playbackFragment = (PlaybackFragment) getSupportFragmentManager().findFragmentByTag(PLAYBACK_FRAGMENT_TAG);
        if (playbackFragment == null) {
            // Show the playback fragment to interact with the media controls
            playbackFragment = PlaybackFragment.getInstance(event.getTracks(), event.getClickedTrack());
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_activity_playback_frame, playbackFragment, PLAYBACK_FRAGMENT_TAG)
                .addToBackStack(null).commit();
        } else {
            // Update playback fragment info
            playbackFragment.setPlaybackInfo(event.getTracks(), event.getClickedTrack());
        }
    }
}
