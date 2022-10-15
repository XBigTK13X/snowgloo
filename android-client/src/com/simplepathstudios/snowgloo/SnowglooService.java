package com.simplepathstudios.snowgloo;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.media.browse.MediaBrowser.MediaItem;
import android.service.media.MediaBrowserService;
import android.media.MediaDescription;
import android.media.session.PlaybackState;

import androidx.lifecycle.Observer;

import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.simplepathstudios.snowgloo.api.model.MusicFile;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;
import com.simplepathstudios.snowgloo.audio.AudioPlayer;
import com.simplepathstudios.snowgloo.viewmodel.ObservableMusicQueue;

import java.util.ArrayList;
import java.util.List;


//https://developer.android.com/guide/topics/media/media3/getting-started/migration-guide#MediaBrowser
public class SnowglooService extends MediaBrowserService {

    public static android.content.ComponentName ComponentName = new ComponentName("com.simplepathstudios.snowgloo.SnowglooService", SnowglooService.class.getName());

    public static class SnowglooBroadcastReceiver extends BroadcastReceiver{

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
    MediaSession.Callback mediaCallback;
    MediaController mediaController;
    PlaybackState playbackState;
    MediaController.TransportControls transportControls;
    IntentFilter intentFilter;
    SnowglooBroadcastReceiver broadcastReceiver;

    public MediaSession getMediaSession(){
        return mediaSession;
    }

    @Override
    public BrowserRoot onGetRoot(String clientPackageName, int clientUid, Bundle rootHints) {
        return new MediaBrowserService.BrowserRoot("__SNOWGLOO_MEDIA_ROOT", null);
    }

    @Override
    public void onLoadChildren(String s, Result<List<MediaItem>> result) {
        MusicQueue queue = ObservableMusicQueue.getInstance().getQueue();
        ArrayList<MediaItem> mediaItems = new ArrayList<>();
        MusicFile song = queue.getCurrent();
        if(song == null || song.Id == null) {
            result.sendResult(null);
            return;
        }
        MediaDescription mediaDescription = new MediaDescription.Builder()
                .setMediaId(song.Id)
                .setIconUri(Uri.parse(song.CoverArt))
                .setTitle(song.Title)
                .setSubtitle(song.DisplayAlbum + " - " + song.DisplayArtist)
                .build();
        MediaItem mediaItem = new MediaItem(mediaDescription, MediaItem.FLAG_PLAYABLE);
        mediaItems.add(mediaItem);
        result.sendResult(mediaItems);
    }

    public void updatePlaybackState(boolean isPlaying){
        AudioPlayer player = AudioPlayer.getInstance();
        Integer position = player.getSongPosition();
        position = position == null ? 0 : position;
        Integer playbackSpeedMultiple = 1;
        int state = isPlaying ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED;
        playbackState = new PlaybackState.Builder()
                .setActions(
                        PlaybackState.ACTION_PLAY |
                        PlaybackState.ACTION_PAUSE |
                        PlaybackState.ACTION_SKIP_TO_NEXT |
                        PlaybackState.ACTION_SKIP_TO_PREVIOUS
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

        if(MainActivity.getInstance() != null) {
            CastContext castContext = MainActivity.getInstance().getCastContext();
            if (castContext != null) {
                castContext.addCastStateListener(new CastStateListener() {
                    @Override
                    public void onCastStateChanged(int i) {
                        if (i == CastState.NOT_CONNECTED || i == CastState.NO_DEVICES_AVAILABLE) {
                            Util.log(TAG, "Cast session changed state to " + CastState.toString(i));
                            audioPlayer.setPlaybackMode(AudioPlayer.PlaybackMode.LOCAL);
                        } else if (i == CastState.CONNECTED) {
                            Util.log(TAG, "Cast session changed state to " + CastState.toString(i));
                            audioPlayer.setPlaybackMode(AudioPlayer.PlaybackMode.REMOTE);
                        } else {
                            Util.log(TAG, "Cast session changed state to " + CastState.toString(i));
                        }
                    }
                });
            }


            mediaCallback = new MediaSession.Callback() {
                @Override
                public boolean onMediaButtonEvent(Intent mediaButtonIntent) {
                    Util.log(TAG, "onMediaButtonEvent" + mediaButtonIntent.getAction());
                    return super.onMediaButtonEvent(mediaButtonIntent);
                }

                @Override
                public void onPlay() {
                    super.onPlay();
                    Util.log(TAG, "onPlay");
                    audioPlayer.play();
                }

                @Override
                public void onPause() {
                    super.onPause();
                    Util.log(TAG, "onPause");
                    audioPlayer.pause();
                }

                @Override
                public void onSkipToNext() {
                    super.onSkipToNext();
                    Util.log(TAG, "onSkipToNext");
                    audioPlayer.next();
                }

                @Override
                public void onSkipToPrevious() {
                    super.onSkipToPrevious();
                    Util.log(TAG, "onSkipToPrevious");
                    audioPlayer.previous();
                }

                @Override
                public void onStop() {
                    super.onStop();
                    Util.log(TAG, "onStop");
                    audioPlayer.stop();
                }
            };
            mediaSession = new MediaSession(Util.getGlobalContext(), "SnowglooMediaSession");
            mediaSession.setCallback(mediaCallback);
            //TODO Deprecated ? mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_QUEUE_COMMANDS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
            mediaSession.setActive(true);
            setSessionToken(mediaSession.getSessionToken());

            mediaController = new MediaController(Util.getGlobalContext(), mediaSession.getSessionToken());
            transportControls = mediaController.getTransportControls();

            updatePlaybackState(true);

            intentFilter = new IntentFilter();
            intentFilter.addAction(MediaNotification.Action.PLAY);
            intentFilter.addAction(MediaNotification.Action.PAUSE);
            intentFilter.addAction(MediaNotification.Action.NEXT);
            intentFilter.addAction(MediaNotification.Action.PREVIOUS);
            broadcastReceiver = new SnowglooBroadcastReceiver();
            Util.getGlobalContext().registerReceiver(broadcastReceiver, intentFilter);

            // TODO https://stackoverflow.com/questions/64045082/live-data-candidate-resolution-will-be-changed-soon
            ObservableMusicQueue.getInstance().observe(new Observer<MusicQueue>() {
                @Override
                public void onChanged(MusicQueue musicQueue) {
                    updatePlaybackState(musicQueue.playerState == MusicQueue.PlayerState.PLAYING ? true : false);
                    MusicFile currentSong = musicQueue.getCurrent();
                    if (musicQueue != null && musicQueue.currentIndex != null) {
                        MediaMetadata metadata = new MediaMetadata.Builder()
                                .putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, currentSong.Title)
                                .putString(MediaMetadata.METADATA_KEY_TITLE, currentSong.Title)
                                .putString(MediaMetadata.METADATA_KEY_ARTIST, currentSong.DisplayArtist)
                                .putString(MediaMetadata.METADATA_KEY_ALBUM, currentSong.DisplayAlbum)
                                .putString(MediaMetadata.METADATA_KEY_MEDIA_URI, currentSong.CoverArt)
                                .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, ObservableMusicQueue.getInstance().getCurrentAlbumArt())
                                .putBitmap(MediaMetadata.METADATA_KEY_ART, ObservableMusicQueue.getInstance().getCurrentAlbumArt())
                                .build();
                        mediaSession.setMetadata(metadata);
                    }
                }
            });
        }
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
        if(mediaSession != null) {
            mediaSession.release();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}