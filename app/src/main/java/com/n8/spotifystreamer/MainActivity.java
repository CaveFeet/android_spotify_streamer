package com.n8.spotifystreamer;

import android.app.SearchManager;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity implements ArtistSearchFragment.OnFragmentInteractionListener {

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

        processIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    @Override
    public void onArtistSelected() {
        // no op
    }

    private void processIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (query == null) {
                return;
            }

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            spotify.searchArtists(query, new Callback<ArtistsPager>() {
                @Override
                public void success(ArtistsPager artistsPager, Response response) {
                    BusProvider.getInstance().post(new ArtistSearchCompletedEvent(artistsPager));
                }

                @Override
                public void failure(RetrofitError error) {
                    AndroidUtils.showToast(MainActivity.this, getString(R.string.error_searching_for_artist));
                }
            });
        }
    }
}
