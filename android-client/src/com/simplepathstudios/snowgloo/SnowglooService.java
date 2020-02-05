package com.simplepathstudios.snowgloo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import android.provider.MediaStore;
import android.util.Log;

import com.simplepathstudios.snowgloo.audio.AudioPlayer;

public class SnowglooService extends Service {

    private static final String TAG = "SnowglooService";

    AudioPlayer audioPlayer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        audioPlayer = AudioPlayer.getInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        return START_NOT_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(TAG, "Swiped away from the recents menu, close the activity");
        MainActivity.getInstance().cleanup();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d(TAG, "onLowMemory()");
    }
}