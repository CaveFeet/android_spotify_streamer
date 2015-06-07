package com.n8.spotifystreamer;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;

public class ArtistViewHolder extends RecyclerView.ViewHolder {

    public static int VIEW_ID = R.layout.artist_recycler_view;

    private static final String TAG = ArtistViewHolder.class.getSimpleName();

    @InjectView(R.id.artist_recycler_view_title_textView)
    TextView mArtistTitleTextView;

    @InjectView(R.id.artist_recycler_view_imageView)
    ImageView mArtistImageView;

    public ArtistViewHolder(View itemView) {
        super(itemView);
        ButterKnife.inject(this, itemView);
    }

    public void bindViewHolder(final Artist artist){
        mArtistTitleTextView.setText(artist.name);
        mArtistImageView.setImageBitmap(null);

        List<Image> images = artist.images;
        if (images != null && images.size() > 0) {
            int index = images.size()-1;
            Picasso.with(itemView.getContext()).load(images.get(index).url).into(mArtistImageView);
        }
    }
}
