package com.simplepathstudios.snowgloo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import android.util.Log;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;
import com.simplepathstudios.snowgloo.audio.AudioPlayer;
import com.simplepathstudios.snowgloo.viewmodel.ObservableMusicQueue;

public class SnowglooService extends Service {

    private static final String TAG = "SnowglooService";

    public static SnowglooService __instance;
    public static SnowglooService getInstance(){
        return __instance;
    }

    AudioPlayer audioPlayer;
    CastContext castContext;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        __instance = this;
        Log.d(TAG, "onCreate()");
        audioPlayer = AudioPlayer.getInstance();
        ObservableMusicQueue.getInstance().observe(new Observer<MusicQueue>() {
            @Override
            public void onChanged(MusicQueue musicQueue) {
                audioPlayer.handleUpdate(musicQueue);
            }
        });

        castContext = CastContext.getSharedInstance(this);
        castContext.addCastStateListener(new CastStateListener() {
            @Override
            public void onCastStateChanged(int i) {
                if(i == CastState.NOT_CONNECTED || i == CastState.NO_DEVICES_AVAILABLE){
                    Log.d(TAG,"Cast state is now "+i+" the chromecast is disconnected.");
                    audioPlayer.setPlaybackMode(AudioPlayer.PlaybackMode.LOCAL);
                }
                else if(i == CastState.CONNECTING){
                    Log.d(TAG,"Cast state is now "+i+" the chromecast is handshaking.");
                }
                else if(i == CastState.CONNECTED){
                    Log.d(TAG,"Cast state is now "+i+" the chromecast is connected.");
                    audioPlayer.setPlaybackMode(AudioPlayer.PlaybackMode.REMOTE);
                }
            }
        });
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
        audioPlayer.destroy();
        stopForeground(true);
        stopSelf();
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