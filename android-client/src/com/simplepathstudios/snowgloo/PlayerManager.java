package com.simplepathstudios.snowgloo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Player.EventListener;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.cast.CastPlayer;
import com.google.android.exoplayer2.ext.cast.DefaultMediaItemConverter;
import com.google.android.exoplayer2.ext.cast.MediaItem;
import com.google.android.exoplayer2.ext.cast.MediaItemConverter;
import com.google.android.exoplayer2.ext.cast.SessionAvailabilityListener;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.gms.cast.MediaQueueItem;
import com.google.android.gms.cast.framework.CastContext;
import com.simplepathstudios.snowgloo.api.model.MusicFile;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;
import com.simplepathstudios.snowgloo.viewmodel.MusicQueueViewModel;

import java.util.EnumSet;

public class PlayerManager implements EventListener, SessionAvailabilityListener {

    private final String TAG = "PlayerManager";
    private final Integer NOTIFICATION_ID = 776677;

    private static final String USER_AGENT = "SnowglooMobile";
    private static final DefaultHttpDataSourceFactory DATA_SOURCE_FACTORY =
            new DefaultHttpDataSourceFactory(USER_AGENT);

    private final boolean PLAY_WHEN_READY = false;

    private final Context context;
    private final PlayerView localPlayerView;
    private final PlayerControlView castControlView;
    private final DefaultTrackSelector trackSelector;
    private final SimpleExoPlayer exoPlayer;
    private final CastPlayer castPlayer;
    private final MediaItemConverter mediaItemConverter;

    private ConcatenatingMediaSource concatenatingMediaSource;
    private PlayerNotificationManager playerNotificationManager;
    private MusicQueueViewModel musicQueueViewModel;
    private Integer currentItemIndex;
    private Player currentPlayer;

    private boolean skipNextTrackMonitor = false;

    public PlayerManager(
            MainActivity mainActivity,
            PlayerView localPlayerView,
            PlayerControlView castControlView,
            Context context,
            CastContext castContext) {
        this.context = context;
        this.localPlayerView = localPlayerView;
        localPlayerView.setUseArtwork(false);
        this.castControlView = castControlView;
        currentItemIndex = null;
        concatenatingMediaSource = new ConcatenatingMediaSource();
        mediaItemConverter = new DefaultMediaItemConverter();
        musicQueueViewModel = new ViewModelProvider(mainActivity).get(MusicQueueViewModel.class);
        musicQueueViewModel.Data.observe(mainActivity, new Observer<MusicQueue>() {
            @Override
            public void onChanged(MusicQueue musicQueue) {
                handleUpdate(musicQueue);
            }
        });

        trackSelector = new DefaultTrackSelector(context);
        exoPlayer = new SimpleExoPlayer.Builder(context).setTrackSelector(trackSelector).build();
        exoPlayer.addListener(this);

        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(
                context,
                "com.simplepathstudios.snowgloo",
                R.string.notification_channel_name,
                R.string.notification_channel_description,
                NOTIFICATION_ID,
                new SnowglooNotificationAdapter(this),
                new PlayerNotificationManager.NotificationListener() {
            @Override
            public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
            }

            @Override
            public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {

            }
        });
        playerNotificationManager.setUseNavigationActionsInCompactView(true);
        playerNotificationManager.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        playerNotificationManager.setPlayer(exoPlayer);

        localPlayerView.setPlayer(exoPlayer);
        localPlayerView.setControllerShowTimeoutMs(0);
        localPlayerView.setControllerHideOnTouch(false);

        castPlayer = new CastPlayer(castContext);
        castPlayer.addListener(this);
        castPlayer.setSessionAvailabilityListener(this);
        castControlView.setPlayer(castPlayer);

        setCurrentPlayer(castPlayer.isCastSessionAvailable() ? castPlayer : exoPlayer);
    }

    private void handleUpdate(MusicQueue musicQueue){
        Log.d(TAG,"Updating music player with new queue " + musicQueue.songs.size() + " because " +musicQueue.updateReason);
        MusicQueue.UpdateReason updateReason = musicQueue.updateReason;
        if(updateReason == MusicQueue.UpdateReason.INITIALIZE){
            return;
        }
        concatenatingMediaSource = new ConcatenatingMediaSource();
        for(MusicFile musicFile : musicQueue.songs){
            concatenatingMediaSource.addMediaSource(buildMediaSource(musicFile));
        }
        long currentSeekPosition = 0L;
        if(EnumSet.of(MusicQueue.UpdateReason.ITEM_MOVED, MusicQueue.UpdateReason.ITEM_REMOVED, MusicQueue.UpdateReason.ITEM_ADDED).contains(updateReason)){
            currentSeekPosition = currentPlayer.getCurrentPosition();
        }
        if(EnumSet.of(MusicQueue.UpdateReason.CURRENT_INDEX_CHANGED, MusicQueue.UpdateReason.SHUFFLE, MusicQueue.UpdateReason.ITEM_MOVED, MusicQueue.UpdateReason.ITEM_REMOVED, MusicQueue.UpdateReason.ITEM_ADDED).contains(updateReason)){
            skipNextTrackMonitor = true;
        }
        if(currentPlayer == exoPlayer){
            exoPlayer.prepare(concatenatingMediaSource);
        }
        if(musicQueue.currentIndex != null) {
            if(currentPlayer == castPlayer){
                MediaQueueItem[] items = new MediaQueueItem[musicQueue.songs.size()];
                for (int i = 0; i < items.length; i++) {
                    MusicFile item = musicQueue.songs.get(i);
                    items[i] = mediaItemConverter.toMediaQueueItem(musicToMedia(item));
                }
                castPlayer.loadItems(items, musicQueue.currentIndex, currentSeekPosition, Player.REPEAT_MODE_OFF);
            }
            else {
                if(updateReason == MusicQueue.UpdateReason.CURRENT_INDEX_CHANGED){
                    currentPlayer.seekTo(musicQueue.currentIndex, currentSeekPosition);
                    currentPlayer.setPlayWhenReady(true);
                }else{
                    currentPlayer.setPlayWhenReady(false);
                }
            }
        } else {
            currentPlayer.setPlayWhenReady(false);
        }
        currentItemIndex = musicQueue.currentIndex;
    }

    private MediaItem musicToMedia(MusicFile musicFile){
        return new MediaItem.Builder()
                .setMimeType("audio/mpeg")
                .setTitle(musicFile.Title)
                .setUri(musicFile.AudioUrl)
                .build();
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d(TAG,"dispatchKeyEvent");
        if (currentPlayer == exoPlayer) {
            return localPlayerView.dispatchKeyEvent(event);
        } else {
            return castControlView.dispatchKeyEvent(event);
        }
    }

    public void release() {
        Log.d(TAG, "Released the PlayerManager");
        currentItemIndex = C.INDEX_UNSET;
        concatenatingMediaSource.clear();
        castPlayer.setSessionAvailabilityListener(null);
        castPlayer.release();
        localPlayerView.setPlayer(null);
        exoPlayer.release();
        playerNotificationManager.setPlayer(null);
        playerNotificationManager = null;
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) this.context.getSystemService(ns);
        nMgr.cancel(NOTIFICATION_ID);
    }

    // Player.EventListener implementation.
    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        // This should only matter when one track starts immediately after the end of another in the queue.
        // The event gets fired left right and everywhere, thus the janky boolean to turn it off except when needed.
        Log.d(TAG,"Tracks changed");
        Integer playerIndex;
        if(currentPlayer == exoPlayer){
            playerIndex = exoPlayer.getCurrentWindowIndex();
        } else {
            playerIndex = castPlayer.getCurrentWindowIndex();
        }
        if(currentItemIndex != null && playerIndex != currentItemIndex){
            if(skipNextTrackMonitor){
                skipNextTrackMonitor = false;
            } else{
                musicQueueViewModel.setCurrentIndex(playerIndex);
            }
        }
    }

    // CastPlayer.SessionAvailabilityListener implementation.

    @Override
    public void onCastSessionAvailable() {
        setCurrentPlayer(castPlayer);
    }

    @Override
    public void onCastSessionUnavailable() {
        setCurrentPlayer(exoPlayer);
    }

    private void updateCurrentItemIndex() {
        int playbackState = currentPlayer.getPlaybackState();
        if(playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED){
            int playerIndex = currentPlayer.getCurrentWindowIndex();
            musicQueueViewModel.setCurrentIndex(playerIndex);
        } else {
            musicQueueViewModel.setCurrentIndex(null);
        }
    }

    private void setCurrentPlayer(Player currentPlayer) {
        Log.d(TAG,"setCurrentPlayer");
        if (this.currentPlayer == currentPlayer) {
            return;
        }

        if (currentPlayer == exoPlayer) {
            localPlayerView.setVisibility(View.VISIBLE);
            castControlView.hide();
        } else  {
            localPlayerView.setVisibility(View.GONE);
            castControlView.show();
        }

        long playbackPositionMs = C.TIME_UNSET;
        int windowIndex = C.INDEX_UNSET;
        boolean playWhenReady = false;

        Player previousPlayer = this.currentPlayer;
        if (previousPlayer != null) {
            int playbackState = previousPlayer.getPlaybackState();
            if (playbackState != Player.STATE_ENDED) {
                playbackPositionMs = previousPlayer.getCurrentPosition();
                playWhenReady = previousPlayer.getPlayWhenReady();
                windowIndex = previousPlayer.getCurrentWindowIndex();
                if (currentItemIndex != null && windowIndex != currentItemIndex) {
                    playbackPositionMs = C.TIME_UNSET;
                    windowIndex = currentItemIndex;
                }
            }
            skipNextTrackMonitor = true;
            previousPlayer.stop(true);
        }

        this.currentPlayer = currentPlayer;

        if (currentPlayer == exoPlayer) {
            exoPlayer.prepare(concatenatingMediaSource);
        }

        if (windowIndex != C.INDEX_UNSET) {
            currentPlayer.seekTo(windowIndex, playbackPositionMs);
            currentPlayer.setPlayWhenReady(true);
        }
    }

    public MusicFile getCurrentMusic(){
        return musicQueueViewModel.getCurrent();
    }

    private MediaSource buildMediaSource(MusicFile item) {
        Uri uri = Uri.parse(item.AudioUrl);
        MediaSource createdMediaSource = new ProgressiveMediaSource.Factory(DATA_SOURCE_FACTORY)
                        .createMediaSource(uri);
        return createdMediaSource;
    }

    private class SnowglooNotificationAdapter implements PlayerNotificationManager.MediaDescriptionAdapter {

        private final PlayerManager playerManager;

        public SnowglooNotificationAdapter(PlayerManager playerManager){
            this.playerManager = playerManager;
        }

        @Override
        public String getCurrentSubText(Player player) {
            return null;
        }

        @Override
        public String getCurrentContentTitle(Player player) {
            return playerManager.getCurrentMusic().Title;
        }

        @Override
        public PendingIntent createCurrentContentIntent(Player player) {
            return null;
        }

        @Override
        public String getCurrentContentText(Player player) {
            return null;
        }

        @Override
        public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {

            return null;
        }
    };
}

