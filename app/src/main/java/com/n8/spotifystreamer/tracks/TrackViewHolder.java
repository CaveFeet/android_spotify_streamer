package com.n8.spotifystreamer.tracks;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.n8.spotifystreamer.artists.ArtistViewHolder;
import com.n8.spotifystreamer.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

public class TrackViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public static int VIEW_ID = R.layout.track_recycler_view;

    private static final String TAG = ArtistViewHolder.class.getSimpleName();

    @InjectView(R.id.track_recycler_view_title_textView)
    TextView mTrackTitleTextView;

    @InjectView(R.id.track_recycler_view_imageView)
    ImageView mTrackImageView;

    private Track mTrack;

    public TrackViewHolder(View itemView) {
        super(itemView);
        ButterKnife.inject(this, itemView);
        itemView.setOnClickListener(this);
    }

    public void bindViewHolder(final Track track){

        mTrack = track;
        mTrackTitleTextView.setText(mTrack.name);
        mTrackImageView.setImageBitmap(null);

        List<Image> images = mTrack.album.images;
        if (images != null && images.size() > 0) {
            int index = images.size()-1;
            Picasso.with(itemView.getContext()).load(images.get(index).url).into(mTrackImageView);
        }
    }

    @Override
    public void onClick(View v) {

    }
}
