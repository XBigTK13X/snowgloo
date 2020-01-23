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

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Player.DiscontinuityReason;
import com.google.android.exoplayer2.Player.EventListener;
import com.google.android.exoplayer2.Player.TimelineChangeReason;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.Timeline.Period;
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
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.gms.cast.MediaQueueItem;
import com.google.android.gms.cast.framework.CastContext;
import com.simplepathstudios.snowgloo.api.model.MusicFile;

import java.util.ArrayList;

class PlayerManager implements EventListener, SessionAvailabilityListener {

    private final String TAG = "PlayerManager";
    private final Integer NOTIFICATION_ID = 776677;

    interface AudioListener {
        void onUnsupportedTrack(int trackType);
        void onTrackMetadataChange(MusicFile musicFile);
    }
    interface QueueListener {
        void onQueuePositionChanged(int previousIndex, int newIndex);
    }

    private static final String USER_AGENT = "SnowglooMobile";
    private static final DefaultHttpDataSourceFactory DATA_SOURCE_FACTORY =
            new DefaultHttpDataSourceFactory(USER_AGENT);

    private final Context context;
    private final PlayerView localPlayerView;
    private final PlayerControlView castControlView;
    private final DefaultTrackSelector trackSelector;
    private final SimpleExoPlayer exoPlayer;
    private final CastPlayer castPlayer;
    private final ArrayList<MusicFile> mediaQueue;
    private final AudioListener audioListener;
    private final QueueListener queueListener;
    private final ConcatenatingMediaSource concatenatingMediaSource;
    private final MediaItemConverter mediaItemConverter;
    private PlayerNotificationManager playerNotificationManager;

    private TrackGroupArray lastSeenTrackGroupArray;
    private int currentItemIndex;
    private Player currentPlayer;

    /**
     * Creates a new manager for {@link SimpleExoPlayer} and {@link CastPlayer}.
     *
     * @param localPlayerView The {@link PlayerView} for local playback.
     * @param castControlView The {@link PlayerControlView} to control remote playback.
     * @param context A {@link Context}.
     * @param castContext The {@link CastContext}.
     */
    public PlayerManager(
            AudioListener audioListener,
            QueueListener queueListener,
            PlayerView localPlayerView,
            PlayerControlView castControlView,
            Context context,
            CastContext castContext) {
        this.context = context;
        this.audioListener = audioListener;
        this.queueListener = queueListener;
        this.localPlayerView = localPlayerView;
        this.castControlView = castControlView;
        mediaQueue = new ArrayList<>();
        currentItemIndex = C.INDEX_UNSET;
        concatenatingMediaSource = new ConcatenatingMediaSource();
        mediaItemConverter = new DefaultMediaItemConverter();

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

    // Queue manipulation methods.

    /**
     * Plays a specified queue item in the current player.
     *
     * @param itemIndex The index of the item to play.
     */
    public void selectQueueItem(int itemIndex) {
        setCurrentItem(itemIndex, C.TIME_UNSET, true);
    }

    /** Returns the index of the currently played item. */
    public int getCurrentItemIndex() {
        return currentItemIndex;
    }

    /**
     * Appends {@code item} to the media queue.
     *
     * @param item The {@link MediaItem} to append.
     */
    public void addItem(MusicFile item) {
        mediaQueue.add(item);
        concatenatingMediaSource.addMediaSource(buildMediaSource(item));
        if (currentPlayer == castPlayer) {
            castPlayer.addItems(mediaItemConverter.toMediaQueueItem(musicToMedia(item)));
        }
    }

    /** Returns the size of the media queue. */
    public int getMediaQueueSize() {
        return mediaQueue.size();
    }

    /**
     * Returns the item at the given index in the media queue.
     *
     * @param position The index of the item.
     * @return The item at the given index in the media queue.
     */
    public MusicFile getItem(int position) {
        return mediaQueue.get(position);
    }

    private MediaItem musicToMedia(MusicFile musicFile){
        return new MediaItem.Builder()
                .setMimeType("audio/mpeg")
                .setTitle(musicFile.Title)
                .setUri(musicFile.AudioUrl)
                .build();
    }

    /**
     * Removes the item at the given index from the media queue.
     *
     * @param item The item to remove.
     * @return Whether the removal was successful.
     */
    public boolean removeItem(MusicFile item) {
        int itemIndex = mediaQueue.indexOf(item);
        if (itemIndex == -1) {
            return false;
        }
        concatenatingMediaSource.removeMediaSource(itemIndex);
        if (currentPlayer == castPlayer) {
            if (castPlayer.getPlaybackState() != Player.STATE_IDLE) {
                Timeline castTimeline = castPlayer.getCurrentTimeline();
                if (castTimeline.getPeriodCount() <= itemIndex) {
                    return false;
                }
                castPlayer.removeItem((int) castTimeline.getPeriod(itemIndex, new Period()).id);
            }
        }
        mediaQueue.remove(itemIndex);
        if (itemIndex == currentItemIndex && itemIndex == mediaQueue.size()) {
            maybeSetCurrentItemAndNotify(C.INDEX_UNSET);
        } else if (itemIndex < currentItemIndex) {
            maybeSetCurrentItemAndNotify(currentItemIndex - 1);
        }
        return true;
    }

    /**
     * Moves an item within the queue.
     *
     * @param item The item to move.
     * @param toIndex The target index of the item in the queue.
     * @return Whether the item move was successful.
     */
    public boolean moveItem(MusicFile item, int toIndex) {
        int fromIndex = mediaQueue.indexOf(item);
        if (fromIndex == -1) {
            return false;
        }
        // Player update.
        concatenatingMediaSource.moveMediaSource(fromIndex, toIndex);
        if (currentPlayer == castPlayer && castPlayer.getPlaybackState() != Player.STATE_IDLE) {
            Timeline castTimeline = castPlayer.getCurrentTimeline();
            int periodCount = castTimeline.getPeriodCount();
            if (periodCount <= fromIndex || periodCount <= toIndex) {
                return false;
            }
            int elementId = (int) castTimeline.getPeriod(fromIndex, new Period()).id;
            castPlayer.moveItem(elementId, toIndex);
        }

        mediaQueue.add(toIndex, mediaQueue.remove(fromIndex));

        // Index update.
        if (fromIndex == currentItemIndex) {
            maybeSetCurrentItemAndNotify(toIndex);
        } else if (fromIndex < currentItemIndex && toIndex >= currentItemIndex) {
            maybeSetCurrentItemAndNotify(currentItemIndex - 1);
        } else if (fromIndex > currentItemIndex && toIndex <= currentItemIndex) {
            maybeSetCurrentItemAndNotify(currentItemIndex + 1);
        }

        return true;
    }

    /**
     * Dispatches a given {@link KeyEvent} to the corresponding view of the current player.
     *
     * @param event The {@link KeyEvent}.
     * @return Whether the event was handled by the target view.
     */
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (currentPlayer == exoPlayer) {
            return localPlayerView.dispatchKeyEvent(event);
        } else /* currentPlayer == castPlayer */ {
            return castControlView.dispatchKeyEvent(event);
        }
    }

    /** Releases the manager and the players that it holds. */
    public void release() {
        Log.d(TAG, "Released the PlayerManager");
        currentItemIndex = C.INDEX_UNSET;
        mediaQueue.clear();
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
    public void onPlayerStateChanged(boolean playWhenReady, @Player.State int playbackState) {
        updateCurrentItemIndex();
    }

    @Override
    public void onPositionDiscontinuity(@DiscontinuityReason int reason) {
        updateCurrentItemIndex();
    }

    @Override
    public void onTimelineChanged(Timeline timeline, @TimelineChangeReason int reason) {
        updateCurrentItemIndex();
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        if (currentPlayer == exoPlayer && trackGroups != lastSeenTrackGroupArray) {
            MappingTrackSelector.MappedTrackInfo mappedTrackInfo =
                    trackSelector.getCurrentMappedTrackInfo();
            if (mappedTrackInfo != null) {
                if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_VIDEO)
                        == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                    audioListener.onUnsupportedTrack(C.TRACK_TYPE_VIDEO);
                }
                if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_AUDIO)
                        == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                    audioListener.onUnsupportedTrack(C.TRACK_TYPE_AUDIO);
                }
            }
            lastSeenTrackGroupArray = trackGroups;
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

    // Internal methods.

    private void updateCurrentItemIndex() {
        int playbackState = currentPlayer.getPlaybackState();
        maybeSetCurrentItemAndNotify(
                playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED
                        ? currentPlayer.getCurrentWindowIndex()
                        : C.INDEX_UNSET);
    }

    private void setCurrentPlayer(Player currentPlayer) {
        if (this.currentPlayer == currentPlayer) {
            return;
        }

        // View management.
        if (currentPlayer == exoPlayer) {
            localPlayerView.setVisibility(View.VISIBLE);
            castControlView.hide();
        } else /* currentPlayer == castPlayer */ {
            localPlayerView.setVisibility(View.GONE);
            castControlView.show();
        }

        // Player state management.
        long playbackPositionMs = C.TIME_UNSET;
        int windowIndex = C.INDEX_UNSET;
        boolean playWhenReady = false;

        Player previousPlayer = this.currentPlayer;
        if (previousPlayer != null) {
            // Save state from the previous player.
            int playbackState = previousPlayer.getPlaybackState();
            if (playbackState != Player.STATE_ENDED) {
                playbackPositionMs = previousPlayer.getCurrentPosition();
                playWhenReady = previousPlayer.getPlayWhenReady();
                windowIndex = previousPlayer.getCurrentWindowIndex();
                if (windowIndex != currentItemIndex) {
                    playbackPositionMs = C.TIME_UNSET;
                    windowIndex = currentItemIndex;
                }
            }
            previousPlayer.stop(true);
        }

        this.currentPlayer = currentPlayer;

        // Media queue management.
        if (currentPlayer == exoPlayer) {
            exoPlayer.prepare(concatenatingMediaSource);
        }

        // Playback transition.
        if (windowIndex != C.INDEX_UNSET) {
            setCurrentItem(windowIndex, playbackPositionMs, playWhenReady);
        }
    }

    /**
     * Starts playback of the item at the given position.
     *
     * @param itemIndex The index of the item to play.
     * @param positionMs The position at which playback should start.
     * @param playWhenReady Whether the player should proceed when ready to do so.
     */
    private void setCurrentItem(int itemIndex, long positionMs, boolean playWhenReady) {
        maybeSetCurrentItemAndNotify(itemIndex);
        if (currentPlayer == castPlayer && castPlayer.getCurrentTimeline().isEmpty()) {
            MediaQueueItem[] items = new MediaQueueItem[mediaQueue.size()];
            for (int i = 0; i < items.length; i++) {
                MusicFile item = mediaQueue.get(i);
                items[i] = mediaItemConverter.toMediaQueueItem(musicToMedia(item));
            }
            castPlayer.loadItems(items, itemIndex, positionMs, Player.REPEAT_MODE_OFF);
        } else {
            currentPlayer.seekTo(itemIndex, positionMs);
            currentPlayer.setPlayWhenReady(playWhenReady);
        }
    }

    private void maybeSetCurrentItemAndNotify(int currentItemIndex) {
        if (this.currentItemIndex != currentItemIndex) {
            int oldIndex = this.currentItemIndex;
            this.currentItemIndex = currentItemIndex;
            queueListener.onQueuePositionChanged(oldIndex, currentItemIndex);
            audioListener.onTrackMetadataChange(getCurrentMusic());
        }
    }

    public MusicFile getCurrentMusic(){
        if(mediaQueue.size() > currentItemIndex && currentItemIndex > -1){
            return mediaQueue.get(currentItemIndex);
        }
        return MusicFile.EMPTY;
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

