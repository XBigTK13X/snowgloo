package com.simplepathstudios.snowgloo;

import com.simplepathstudios.snowgloo.api.ApiClient;
import com.simplepathstudios.snowgloo.api.model.MusicFile;

import android.content.Context;
import androidx.loader.content.AsyncTaskLoader;
import android.util.Log;

import java.util.List;

public class SnowglooLoader extends AsyncTaskLoader<List<MusicFile>> {

    private static final String TAG = "SnowglooLoader";

    public SnowglooLoader(Context context) {
        super(context);
    }

    @Override
    public List<MusicFile> loadInBackground() {
        try {
            return ApiClient.getInstance().getQueue().songs;
        } catch (Exception e) {
            Log.e(TAG, "Failed to fetch media data", e);
            return null;
        }
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

}
