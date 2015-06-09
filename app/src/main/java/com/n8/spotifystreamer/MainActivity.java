package com.n8.spotifystreamer;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.n8.spotifystreamer.artists.ArtistSearchFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String ARTIST_FRAGMENT_TAG = "artist_fragment_tag";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // If an instance of the ArtistSearchFragment doesn't already exist, create one and show it
        // in the Activity's layout.  Adding the fragment in this way will retain the fragment
        // across Activity recreation.
        //
        FragmentManager fragmentManager = getSupportFragmentManager();
        if(fragmentManager.findFragmentByTag(ARTIST_FRAGMENT_TAG) == null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.main_activity_fragment_frame,
                new ArtistSearchFragment(),
                ARTIST_FRAGMENT_TAG)
                .commit();
        }
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
}
