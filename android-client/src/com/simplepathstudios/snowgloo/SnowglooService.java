package com.simplepathstudios.snowgloo;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.session.MediaSession;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.media.MediaBrowserServiceCompat;

import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.common.images.WebImage;
import com.simplepathstudios.snowgloo.api.model.MusicFile;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;
import com.simplepathstudios.snowgloo.audio.AudioPlayer;
import com.simplepathstudios.snowgloo.viewmodel.ObservableMusicQueue;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

public class SnowglooService extends MediaBrowserServiceCompat {

    public static android.content.ComponentName ComponentName = new ComponentName("com.simplepathstudios.snowgloo.SnowglooService", SnowglooService.class.getName());

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
    MediaSessionCompat mediaSession;
    MediaSessionCompat.Callback mediaCallback;
    MediaControllerCompat mediaController;
    PlaybackStateCompat playbackState;
    MediaControllerCompat.TransportControls transportControls;
    IntentFilter intentFilter;
    SnowglooBroadcastReceiver broadcastReceiver;
    Bitmap coverArtBitmap;

    private Target coverArtTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            coverArtBitmap = bitmap;
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    };

    public MediaSessionCompat getMediaSession(){
        return mediaSession;
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

    public void updatePlaybackState(boolean isPlaying){
        Util.log(TAG, "updatePlaybackState "+isPlaying);
        AudioPlayer player = AudioPlayer.getInstance();
        Integer position = player.getSongPosition();
        position = position == null ? 0 : position;
        Integer playbackSpeedMultiple = 1;
        int state = isPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
        playbackState = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_PAUSE |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                )
                .setState(state,position,playbackSpeedMultiple)
                .build();
        mediaSession.setPlaybackState(playbackState);
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

        mediaCallback = new MediaSessionCompat.Callback() {
            @Override
            public boolean onMediaButtonEvent(@NonNull Intent mediaButtonIntent) {
                Util.log(TAG,"onMediaButtonEvent" + mediaButtonIntent.getAction());
                return super.onMediaButtonEvent(mediaButtonIntent);
            }

            @Override
            public void onPlay() {
                super.onPlay();
                Util.log(TAG,"onPlay");
                audioPlayer.play();
                updatePlaybackState(true);
            }

            @Override
            public void onPause() {
                super.onPause();
                Util.log(TAG,"onPause");
                audioPlayer.pause();
                updatePlaybackState(false);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                Util.log(TAG,"onSkipToNext");
                audioPlayer.next();
                updatePlaybackState(true);
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                Util.log(TAG,"onSkipToPrevious");
                audioPlayer.previous();
                updatePlaybackState(true);
            }

            @Override
            public void onStop() {
                super.onStop();
                Util.log(TAG,"onStop");
                audioPlayer.stop();
            }
        };
        mediaSession = new MediaSessionCompat(Util.getGlobalContext(),"SnowglooMediaSession");
        mediaSession.setCallback(mediaCallback);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setActive(true);
        setSessionToken(mediaSession.getSessionToken());

        try {
            mediaController = new MediaControllerCompat(Util.getGlobalContext(), mediaSession.getSessionToken());
            transportControls = mediaController.getTransportControls();
        } catch (RemoteException e) {
            Util.error(TAG, e);
        }

        updatePlaybackState(true);

        intentFilter = new IntentFilter();
        intentFilter.addAction(MediaNotification.Action.PLAY);
        intentFilter.addAction(MediaNotification.Action.PAUSE);
        intentFilter.addAction(MediaNotification.Action.NEXT);
        intentFilter.addAction(MediaNotification.Action.PREVIOUS);
        broadcastReceiver = new SnowglooBroadcastReceiver();
        Util.getGlobalContext().registerReceiver(broadcastReceiver, intentFilter);

        ObservableMusicQueue.getInstance().observe(new Observer<MusicQueue>() {
            @Override
            public void onChanged(MusicQueue musicQueue) {
                MusicFile currentSong = musicQueue.getCurrent();
                if(currentSong.CoverArt != null && !currentSong.CoverArt.isEmpty()){
                    Picasso.get().load(currentSong.CoverArt).into(coverArtTarget);
                }
                Util.log(TAG, "metadata queue changed to "+currentSong.Title);
                MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, currentSong.Title)
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentSong.Title)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentSong.DisplayArtist)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, currentSong.DisplayAlbum)
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, currentSong.CoverArt)
                        .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, coverArtBitmap)
                        .putBitmap(MediaMetadataCompat.METADATA_KEY_ART, coverArtBitmap)
                    .build();
                mediaSession.setMetadata(metadata);
            }
        });
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
        mediaSession.release();
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaSession.release();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}