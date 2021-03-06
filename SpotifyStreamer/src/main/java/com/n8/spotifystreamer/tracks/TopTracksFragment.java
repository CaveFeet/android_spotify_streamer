package com.n8.spotifystreamer.tracks;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.n8.n8droid.util.UiUtils;
import com.n8.n8droid.viewcontroller.BaseViewControllerFragment;
import com.n8.spotifystreamer.BusProvider;
import com.n8.spotifystreamer.ImageUtils;
import com.n8.spotifystreamer.R;
import com.n8.spotifystreamer.SettingsActivity;
import com.n8.spotifystreamer.SpotifyStreamerApplication;
import com.n8.spotifystreamer.events.ArtistClickedEvent;
import com.n8.spotifystreamer.events.CountryCodeSettingChangedEvent;
import com.n8.spotifystreamer.events.PlayAllEvent;
import com.n8.spotifystreamer.events.ShowPlaybackFragmentEvent;
import com.n8.spotifystreamer.events.TrackClickedEvent;
import com.n8.spotifystreamer.models.ParcelableArtist;
import com.n8.spotifystreamer.models.ParcelableImage;
import com.n8.spotifystreamer.models.ParcelableTrack;
import com.n8.spotifystreamer.models.ParcelableTracks;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

  private ParcelableArtist mArtist;

  private ParcelableTracks mTracks;

  private AnimatorSet mAnimatorSet;

  private TracksRecyclerAdapter mAdapter;

  private String mCountryCode;

  public static TopTracksFragment getInstance(ParcelableArtist artist) {
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

    mView.getToolbar().inflateMenu(R.menu.settings_menu);
    mView.getToolbar().inflateMenu(R.menu.share_menu);
    mView.getToolbar().setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
          case R.id.main_menu_settings:
            onSettingsMenuOptionClicked();
            return true;
          case R.id.action_share:
            onShareClicked();
            return true;
          case R.id.main_menu_now_playing:
            onNowPlayingMenuOptionClicked();
            return true;
          default:
        }
        return false;
      }
    });
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
  public void onPlayAllClicked() {
    playAllTracks();
  }

  @Override
  public void onNavIconClicked() {
    getActivity().onBackPressed();
  }

  @Override
  public RecyclerView.LayoutManager getLayoutManager() {
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
  public void onTrackViewClicked(ParcelableTrack track) {
    playTrack(track, false);
  }

  @Override
  public void onOverflowClicked(View view, final ParcelableTrack track) {
    PopupMenu overflowMenu = new PopupMenu(getActivity(), view);
    overflowMenu.inflate(R.menu.track_overflow_menu);

    overflowMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
          case R.id.track_overflow_play:
            playTrack(track, false);
            return true;
          case R.id.track_overflow_play_in_dialog:
            playTrack(track, true);
            return true;
        }
        return false;
      }
    });

    overflowMenu.show();
  }

  public void onShareClicked() {
    startActivity(createShareIntent());
  }

  public void onNowPlayingMenuOptionClicked(){
    BusProvider.getInstance().post(new ShowPlaybackFragmentEvent());
  }

  public void onSettingsMenuOptionClicked() {
    Intent intent = new Intent(getActivity(), SettingsActivity.class);
    startActivity(intent);
  }

  private void playAllTracks() {
    BusProvider.getInstance().post(new PlayAllEvent(mArtist, mTracks, false));
  }

  private void playTrack(ParcelableTrack track, boolean playInDialog) {
    BusProvider.getInstance().post(new TrackClickedEvent(mArtist, mTracks, track, playInDialog));
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
    List<ParcelableImage> images = mArtist.images;
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
    mView.mPlayAllButton.setEnabled(true);

    if (startFromScratch) {
      mAdapter = new TracksRecyclerAdapter(mTracks, this);
    }
    mView.getTopTracksRecyclerView().setAdapter(mAdapter);
  }

  private void setupToolbarHeader() {
    // Sets up the collapsing toolbar header to display the artist's top track images
    //
    List<ParcelableImage> trackImages = new ArrayList<>();
    for (ParcelableTrack track : mTracks.tracks) {
      List<ParcelableImage> imgs = track.album.images;
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
    if (mTracks != null && mTracks.tracks.size() > 0) {
      mView.getTopTracksRecyclerView().setVisibility(View.VISIBLE);
      mView.getNoContentView().setVisibility(View.GONE);
    } else {
      mView.getTopTracksRecyclerView().setVisibility(View.GONE);
      mView.getNoContentView().setVisibility(View.VISIBLE);
    }
  }

  private void requestTopTracks() {
    if (mArtist == null) {
      return;
    }

    final Map<String, Object> map = new HashMap<>();

    map.put("country", mCountryCode);

    final Handler handler = new Handler();
    SpotifyStreamerApplication.getSpotifyService().getArtistTopTrack(mArtist.id, map,
        new Callback<Tracks>() {
          @Override
          public void success(Tracks tracks, Response response) {
            mTracks = new ParcelableTracks(tracks);
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
                UiUtils.showToast(getActivity(), error.getLocalizedMessage());
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
  private void setupHeaderImages(final List<ParcelableImage> images, final int index) {
    // Preload the next image to avoid any delay
    if (index != images.size() - 1) {
      Picasso.with(getActivity()).load(images.get(index + 1).url);
    }

    Picasso.with(getActivity()).load(images.get(index).url)
        .into(mView.getArtistHeaderBackgroundImageView(), new com.squareup.picasso.Callback() {
          @Override
          public void onSuccess() {
            mView.getArtistHeaderBackgroundImageView();

            Picasso.with(getActivity()).load(images.get(index).url).into(new Target() {
              @Override
              public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                  @Override
                  public void onGenerated(Palette palette) {
                    mView.mCollapsingToolbarLayout.setExpandedTitleColor(palette.getLightVibrantColor(mView
                        .getDefaultExpandedTitleColor()));
                  }
                });
              }

              @Override
              public void onBitmapFailed(Drawable errorDrawable) {

              }

              @Override
              public void onPrepareLoad(Drawable placeHolderDrawable) {

              }
            });

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
