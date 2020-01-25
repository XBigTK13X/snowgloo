package com.simplepathstudios.snowgloo;

import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

public class LoadingIndicator {
    private static final String TAG = "LoadingIndicator";
    private static boolean isLoading;
    private static ProgressBar progressBar;
    public static void setLoading(boolean status){
        isLoading = status;
        if(isLoading){
            Log.d(TAG,"Showing the progress bar");
            progressBar.setVisibility(View.VISIBLE);
        } else {
            Log.d(TAG,"Hiding the progress bar");
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public static void setProgressBar(ProgressBar progressBar) {
        LoadingIndicator.progressBar = progressBar;
    }
}
