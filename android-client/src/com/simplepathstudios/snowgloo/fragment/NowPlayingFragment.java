package com.simplepathstudios.snowgloo.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.simplepathstudios.snowgloo.MainActivity;
import com.simplepathstudios.snowgloo.R;
import com.simplepathstudios.snowgloo.api.ApiClient;
import com.simplepathstudios.snowgloo.api.model.CoverArt;
import com.simplepathstudios.snowgloo.api.model.MusicFile;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;
import com.simplepathstudios.snowgloo.viewmodel.ObservableMusicQueue;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NowPlayingFragment extends Fragment {
    private static final String TAG = "NowPlayingFragment";

    private TextView trackMetadataView;
    private ImageView coverArt;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "NowPlayingFragment initiated");
        return inflater.inflate(R.layout.now_playing_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        trackMetadataView = view.findViewById(R.id.track_metadata);
        coverArt = view.findViewById(R.id.cover_art);
        ObservableMusicQueue.getInstance().observe(new Observer<MusicQueue>() {
            @Override
            public void onChanged(MusicQueue musicQueue) {
                Log.d(TAG, "Updating now playing track metadata");
                MusicFile currentSong = musicQueue.getCurrent();
                trackMetadataView.setText(currentSong.getMetadata());

                if(currentSong.CoverArt != null){
                    ApiClient.getInstance().getCoverArt(currentSong.LocalFilePath, currentSong.CoverArt).enqueue(new Callback<CoverArt>() {
                        @Override
                        public void onResponse(Call call, Response response) {
                            String imageUrl = ((CoverArt)response.body()).coverArtUri;
                            int base64Location = imageUrl.indexOf("base64,");
                            if (base64Location == -1) {
                                Picasso.get().load(currentSong.CoverArt).into(coverArt);
                            } else {
                                String imageData = imageUrl.substring(base64Location + 6);
                                byte[] decodedString = Base64.decode(imageData, Base64.DEFAULT);
                                Bitmap bitMap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                coverArt.setImageBitmap(bitMap);
                            }
                        }

                        @Override
                        public void onFailure(Call call, Throwable t) {
                            Picasso.get().load(currentSong.CoverArt).into(coverArt);
                        }
                    });

                }
            }
        });
    }
}
