package com.n8.spotifystreamer.artists;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Handler;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.n8.spotifystreamer.AndroidUtils;
import com.n8.spotifystreamer.BusProvider;
import com.n8.spotifystreamer.R;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ArtistSearchActivity extends AppCompatActivity {

    private static final String TAG = ArtistSearchActivity.class.getSimpleName();

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void processIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (query == null) {
                return;
            }

            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                ArtistSuggestionProvider.AUTHORITY, ArtistSuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);

            //TODO enable history clearing
//            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
//                HelloSuggestionProvider.AUTHORITY, HelloSuggestionProvider.MODE);
//            suggestions.clearHistory();

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Searching for " + query);
            progressDialog.show();

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            final Handler handler = new Handler();
            spotify.searchArtists(query, new Callback<ArtistsPager>() {
                @Override
                public void success(ArtistsPager artistsPager, Response response) {
                    progressDialog.cancel();
                    BusProvider.getInstance().post(new ArtistSearchCompletedEvent(artistsPager));
                }

                @Override
                public void failure(RetrofitError error) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();
                            AndroidUtils.showToast(ArtistSearchActivity.this,
                                getString(R.string.error_searching_for_artist));
                        }
                    });
                }
            });
        }
    }
}
