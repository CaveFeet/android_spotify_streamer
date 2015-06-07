package com.n8.spotifystreamer;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;

public class ArtistsRecyclerAdapter extends RecyclerView.Adapter<ArtistViewHolder> {

    private List<Artist> mArtists;

    public ArtistsRecyclerAdapter(List<Artist> artists) {
        mArtists = artists;
    }

    @Override
    public ArtistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(ArtistViewHolder.VIEW_ID, parent, false);

        return new ArtistViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ArtistViewHolder holder, int position) {
        holder.bindViewHolder(mArtists.get(position));
    }

    @Override
    public int getItemCount() {
        return mArtists.size();
    }
}
