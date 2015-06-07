package com.n8.spotifystreamer.tracks;

import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.n8.spotifystreamer.AndroidUtils;
import com.n8.spotifystreamer.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class TopTracksActivityFragment extends Fragment {

  private static final String TAG = TopTracksActivityFragment.class.getSimpleName();

  @InjectView(R.id.fragment_top_tracks_toolbar)
  Toolbar mToolbar;

  @InjectView(R.id.fragment_top_tracks_collapsingToolbarLayout)
  CollapsingToolbarLayout mCollapsingToolbarLayout;

  @InjectView(R.id.fragment_top_tracks_recyclerView)
  RecyclerView mTopTracksRecyclerView;

  @InjectView(R.id.fragment_top_tracks_artist_image)
  ImageView mArtistImageView;

  private Artist mArtist;

  private List<Track> mTracks;

  public static TopTracksActivityFragment getInstance(Artist artist) {
    TopTracksActivityFragment fragment = new TopTracksActivityFragment();
    fragment.mArtist = artist;

    return fragment;
  }

  public TopTracksActivityFragment() { }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_top_tracks, container, false);
    ButterKnife.inject(this, view);

    AppCompatActivity activity = (AppCompatActivity)getActivity();
    activity.setSupportActionBar(mToolbar);
    activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    mToolbar.getNavigationIcon().setColorFilter(getResources().getColor(android.R.color.white),
        PorterDuff.Mode.SRC_ATOP);

    mTopTracksRecyclerView.setLayoutManager(getLinearLayoutManager());
    mTopTracksRecyclerView.setHasFixedSize(true);

    mCollapsingToolbarLayout.setTitle(mArtist.name);
    mCollapsingToolbarLayout.setCollapsedTitleTextColor(getResources().getColor(android.R.color.black));
    mCollapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.white));

    // If view is being recreated after a rotation, there may be existing artist data to view
    if (mTracks != null) {
      bindTracks();
    }

    List<Image> images = mArtist.images;
    if (images != null && images.size() > 0) {
      Picasso.with(view.getContext()).load(images.get(0).url).into(mArtistImageView);
    }

    SpotifyApi api = new SpotifyApi();
    SpotifyService spotify = api.getService();
    Map<String, Object> map = new HashMap<>();
    map.put("country", Locale.getDefault().getCountry());
    final Handler handler = new Handler();
    spotify.getArtistTopTrack(mArtist.id, map, new Callback<Tracks>() {
      @Override
      public void success(Tracks tracks, Response response) {
        mTracks = tracks.tracks;
        handler.post(new Runnable() {
          @Override
          public void run() {
            bindTracks();
          }
        });
      }

      @Override
      public void failure(final RetrofitError error) {
        handler.post(new Runnable() {
          @Override
          public void run() {
            AndroidUtils.showToast(getActivity(), error.getLocalizedMessage());
          }
        });
      }
    });

    return view;
  }

  @NonNull
  private LinearLayoutManager getLinearLayoutManager() {
    return new LinearLayoutManager(getActivity());
  }

  private void bindTracks() {
    mTopTracksRecyclerView.setAdapter(new TracksRecyclerAdapter(mTracks));
  }
}
