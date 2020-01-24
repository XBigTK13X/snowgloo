package com.simplepathstudios.snowgloo.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.simplepathstudios.snowgloo.MainActivity;
import com.simplepathstudios.snowgloo.R;
import com.simplepathstudios.snowgloo.component.SongListComponent;

public class QueueFragment extends Fragment {
    static final String TAG = "QueueFragment";

    private SongListComponent songListComponent;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "QueueFragment initiated");
        return inflater.inflate(R.layout.queue_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        songListComponent = new SongListComponent((MainActivity)getActivity(),this);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    public void onResume(){
        super.onResume();
        songListComponent.refresh();
    }

    @Override
    public void onPause(){
        super.onPause();
        songListComponent.clear();
    }

}
