package com.n8.spotifystreamer.tracks;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.n8.n8droid.AndroidUtils;
import com.n8.n8droid.BaseViewControllerFragment;
import com.n8.spotifystreamer.BusProvider;
import com.n8.spotifystreamer.ImageUtils;
import com.n8.spotifystreamer.R;
import com.n8.spotifystreamer.SettingsActivity;
import com.n8.spotifystreamer.SpotifyStreamerApplication;
import com.n8.spotifystreamer.events.ArtistClickedEvent;
import com.n8.spotifystreamer.events.CountryCodeSettingChangedEvent;
import com.n8.spotifystreamer.events.TrackClickedEvent;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Fragment that displays the top ten spotify tracks for an artist.
 */
public class TopTracksFragment extends BaseViewControllerFragment<TopTracksFragmentView> implements TopTracksController,
    TracksRecyclerAdapter.TrackClickListener {

  private static final String TAG = TopTracksFragment.class.getSimpleName();

  public static final int THUMBNAIL_IMAGE_SIZE = 200;

  private Artist mArtist;

  private List<Track> mTracks;

  private AnimatorSet mAnimatorSet;

  private TracksRecyclerAdapter mAdapter;

  private String mCountryCode;

  private Track mCurrentTrack;

  public static TopTracksFragment getInstance(Artist artist) {
    TopTracksFragment fragment = new TopTracksFragment();
    fragment.mArtist = artist;

    return fragment;
  }

  public TopTracksFragment() { }


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
  }

  @Override
  protected int getLayoutId() {
    return R.layout.fragment_top_tracks;
  }

  @Override
  protected void setViewController() {
    mView.setController(getActivity(), this);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    BusProvider.getInstance().register(this);

    super.onCreateView(inflater, container, savedInstanceState);

    mCountryCode = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getString(R.string
        .pref_country_code_key), Locale.getDefault().getCountry());

    bindArtist();

    return mView;
  }

  @Override
  public void onDetach() {
    super.onDetach();
    BusProvider.getInstance().unregister(this);
    if (mAnimatorSet != null) {
      mAnimatorSet.end();
    }
  }

  @Override
  public void onShareClicked() {
    startActivity(createShareIntent());
  }

  @Override
  public void onNavIconClicked() {
    getActivity().onBackPressed();
  }

  @Override
  public void onSettingsMenuOptionClicked() {
    Intent intent = new Intent(getActivity(), SettingsActivity.class);
    startActivity(intent);
  }

  @Override
  public LinearLayoutManager getLinearLayoutManager() {
    return new LinearLayoutManager(getActivity());
  }

  @Override
  public String getArtistName() {
    if (mArtist != null) {
      return mArtist.name;
    }
    return null;
  }

  @Subscribe
  public void onArtistClicked(ArtistClickedEvent event) {
    mArtist = event.mArtist;
    mTracks = null;
    bindArtist();
  }

  @Subscribe
  public void onCountryCodeSettingChangeEventReceived(CountryCodeSettingChangedEvent event) {
    mCountryCode = event.getCountryCode();
    requestTopTracks();
  }

  @Override
  public void onTrackViewClicked(Track track) {
    mCurrentTrack = track;
    BusProvider.getInstance().post(new TrackClickedEvent(mArtist, mTracks, track));
  }

  private void bindArtist() {
    if (mArtist == null) {
      mView.getNoContentView().setVisibility(View.VISIBLE);
      return;
    }

    mView.getNoContentView().setVisibility(View.GONE);

    mView.getCollapsingToolbarLayout().setTitle(mArtist.name);

    // Load artist thumbnail into thumbnail view in the collapsing toolbar header
    //
    List<Image> images = mArtist.images;
    if (images != null && images.size() > 0) {
      int index = ImageUtils.getIndexOfClosestSizeImage(images, THUMBNAIL_IMAGE_SIZE);
      Picasso.with(getActivity()).load(images.get(index).url).into(mView.getArtistThumbnailImageView());
    } else {
      Picasso.with(getActivity()).load(R.drawable.ic_artist_placeholder_light).into(mView.getArtistThumbnailImageView());
    }

    // If view is being recreated after a rotation, there may be existing artist data to view
    if (mTracks != null) {
      bindTracks(false);
      updateContentViews();
      setupToolbarHeader();
      return;
    }

    requestTopTracks();

    mView.invalidate();
  }

  private void bindTracks(boolean startFromScratch) {
    if (startFromScratch) {
      mAdapter = new TracksRecyclerAdapter(mTracks, this);
    }
    mView.getTopTracksRecyclerView().setAdapter(mAdapter);
  }

  private void setupToolbarHeader() {
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
    } else {
      Picasso.with(getActivity()).load(R.drawable.header_background)
          .into(mView.getArtistHeaderBackgroundImageView());
    }
  }

  private void updateContentViews() {
    if (mTracks != null && mTracks.size() > 0) {
      mView.getTopTracksRecyclerView().setVisibility(View.VISIBLE);
      mView.getNoContentView().setVisibility(View.GONE);
    } else {
      mView.getTopTracksRecyclerView().setVisibility(View.GONE);
      mView.getNoContentView().setVisibility(View.VISIBLE);
    }
  }

  private void requestTopTracks() {
    final Map<String, Object> map = new HashMap<>();

    map.put("country", mCountryCode);

    final Handler handler = new Handler();
    SpotifyStreamerApplication
        .getSpotifyService().getArtistTopTrack(mArtist.id, map, new Callback<Tracks>() {
      @Override
      public void success(Tracks tracks, Response response) {
        mTracks = tracks.tracks;
        handler.post(new Runnable() {
          @Override
          public void run() {
            bindTracks(true);
            updateContentViews();
            setupToolbarHeader();
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
  }

  /**
   * Sets up animation of track images that plays in the expanded toolbar header.
   *
   * @param images
   * @param index
   */
  private void setupHeaderImages(final List<Image> images, final int index) {
    // Preload the next image to avoid any delay
    if (index != images.size() - 1) {
      Picasso.with(getActivity()).load(images.get(index + 1).url);
    }

    Picasso.with(getActivity()).load(images.get(index).url)
        .into(mView.getArtistHeaderBackgroundImageView(), new com.squareup.picasso.Callback() {
          @Override
          public void onSuccess() {
            mView.getArtistHeaderBackgroundImageView().setAlpha(.35f);

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
            } else if (state == 1) {
              transStartX = 50;
              transEndX = 50;
              transStartY = 50;
              transEndY = -50;
              scaleStart = 1.75f;
              scaleEnd = 1.5f;
            } else if (state == 2) {
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
                .ofFloat(mView.getArtistHeaderBackgroundImageView(), "translationX", transStartX, transEndX)
                .setDuration(duration);
            ObjectAnimator transY = ObjectAnimator
                .ofFloat(mView.getArtistHeaderBackgroundImageView(), "translationY", transStartY, transEndY)
                .setDuration(duration);
            ObjectAnimator scaleX = ObjectAnimator
                .ofFloat(mView.getArtistHeaderBackgroundImageView(), "scaleX", scaleStart, scaleEnd).setDuration(
                    duration);
            ObjectAnimator scaleY = ObjectAnimator
                .ofFloat(mView.getArtistHeaderBackgroundImageView(), "scaleY", scaleStart, scaleEnd)
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

  private Intent createShareIntent() {
    Intent shareIntent = new Intent(Intent.ACTION_SEND);
    shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
    shareIntent.setType("text/plain");
    shareIntent.putExtra(Intent.EXTRA_TEXT, mArtist.name + "  -  " + mArtist.external_urls.get("spotify"));
    return shareIntent;
  }
}
