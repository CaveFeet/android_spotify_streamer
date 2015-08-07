package com.n8.spotifystreamer.tracks;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.n8.spotifystreamer.models.ParcelableTrack;
import com.n8.spotifystreamer.models.ParcelableTracks;

import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Track;

public class TracksRecyclerAdapter extends RecyclerView.Adapter<TrackViewHolder> {

    public interface TrackClickListener{
        void onTrackViewClicked(ParcelableTrack track);

        void onOverflowClicked(View view, ParcelableTrack track);
    }

    private TrackClickListener mTrackClickListener;

    private ParcelableTracks mTracks;

    public TracksRecyclerAdapter(ParcelableTracks tracks, TrackClickListener listener) {
        mTrackClickListener = listener;
        mTracks = tracks;
    }

    @Override
    public TrackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(TrackViewHolder.VIEW_ID, parent, false);

        return new TrackViewHolder(v, mTrackClickListener);
    }

    @Override
    public void onBindViewHolder(TrackViewHolder holder, int position) {
        holder.bindViewHolder(mTracks.tracks.get(position));
    }

    @Override
    public int getItemCount() {
        return mTracks == null ? 0 : mTracks.tracks.size();
    }
}
