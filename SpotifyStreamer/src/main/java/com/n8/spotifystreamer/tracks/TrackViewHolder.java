package com.n8.spotifystreamer.tracks;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.n8.spotifystreamer.ImageUtils;
import com.n8.spotifystreamer.artists.ArtistViewHolder;
import com.n8.spotifystreamer.R;
import com.n8.spotifystreamer.models.ParcelableImage;
import com.n8.spotifystreamer.models.ParcelableTrack;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class TrackViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public static int VIEW_ID = R.layout.recycler_view_track;

    private static final String TAG = ArtistViewHolder.class.getSimpleName();

    private static final int THUMBNAIL_SIZE = 200;

    @InjectView(R.id.track_recycler_view_track_title_textView)
    TextView mTrackTitleTextView;

    @InjectView(R.id.track_recycler_view_album_title_textView)
    TextView mAlbumTitleTextView;

    @InjectView(R.id.track_recycler_view_imageView)
    ImageView mTrackImageView;

    @InjectView(R.id.track_recycler_view_popularity_textView)
    TextView mPopularityTextView;

    @InjectView(R.id.track_recycler_view_overflow_imageView)
    ImageView mOverflowImageView;

    private ParcelableTrack mTrack;

    private TracksRecyclerAdapter.TrackClickListener mListener;

    public TrackViewHolder(View itemView, TracksRecyclerAdapter.TrackClickListener listener) {
        super(itemView);
        mListener = listener;
        ButterKnife.inject(this, itemView);
        itemView.setOnClickListener(this);
        mOverflowImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onOverflowClicked(mOverflowImageView, mTrack);
                }
            }
        });
    }

    public void bindViewHolder(final ParcelableTrack track){

        mTrack = track;
        mTrackTitleTextView.setText(mTrack.name);
        mAlbumTitleTextView.setText(mTrack.album.name);
        mTrackImageView.setImageBitmap(null);

        DecimalFormat formatter = new DecimalFormat("#,###,###,###");
        mPopularityTextView.setText(formatter.format(mTrack.popularity));

        List<ParcelableImage> images = mTrack.album.images;
        if (images != null && images.size() > 0) {
            int index = ImageUtils.getIndexOfClosestSizeImage(images, THUMBNAIL_SIZE);
            Picasso.with(itemView.getContext()).load(images.get(index).url).error(R.drawable.ic_track_placeholder_light).into(mTrackImageView);
        } else {
            Picasso.with(itemView.getContext()).load(R.drawable.ic_track_placeholder_light).into(mTrackImageView);
        }
    }

    @Override
    public void onClick(View v) {
        if (mListener != null) {
            mListener.onTrackViewClicked(mTrack);
        }
    }
}
