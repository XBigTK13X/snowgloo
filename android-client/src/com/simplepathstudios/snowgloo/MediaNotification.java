package com.simplepathstudios.snowgloo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.drm.DrmStore;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.session.MediaSession;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDeepLinkBuilder;

import com.simplepathstudios.snowgloo.api.model.MusicFile;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;
import com.simplepathstudios.snowgloo.viewmodel.ObservableMusicQueue;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class MediaNotification {
    public static final class Action {
        public static final String PLAY = "com.simplepathstudios.snowgloo.play";
        public static final String PAUSE = "com.simplepathstudios.snowgloo.pause";
        public static final String NEXT = "com.simplepathstudios.snowgloo.next";
        public static final String PREVIOUS = "com.simplepathstudios.snowgloo.previous";
        public static final String VIEW_NOW_PLAYING = "com.simplepathstudios.snowgloo.view-now-playing";
    }
    public static final Integer NOTIFICATION_ID = 776677;
    private static final String TAG = "MediaNotification";
    private static final String NOTIFICATION_NAME = "Snowgloo";
    private static final String NOTIFICATION_CHANNEL_ID = "com.simplepathstudios.snowgloo";
    private static MediaNotification __instance;
    public static MediaNotification getInstance(){
        return __instance;
    }

    public static void registerActivity(MainActivity mainActivity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            __instance = new MediaNotification(mainActivity);
        }
    }

    public Notification notification;

    private Bitmap coverArtBitmap = null;

    private Target coverArtTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            coverArtBitmap = coverArtBitmap;
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    };

    private MediaNotification(MainActivity mainActivity){
        String description = "Snowgloo controls and information about playing media.";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_NAME, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = mainActivity.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        Intent viewNowPlayingIntent = new Intent(MainActivity.getInstance().getApplicationContext(), MainActivity.class);
        viewNowPlayingIntent.setAction(Action.VIEW_NOW_PLAYING);
        PendingIntent viewNowPlayingPendingIntent = PendingIntent.getActivity(MainActivity.getInstance(), (int)System.currentTimeMillis(), viewNowPlayingIntent, 0);

        PendingIntent playIntent = PendingIntent.getBroadcast(Util.getGlobalContext(), 0, new Intent(Action.PLAY), PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Action playAction= new Notification.Action.Builder(R.drawable.ic_play_arrow_white_24dp, "Play", playIntent).build();

        PendingIntent pauseIntent = PendingIntent.getBroadcast(Util.getGlobalContext(), 0, new Intent(Action.PAUSE), PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Action pauseAction= new Notification.Action.Builder(R.drawable.ic_pause_white_24dp, "Pause", pauseIntent).build();

        PendingIntent nextIntent = PendingIntent.getBroadcast(Util.getGlobalContext(), 0, new Intent(Action.NEXT), PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Action nextAction= new Notification.Action.Builder(R.drawable.ic_skip_next_white_24dp, "Next", nextIntent).build();

        PendingIntent previousIntent = PendingIntent.getBroadcast(Util.getGlobalContext(), 0, new Intent(Action.PREVIOUS), PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Action previousAction = new Notification.Action.Builder(R.drawable.ic_skip_previous_white_24dp, "Previous", previousIntent).build();



        ObservableMusicQueue.getInstance().observe(new Observer<MusicQueue>() {
            @Override
            public void onChanged(MusicQueue musicQueue) {
                if(musicQueue != null && musicQueue.currentIndex != null){
                    MusicFile currentSong = musicQueue.getCurrent();

                    if(currentSong.CoverArt != null && !currentSong.CoverArt.isEmpty()){
                        Picasso.get().load(currentSong.CoverArt).into(coverArtTarget);
                    }
                    notification = new Notification.Builder(MainActivity.getInstance(), NOTIFICATION_NAME)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(currentSong.Title)
                            .setContentText(currentSong.DisplayAlbum)
                            .setLargeIcon(coverArtBitmap)
                            .setSubText(currentSong.DisplayArtist)
                            .setStyle(
                                    new Notification.MediaStyle().setMediaSession(SnowglooService.getInstance().getMediaSession().getSessionToken())
                            )
                            .setChannelId(NOTIFICATION_CHANNEL_ID)
                            .setContentIntent(viewNowPlayingPendingIntent)
                            .addAction(previousAction)
                            .addAction(playAction)
                            .addAction(pauseAction)
                            .addAction(nextAction)
                            .build();
                    notificationManager.notify(NOTIFICATION_ID, notification);
                    SnowglooService.getInstance().startForeground(NOTIFICATION_ID, notification);
                }
            }
        });
    }
}