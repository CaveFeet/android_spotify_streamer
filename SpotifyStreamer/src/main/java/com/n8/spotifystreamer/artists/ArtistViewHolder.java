package com.n8.spotifystreamer.artists;

import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.n8.spotifystreamer.ImageUtils;
import com.n8.spotifystreamer.R;
import com.n8.spotifystreamer.models.ParcelableArtist;
import com.n8.spotifystreamer.models.ParcelableImage;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ArtistViewHolder extends RecyclerView.ViewHolder {

    public static final int THUMBNAIL_SIZE = 200;

    public static int VIEW_ID = R.layout.recycler_view_artist;

    private static final String TAG = ArtistViewHolder.class.getSimpleName();

    @InjectView(R.id.artist_recycler_view_title_textView)
    TextView mArtistTitleTextView;

    @InjectView(R.id.artist_recycler_view_imageView)
    ImageView mArtistImageView;

    @InjectView(R.id.artist_recycler_view_popularity_textView)
    TextView mPopularityTextView;

    @InjectView(R.id.artist_recycler_view_followers_textView)
    TextView mFollowersTextView;

    private ParcelableArtist mArtist;

    public ArtistViewHolder(View itemView) {
        super(itemView);
        ButterKnife.inject(this, itemView);
    }

    public void bindViewHolder(final ParcelableArtist artist){

        // Set a unique transition name if using api 21+ to support shared element transitions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mArtistImageView.setTransitionName(artist.id);
        }

        mArtist = artist;
        mArtistTitleTextView.setText(artist.name);
        mArtistImageView.setImageBitmap(null);

        DecimalFormat formatter = new DecimalFormat("#,###,###,###");
        mPopularityTextView.setText(formatter.format(mArtist.popularity));
        mFollowersTextView.setText(formatter.format(mArtist.followers.total));


        List<ParcelableImage> images = artist.images;

        if (images != null && images.size() > 0) {
            int index = ImageUtils.getIndexOfClosestSizeImage(images, THUMBNAIL_SIZE);
            Picasso.with(itemView.getContext()).load(images.get(index).url).error(R.drawable.ic_artist_placeholder_light).into(mArtistImageView);
        }else {
            Picasso.with(itemView.getContext()).load(R.drawable.ic_artist_placeholder_light).into(mArtistImageView);
        }
    }
}
