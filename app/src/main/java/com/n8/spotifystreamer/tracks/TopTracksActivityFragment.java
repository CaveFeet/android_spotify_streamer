package com.n8.spotifystreamer.tracks;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.n8.spotifystreamer.AndroidUtils;
import com.n8.spotifystreamer.DividerItemDecoration;
import com.n8.spotifystreamer.ImageUtils;
import com.n8.spotifystreamer.R;
import com.n8.spotifystreamer.SpotifyStreamerApplication;
import com.squareup.okhttp.Call;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
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

  public static final int THUMBNAIL_IMAGE_SIZE = 200;

  @InjectView(R.id.fragment_top_tracks_toolbar)
  Toolbar mToolbar;

  @InjectView(R.id.fragment_top_tracks_collapsingToolbarLayout)
  CollapsingToolbarLayout mCollapsingToolbarLayout;

  @InjectView(R.id.fragment_top_tracks_recyclerView)
  RecyclerView mTopTracksRecyclerView;

  @InjectView(R.id.fragment_top_tracks_artist_image_thumbnail)
  ImageView mArtistThumbnailImageView;

  @InjectView(R.id.fragment_top_tracks_artist_image_header_background)
  ImageView mArtistHeaderBackgroundImageView;

  private Artist mArtist;

  private List<Track> mTracks;

  private AnimatorSet mAnimatorSet;

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
  public void onDetach() {
    super.onDetach();
    if (mAnimatorSet != null) {
      mAnimatorSet.end();
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_top_tracks, container, false);
    ButterKnife.inject(this, view);

    mToolbar.setNavigationIcon(R.drawable.ic_menu_back);
    mToolbar.getNavigationIcon().setColorFilter(getResources().getColor(android.R.color.white),
        PorterDuff.Mode.SRC_ATOP);
    mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        getActivity().onBackPressed();
      }
    });
    mToolbar.setSubtitle(R.string.top_ten_tracks);

    mTopTracksRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
    mTopTracksRecyclerView.setLayoutManager(getLinearLayoutManager());
    mTopTracksRecyclerView.setHasFixedSize(true);

    mCollapsingToolbarLayout.setTitle(mArtist.name);
    mCollapsingToolbarLayout.setCollapsedTitleTextColor(getResources().getColor(android.R.color.black));
    mCollapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.white));

    // Load artist thumbnail into thumbnail view in the collapsing toolbar header
    //
    List<Image> images = mArtist.images;
    if (images != null && images.size() > 0) {
      int index = ImageUtils.getIndexOfClosestSizeImage(images, THUMBNAIL_IMAGE_SIZE);
      Picasso.with(getActivity()).load(images.get(index).url).into(mArtistThumbnailImageView);
    }

    // If view is being recreated after a rotation, there may be existing artist data to view
    if (mTracks != null) {
      bindTracks();
      return view;
    }

    Map<String, Object> map = new HashMap<>();
    map.put("country", Locale.getDefault().getCountry());
    final Handler handler = new Handler();
    SpotifyStreamerApplication
        .getSpotifyService().getArtistTopTrack(mArtist.id, map, new Callback<Tracks>() {
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

  private void setupHeaderImages(final List<Image> images, final int index) {
    // Preload the next image to avoid any delay
    if (index != images.size()-1) {
      Picasso.with(getActivity()).load(images.get(index + 1).url);
    }

    Picasso.with(getActivity()).load(images.get(index).url)
        .into(mArtistHeaderBackgroundImageView, new com.squareup.picasso.Callback() {
          @Override
          public void onSuccess() {
            mArtistHeaderBackgroundImageView.setAlpha(.35f);

            long duration = 7000;
            float transStartX;
            float transStartY;
            float transEndX;
            float transEndY;
            float scaleStart;
            float scaleEnd;

            int state = index % 4;
            if (state == 0) {
              transStartX = -50;
              transEndX = 50;
              transStartY = -50;
              transEndY = 50;
              scaleStart = 1.5f;
              scaleEnd = 1.75f;
            }else if (state == 1) {
              transStartX = 50;
              transEndX = 50;
              transStartY = 50;
              transEndY = -50;
              scaleStart = 1.75f;
              scaleEnd = 1.5f;
            }else if (state == 2) {
              transStartX = 50;
              transEndX = -50;
              transStartY = -50;
              transEndY = 50;
              scaleStart = 1.5f;
              scaleEnd = 1.75f;
            } else {
              transStartX = -50;
              transEndX = -50;
              transStartY = 50;
              transEndY = -50;
              scaleStart = 1.75f;
              scaleEnd = 1.5f;

            }

            ObjectAnimator transX = ObjectAnimator
                .ofFloat(mArtistHeaderBackgroundImageView, "translationX", transStartX, transEndX)
                .setDuration(duration);
            ObjectAnimator transY = ObjectAnimator
                .ofFloat(mArtistHeaderBackgroundImageView, "translationY", transStartY, transEndY)
                .setDuration(duration);
            ObjectAnimator scaleX = ObjectAnimator
                .ofFloat(mArtistHeaderBackgroundImageView, "scaleX", scaleStart, scaleEnd).setDuration(
                    duration);
            ObjectAnimator scaleY = ObjectAnimator
                .ofFloat(mArtistHeaderBackgroundImageView, "scaleY", scaleStart, scaleEnd)
                .setDuration(duration);

            mAnimatorSet = new AnimatorSet();
            mAnimatorSet.playTogether(transX, transY, scaleX, scaleY);
            mAnimatorSet.addListener(new Animator.AnimatorListener() {
              @Override
              public void onAnimationStart(Animator animation) {
              }

              @Override
              public void onAnimationEnd(Animator animation) {
                int newindex = index == (images.size() - 1) ? 0 : (index + 1);
                setupHeaderImages(images, newindex);

              }

              @Override
              public void onAnimationCancel(Animator animation) {
              }

              @Override
              public void onAnimationRepeat(Animator animation) {
              }
            });
            mAnimatorSet.start();
          }

          @Override
          public void onError() {
          }
        });
  }

  @NonNull
  private LinearLayoutManager getLinearLayoutManager() {
    return new LinearLayoutManager(getActivity());
  }

  private void bindTracks() {
    mTopTracksRecyclerView.setAdapter(new TracksRecyclerAdapter(mTracks));

    // Sets up the collapsing toolbar header to display the artist's top track images
    //
    List<Image> trackImages = new ArrayList<>();
    for (Track track : mTracks) {
      List<Image> imgs = track.album.images;
      if (imgs != null && imgs.size() > 0) {
        trackImages.add(imgs.get(0));
      }
    }
    if (!trackImages.isEmpty()) {
      setupHeaderImages(trackImages, 0);
    }
  }
}
