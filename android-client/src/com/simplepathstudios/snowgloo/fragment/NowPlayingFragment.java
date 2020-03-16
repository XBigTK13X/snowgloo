package com.simplepathstudios.snowgloo.fragment;

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

import com.simplepathstudios.snowgloo.R;
import com.simplepathstudios.snowgloo.api.model.MusicFile;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;
import com.simplepathstudios.snowgloo.viewmodel.ObservableMusicQueue;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class NowPlayingFragment extends Fragment {
    private static final String TAG = "NowPlayingFragment";

    private TextView songTitle;
    private TextView songAlbum;
    private TextView songArtist;
    private ImageView coverArt;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.now_playing_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        songTitle = view.findViewById(R.id.song_title);
        songAlbum = view.findViewById(R.id.song_album);
        songArtist = view.findViewById(R.id.song_artist);
        coverArt = view.findViewById(R.id.cover_art);
        ObservableMusicQueue.getInstance().observe(new Observer<MusicQueue>() {
            @Override
            public void onChanged(MusicQueue musicQueue) {
                MusicFile currentSong = musicQueue.getCurrent();
                songTitle.setText(currentSong.Title);
                songAlbum.setText(currentSong.DisplayAlbum);
                songArtist.setText(currentSong.DisplayArtist);
                coverArt.setVisibility(View.INVISIBLE);
                if(currentSong.CoverArt != null && !currentSong.CoverArt.isEmpty()){
                    Picasso.get().load(currentSong.CoverArt).into(coverArt, new Callback() {
                        @Override
                        public void onSuccess() {
                            coverArt.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(Exception e) {
                        }
                    });
                }
            }
        });
    }
}
