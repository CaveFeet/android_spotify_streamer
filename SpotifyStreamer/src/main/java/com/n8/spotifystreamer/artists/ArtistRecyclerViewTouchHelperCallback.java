/*
COPYRIGHT 1995-2015 ESRI

TRADE SECRETS: ESRI PROPRIETARY AND CONFIDENTIAL 
Unpublished material - all rights reserved under the Copyright Laws of the United States.

For additional information, contact: Environmental Systems Research Institute, Inc. 
Attn: Contracts Dept 380 New York Street Redlands, California, USA 92373

email: contracts@esri.com
*/
package com.n8.spotifystreamer.artists;

import android.content.Context;
import android.graphics.Canvas;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.n8.spotifystreamer.R;
import com.n8.spotifystreamer.models.ParcelableArtist;

import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;

/**
 * Helper class for managing behavior of custom ItemTouchHelper
 */
public class ArtistRecyclerViewTouchHelperCallback extends ItemTouchHelper.SimpleCallback {

  private ViewGroup mParentView;

  private RecyclerView.Adapter<ArtistViewHolder> mAdapter;

  private List<ParcelableArtist> mArtists;

  private String mSnackbarText;

  private String mSnackbarActionText;

  private LayoutInflater mLayoutInflater;

  private View mSwipeBackgroundView;

  public ArtistRecyclerViewTouchHelperCallback(ViewGroup parentView, RecyclerView.Adapter<ArtistViewHolder> adapter,
                                               List<ParcelableArtist> artists) {
    super(0, ItemTouchHelper.RIGHT);
    mParentView = parentView;
    mAdapter = adapter;
    mArtists = artists;

    Context context = mParentView.getContext();
    mSnackbarText = context.getString(R.string.remove_artist_search_result);
    mSnackbarActionText = context.getString(R.string.undo);

    mLayoutInflater =(LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

    mSwipeBackgroundView = mLayoutInflater.inflate(R.layout.artist_swipe_to_delete_background, mParentView, false);
    mSwipeBackgroundView.measure(
        View.MeasureSpec.getSize(mSwipeBackgroundView.getMeasuredWidth()),
        View.MeasureSpec.getSize(mSwipeBackgroundView.getMeasuredHeight())
    );
  }

  public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
      RecyclerView.ViewHolder target) {
    return false;
  }

  @Override
  public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {

    final int index = viewHolder.getAdapterPosition();
    final ParcelableArtist artist = mArtists.remove(index);
    mAdapter.notifyItemRemoved(index);

    Snackbar snackbar = Snackbar.make(mParentView, mSnackbarText, Snackbar.LENGTH_LONG)
        .setAction(mSnackbarActionText, new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            mArtists.add(index, artist);
            mAdapter.notifyItemInserted(index);

          }
        });

    snackbar.show();
  }

  @Override
  public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
      float dX, float dY, int actionState, boolean isCurrentlyActive) {
    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState,
        isCurrentlyActive);

    // Show the background view if the viewholder.itemView is being swiped
    //
    if (isCurrentlyActive) {
      View viewHolderView = viewHolder.itemView;
      mSwipeBackgroundView.layout(
          viewHolder.itemView.getLeft(),
          viewHolder.itemView.getTop(),
          viewHolder.itemView.getRight(),
          viewHolder.itemView.getBottom()
      );

      final int saveCount = c.save();
      try {
        c.translate(viewHolderView.getLeft(), viewHolderView.getTop());
        mSwipeBackgroundView.draw(c);
      } finally {
        c.restoreToCount(saveCount);
      }
    }
  }
}
