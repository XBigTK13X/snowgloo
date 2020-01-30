package com.simplepathstudios.snowgloo.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.simplepathstudios.snowgloo.R;
import com.simplepathstudios.snowgloo.api.model.MusicFile;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;
import com.simplepathstudios.snowgloo.viewmodel.MusicQueueViewModel;
import com.squareup.picasso.Picasso;

public class NowPlayingFragment extends Fragment {
    private static final String TAG = "NowPlayingFragment";

    private TextView trackMetadataView;
    private ImageView coverArt;
    private MusicQueueViewModel musicQueueViewModel;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "NowPlayingFragment initiated");
        return inflater.inflate(R.layout.now_playing_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        trackMetadataView = view.findViewById(R.id.track_metadata);
        coverArt = view.findViewById(R.id.cover_art);
        this.musicQueueViewModel = new ViewModelProvider(getActivity()).get(MusicQueueViewModel.class);
        musicQueueViewModel.Data.observe(getViewLifecycleOwner(), new Observer<MusicQueue>() {
            @Override
            public void onChanged(MusicQueue musicQueue) {
                Log.d(TAG, "Updating main activity track metadata");
                MusicFile currentSong = musicQueue.getCurrent();
                if(currentSong.CoverArt != null){
                    trackMetadataView.setText(currentSong.getMetadata());
                    Picasso.get().load(currentSong.CoverArt).into(coverArt);
                }

            }
        });
    }
}
