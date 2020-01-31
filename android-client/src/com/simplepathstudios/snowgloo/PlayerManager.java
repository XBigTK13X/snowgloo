package com.simplepathstudios.snowgloo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
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
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class PlayerManager {

    private static final String USER_AGENT = "SnowglooMobile";
    private static final DefaultHttpDataSourceFactory DATA_SOURCE_FACTORY =  new DefaultHttpDataSourceFactory(USER_AGENT);
    private final String TAG = "PlayerManager";
    private final Integer NOTIFICATION_ID = 776677;

    private final Context context;
    private final PlayerView localPlayerView;
    private final MediaItemConverter mediaItemConverter;
    private final PlayerControlView castControlView;
    private final DefaultTrackSelector trackSelector;
    private final SimpleExoPlayer localPlayer;
    private final CastPlayer castPlayer;
    private Player currentPlayer;


    private ConcatenatingMediaSource concatenatingMediaSource;
    private PlayerNotificationManager playerNotificationManager;
    private MusicQueueViewModel musicQueueViewModel;
    private Integer currentItemIndex;

    private String lastPreparedContentHash;
    public MusicQueue.UpdateReason lastUpdateReason;


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
        localPlayer = new SimpleExoPlayer.Builder(context).setTrackSelector(trackSelector).build();
        localPlayer.addListener(new EventListener() {
            @Override
            public void onPositionDiscontinuity(@Player.DiscontinuityReason int reason) {
                // Track finished playing or the forward/backward button was pressed.
                if(reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION || reason == Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT){
                    Log.d(TAG, String.format("Local player discontinuity reason => [%d]",reason));
                    int playerIndex = currentPlayer.getCurrentWindowIndex();
                    if(currentItemIndex != null && currentItemIndex != playerIndex){
                        musicQueueViewModel.setCurrentIndex(playerIndex, MusicQueueViewModel.SelectionMode.PlayerAction);
                    }
                }
            }
        });

        localPlayerView.setPlayer(localPlayer);
        localPlayerView.setControllerShowTimeoutMs(0);
        localPlayerView.setControllerHideOnTouch(false);

        castPlayer = new CastPlayer(castContext);
        castPlayer.addListener(new EventListener() {
            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                // Chromecast finished playing the last track, or back/forward was pressed
                int playerIndex = currentPlayer.getCurrentWindowIndex();
                Log.d(TAG, String.format("Cast tracks changed currentIndex => %d playerIndex => %d", currentItemIndex, playerIndex));
                if(currentItemIndex != null && currentItemIndex != playerIndex){
                    Log.d(TAG, "Firing off an update from the player");
                    musicQueueViewModel.setCurrentIndex(playerIndex, MusicQueueViewModel.SelectionMode.PlayerAction);
                }
            }
        });
        castPlayer.setSessionAvailabilityListener(new SessionAvailabilityListener() {
            @Override
            public void onCastSessionAvailable() {
                setCurrentPlayer(castPlayer);
            }

            @Override
            public void onCastSessionUnavailable() {
                setCurrentPlayer(localPlayer);
            }
        });
        castControlView.setPlayer(castPlayer);

        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(
                context,
                "com.simplepathstudios.snowgloo",
                R.string.notification_channel_name,
                R.string.notification_channel_description,
                NOTIFICATION_ID,
                new SnowglooNotificationAdapter(),
                new PlayerNotificationManager.NotificationListener() {
                    @Override
                    public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
                    }

                    @Override
                    public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                        Log.d(TAG, "The notification was cancelled");
                    }
                });
        playerNotificationManager.setUseNavigationActionsInCompactView(true);
        playerNotificationManager.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        setCurrentPlayer(castPlayer.isCastSessionAvailable() ? castPlayer : localPlayer);
    }

    private void handleUpdate(MusicQueue musicQueue){
        Log.d(TAG,"Updating music player with new queue of " + musicQueue.songs.size() + " songs and currentIndex " + musicQueue.currentIndex + " because " +musicQueue.updateReason);
        MusicQueue.UpdateReason updateReason = musicQueue.updateReason;
        lastUpdateReason = musicQueue.updateReason;
        currentItemIndex = musicQueue.currentIndex;
        if(updateReason == MusicQueue.UpdateReason.SERVER_RELOAD || updateReason == MusicQueue.UpdateReason.TRACK_CHANGED){
            return;
        }

        String currentContentHash = musicQueue.contentHash();

        boolean shouldSeek = MusicQueue.UpdateReason.shouldSeek(updateReason);
        boolean shouldPlay = MusicQueue.UpdateReason.shouldPlay(updateReason);

        final long currentSeekPosition = shouldSeek ? currentPlayer.getCurrentPosition() : 0L;
        if(currentPlayer == localPlayer){
            if(!currentContentHash.equals(lastPreparedContentHash)){
                concatenatingMediaSource = new ConcatenatingMediaSource();
                for(MusicFile musicFile : musicQueue.songs){
                    concatenatingMediaSource.addMediaSource(buildMediaSource(musicFile));
                }
                localPlayer.prepare(concatenatingMediaSource);
            }

            if(shouldSeek || shouldPlay){
                localPlayer.seekTo(musicQueue.currentIndex == null ? 0 : musicQueue.currentIndex, currentSeekPosition);
            }
            if(shouldPlay){
                localPlayer.setPlayWhenReady(true);
            }else{
                localPlayer.setPlayWhenReady(false);
            }
        } else {
            if(!currentContentHash.equals(lastPreparedContentHash)) {
                MediaQueueItem[] items = new MediaQueueItem[musicQueue.songs.size()];
                for (int i = 0; i < items.length; i++) {
                    MusicFile item = musicQueue.songs.get(i);
                    items[i] = mediaItemConverter.toMediaQueueItem(musicToMedia(item));
                }
                castPlayer.setPlayWhenReady(musicQueue.currentIndex == null || !shouldPlay);
                castPlayer.loadItems(items, musicQueue.currentIndex == null ? 0 : musicQueue.currentIndex, currentSeekPosition, Player.REPEAT_MODE_OFF);
            } else {
                castPlayer.seekTo(musicQueue.currentIndex == null ? 0: musicQueue.currentIndex, currentSeekPosition);
            }

        }
        lastPreparedContentHash = currentContentHash;
    }

    private MediaItem musicToMedia(MusicFile musicFile){
        return new MediaItem.Builder()
                .setMimeType("audio/mpeg")
                .setTitle(musicFile.Title)
                .setUri(musicFile.AudioUrl)
                .build();
    }

    public void release() {
        Log.d(TAG, "Released the PlayerManager");
        currentItemIndex = C.INDEX_UNSET;
        concatenatingMediaSource.clear();
        castPlayer.setSessionAvailabilityListener(null);
        castPlayer.release();
        localPlayerView.setPlayer(null);
        localPlayer.release();
        playerNotificationManager.setPlayer(null);
        playerNotificationManager = null;
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) this.context.getSystemService(ns);
        nMgr.cancel(NOTIFICATION_ID);
    }


    private void setCurrentPlayer(Player currentPlayer) {
        if (this.currentPlayer == currentPlayer) {
            return;
        }

        Log.d(TAG,"setCurrentPlayer");

        playerNotificationManager.setPlayer(currentPlayer);
        lastPreparedContentHash = null;

        if (currentPlayer == localPlayer) {
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
            previousPlayer.stop(true);
        }

        this.currentPlayer = currentPlayer;

        if (currentPlayer == localPlayer) {
            localPlayer.prepare(concatenatingMediaSource);
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
        public SnowglooNotificationAdapter(){
        }

        @Override
        public String getCurrentSubText(Player player) {
            return getCurrentMusic().Artist;
        }

        @Override
        public String getCurrentContentTitle(Player player) {
            return getCurrentMusic().Title;
        }

        @Override
        public PendingIntent createCurrentContentIntent(Player player) {
            return null;
        }

        @Override
        public String getCurrentContentText(Player player) {
            return getCurrentMusic().Album;
        }

        @Override
        public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
            Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    callback.onBitmap(bitmap);
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            };
            Picasso.get().load(getCurrentMusic().CoverArt).into(target);
            return null;
        }
    };
}


/*            @Override
            public void onPlayerStateChanged(boolean playWhenReady, @Player.State int playbackState) {
                int playerIndex = currentPlayer.getCurrentWindowIndex();
                Log.d(TAG, String.format("Cast player state changed currentIndex => %d playerIndex => %d playbackState => %d playWhenReady => %b",currentItemIndex, playerIndex, playbackState, playWhenReady));
                if(playbackState == Player.STATE_READY){
                    if(currentItemIndex != null && currentItemIndex != playerIndex){
                        musicQueueViewModel.setCurrentIndex(playerIndex, MusicQueueViewModel.SelectionMode.PlayerAction);
                    }
                }
            }
            @Override
            public void onPositionDiscontinuity(@Player.DiscontinuityReason int reason) {
                int playerIndex = currentPlayer.getCurrentWindowIndex();
                Log.d(TAG, String.format("Cast position discontinuity currentIndex => %d playerIndex => %d", currentItemIndex, playerIndex));
            }*/

/*

    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d(TAG,"dispatchKeyEvent");
        if (currentPlayer == localPlayer) {
            return localPlayerView.dispatchKeyEvent(event);
        } else {
            return castControlView.dispatchKeyEvent(event);
        }
    }*/