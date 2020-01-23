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
import com.simplepathstudios.snowgloo.adapter.MediaQueueAdapter;

public class QueueFragment extends Fragment {
    static final String TAG = "QueueFragment";

    private MediaQueueAdapter musicQueueAdapter;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        musicQueueAdapter = new MediaQueueAdapter((MainActivity)getActivity(),this);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "QueueFragment initiated");
        return inflater.inflate(R.layout.queue_fragment, container, false);
    }


    public void onResume(){
        super.onResume();
        musicQueueAdapter.refresh();
    }

    @Override
    public void onPause(){
        super.onPause();
        musicQueueAdapter.clear();
    }

}
