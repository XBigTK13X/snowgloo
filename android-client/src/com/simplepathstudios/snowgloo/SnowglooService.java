package com.simplepathstudios.snowgloo;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.browse.MediaBrowser;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.service.media.MediaBrowserService;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.simplepathstudios.snowgloo.api.model.MusicFile;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;
import com.simplepathstudios.snowgloo.audio.AudioPlayer;
import com.simplepathstudios.snowgloo.viewmodel.ObservableMusicQueue;

import java.util.ArrayList;
import java.util.List;

public class SnowglooService extends MediaBrowserServiceCompat {

    public class SnowglooBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Util.log(TAG, "onReceive "+intent.getAction());
            String action = intent.getAction();
            switch(action){
                case MediaNotification.Action.PLAY:
                    __instance.audioPlayer.play();
                    break;
                case MediaNotification.Action.PAUSE:
                    __instance.audioPlayer.pause();
                    break;
                case MediaNotification.Action.NEXT:
                    __instance.audioPlayer.next();
                    break;
                case MediaNotification.Action.PREVIOUS:
                    __instance.audioPlayer.previous();
                    break;
            }
        }
    }

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
    IntentFilter intentFilter;
    SnowglooBroadcastReceiver broadcastReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new MediaBrowserServiceCompat.BrowserRoot("__SNOWGLOO_MEDIA_ROOT", null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        MusicQueue queue = ObservableMusicQueue.getInstance().getQueue();
        ArrayList<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        for(MusicFile song : queue.songs){
            MediaDescriptionCompat mediaDescription = new MediaDescriptionCompat.Builder()
                    .setIconUri(Uri.parse(song.CoverArt))
                    .setTitle(song.Title)
                    .setSubtitle(song.DisplayAlbum + " - " + song.DisplayArtist)
                    .build();
            MediaBrowserCompat.MediaItem mediaItem = new MediaBrowserCompat.MediaItem(mediaDescription, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
            mediaItems.add(mediaItem);
        }
        result.sendResult(mediaItems);
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
                Util.log(TAG,"onMediaButtonEvent" + mediaButtonIntent.getAction());
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
        intentFilter = new IntentFilter();
        intentFilter.addAction(MediaNotification.Action.PLAY);
        intentFilter.addAction(MediaNotification.Action.PAUSE);
        intentFilter.addAction(MediaNotification.Action.NEXT);
        intentFilter.addAction(MediaNotification.Action.PREVIOUS);
        broadcastReceiver = new SnowglooBroadcastReceiver();
        Util.getGlobalContext().registerReceiver(broadcastReceiver, intentFilter);
    }

    public MediaSession getMediaSession(){
        return mediaSession;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Util.log(TAG, "onStartCommand action=" + intent.getAction());
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