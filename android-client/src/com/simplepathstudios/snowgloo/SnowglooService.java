package com.simplepathstudios.snowgloo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.ResultReceiver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.simplepathstudios.snowgloo.audio.AudioPlayer;

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
    MediaSession mediaSession;

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
        mediaSession = new MediaSession(Util.getGlobalContext(),"SnowglooMediaSession");
        mediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public boolean onMediaButtonEvent(@NonNull Intent mediaButtonIntent) {
                Util.log(TAG,mediaButtonIntent.getAction());
                return super.onMediaButtonEvent(mediaButtonIntent);
            }

            @Override
            public void onPlay() {
                super.onPlay();
                audioPlayer.play();
            }

            @Override
            public void onPause() {
                super.onPause();
                audioPlayer.pause();
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                audioPlayer.next();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                audioPlayer.previous();
            }

            @Override
            public void onStop() {
                super.onStop();
                audioPlayer.stop();
            }
        });

        mediaSession.setActive(true);

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

    public MediaSession getMediaSession(){
        return mediaSession;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Util.toast(intent.getAction());
        return START_NOT_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Util.log(TAG, "Swiped away from the recents menu, close the activity");
        try{
            audioPlayer.destroy();
            audioPlayer = null;
        } catch(Exception swallow){

        }
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