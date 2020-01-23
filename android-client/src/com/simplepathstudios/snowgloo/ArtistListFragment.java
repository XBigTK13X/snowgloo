package com.simplepathstudios.snowgloo;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ArtistListFragment extends Fragment {
    private final String TAG = "ArtistListFragment";
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "ArtistListFragment initiated");
        return inflater.inflate(R.layout.artist_list_fragment, container, false);
    }
}
