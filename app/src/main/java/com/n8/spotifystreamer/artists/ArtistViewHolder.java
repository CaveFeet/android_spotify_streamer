package com.n8.spotifystreamer.artists;

import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.n8.spotifystreamer.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;

public class ArtistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public static int VIEW_ID = R.layout.artist_recycler_view;

    private static final String TAG = ArtistViewHolder.class.getSimpleName();

    @InjectView(R.id.artist_recycler_view_title_textView)
    TextView mArtistTitleTextView;

    @InjectView(R.id.artist_recycler_view_imageView)
    ImageView mArtistImageView;

    private Artist mArtist;

    private ArtistsRecyclerAdapter.ArtistClickListener mArtistClickListener;

    public ArtistViewHolder(View itemView, ArtistsRecyclerAdapter.ArtistClickListener artistClickListener) {
        super(itemView);
        ButterKnife.inject(this, itemView);
        mArtistClickListener = artistClickListener;
        itemView.setOnClickListener(this);
    }

    public void bindViewHolder(final Artist artist){

        // Set a unique transition name if using api 21+ to support shared element transitions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mArtistImageView.setTransitionName(artist.id);
        }

        mArtist = artist;
        mArtistTitleTextView.setText(artist.name);
        mArtistImageView.setImageBitmap(null);

        List<Image> images = artist.images;

        if (images != null && images.size() > 0) {
            double closestRatio = Math.abs(((double)images.get(0).height)/200f - 1);
            int closesIndex = 0;

            for (int i = 1; i < images.size(); i++) {
                double ratio = Math.abs(((double)images.get(i).height)/200f - 1);
                if (ratio < closestRatio) {
                    closestRatio = ratio;
                    closesIndex = i;
                }
            }

            Log.d(TAG, "chose image with size height of " + images.get(closesIndex).height);
            Picasso.with(itemView.getContext()).load(images.get(closesIndex).url).into(
                mArtistImageView);
        }
    }

    @Override
    public void onClick(View v) {
        if (mArtistClickListener != null) {
            mArtistClickListener.onArtistViewClicked(mArtist, mArtistImageView);
        }
    }
}
