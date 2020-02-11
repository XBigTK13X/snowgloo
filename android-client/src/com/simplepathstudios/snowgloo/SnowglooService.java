package com.simplepathstudios.snowgloo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import android.os.PowerManager;
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
    private static final String WAKE_LOCK_TAG = "snowgloo:background_audio";

    public static SnowglooService __instance;
    public static SnowglooService getInstance(){
        return __instance;
    }

    AudioPlayer audioPlayer;
    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        __instance = this;
        Util.log(TAG, "onCreate()");
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
        wakeLock.acquire();
        audioPlayer = AudioPlayer.getInstance();

        CastContext castContext = MainActivity.getInstance().getCastContext();
        if(castContext != null){
            castContext.addCastStateListener(new CastStateListener() {
                @Override
                public void onCastStateChanged(int i) {
                    if(i == CastState.NOT_CONNECTED || i == CastState.NO_DEVICES_AVAILABLE){
                        Util.log(TAG, "Cast session changed state to " +CastState.toString(i));
                        audioPlayer.setPlaybackMode(AudioPlayer.PlaybackMode.LOCAL);
                    }
                    else if(i == CastState.CONNECTED){
                        Util.log(TAG, "Cast session changed state to " + CastState.toString(i));
                        audioPlayer.setPlaybackMode(AudioPlayer.PlaybackMode.REMOTE);
                    } else {
                        Util.log(TAG, "Cast session changed state to " + CastState.toString(i));
                    }
                }
            });
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Util.log(TAG, "Swiped away from the recents menu, close the activity");
        audioPlayer.destroy();
        wakeLock.release();
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}