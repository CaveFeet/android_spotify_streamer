package com.n8.spotifystreamer.artists;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.n8.spotifystreamer.models.ParcelableArtist;

import java.util.List;

public class ArtistsRecyclerAdapter extends RecyclerView.Adapter<ArtistViewHolder> {

    public interface ArtistClickListener{
        void onArtistViewClicked(ParcelableArtist artist, ImageView sharedImage);
    }

    private List<ParcelableArtist> mArtists;

    private ArtistClickListener mArtistClickListener;

    int selectedPosition = -1;

    public ArtistsRecyclerAdapter(List<ParcelableArtist> artists, ArtistClickListener artistClickListener) {
        mArtists = artists;
        mArtistClickListener = artistClickListener;
    }

    @Override
    public ArtistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(ArtistViewHolder.VIEW_ID, parent, false);

        return new ArtistViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ArtistViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int oldselected = selectedPosition;
                selectedPosition = position;
                notifyItemChanged(oldselected);
                holder.itemView.setActivated(true);
                mArtistClickListener.onArtistViewClicked(mArtists.get(position), holder.mArtistImageView);
            }
        });
        if (position != selectedPosition) {
            holder.itemView.setActivated(false);
        } else {
            holder.itemView.setActivated(true);
        }
        holder.bindViewHolder(mArtists.get(position));
    }

    @Override
    public int getItemCount() {
        return mArtists == null ? 0 : mArtists.size();
    }
}
