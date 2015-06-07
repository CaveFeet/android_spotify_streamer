package com.n8.spotifystreamer.tracks;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

public class TracksRecyclerAdapter extends RecyclerView.Adapter<TrackViewHolder> {

    private List<Track> mTracks;

    public TracksRecyclerAdapter(List<Track> tracks) {
        mTracks = tracks;
    }

    @Override
    public TrackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(TrackViewHolder.VIEW_ID, parent, false);

        return new TrackViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TrackViewHolder holder, int position) {
        holder.bindViewHolder(mTracks.get(position));
    }

    @Override
    public int getItemCount() {
        return mTracks == null ? 0 : mTracks.size();
    }
}
