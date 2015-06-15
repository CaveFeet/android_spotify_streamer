package com.n8.spotifystreamer;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.n8.spotifystreamer.artists.ArtistSearchFragment;
import com.n8.spotifystreamer.artists.ArtistSuggestionProvider;
import com.n8.spotifystreamer.coachmarks.CoachmarkFragment;
import com.n8.spotifystreamer.events.CoachmarkShowAgainEvent;
import com.n8.spotifystreamer.events.CoachmarksDoneEvent;
import com.n8.spotifystreamer.events.SearchIntentReceivedEvent;
import com.squareup.otto.Subscribe;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String COACHMARK_FRAGMENT_TAG = "coachmark_fragment_tag";

    private static final String ARTIST_FRAGMENT_TAG = "artist_fragment_tag";

    private static final String PREFS_COACHMARK_KEY = "prefs_key_coachmarks";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            SharedPreferences sharedPreferences = getSharedPreferences();
            boolean showCoachmarks = sharedPreferences.getBoolean(PREFS_COACHMARK_KEY, true);

            if (showCoachmarks) {
                showFragment(new CoachmarkFragment(), COACHMARK_FRAGMENT_TAG);
                return;
            }

            // If an instance of the ArtistSearchFragment doesn't already exist, create one and show it
            // in the Activity's layout.  Adding the fragment in this way will retain the fragment
            // across Activity recreation.
            //
            showFragment(new ArtistSearchFragment(), ARTIST_FRAGMENT_TAG);
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

        showFragment(new ArtistSearchFragment(), ARTIST_FRAGMENT_TAG);
    }

    @Subscribe
    public void onCoachmarkShowAgainButtonChecked(CoachmarkShowAgainEvent event) {
        getSharedPreferences().edit().putBoolean(PREFS_COACHMARK_KEY, event.isShowCoachmarksAgain()).apply();
    }

    private void showFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if(fragmentManager.findFragmentByTag(tag) == null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.main_activity_fragment_frame,
                    fragment,
                    tag)
                    .commit();
        }
    }

    private SharedPreferences getSharedPreferences() {
        return getSharedPreferences(MainActivity.class.getSimpleName(), MODE_PRIVATE);
    }
}
