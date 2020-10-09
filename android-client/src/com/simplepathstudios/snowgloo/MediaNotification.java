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

        Intent playIntent = new Intent(MainActivity.getInstance().getApplicationContext(), MainActivity.class);
        playIntent.setAction("notification-play");
        PendingIntent pendingPlayIntent = PendingIntent.getActivity(MainActivity.getInstance(), (int)System.currentTimeMillis(), playIntent, 0);
        Notification.Action playAction = new Notification.Action.Builder(R.drawable.ic_play_arrow_white_24dp, "Play", pendingPlayIntent).build();

        Intent pauseIntent = new Intent(MainActivity.getInstance().getApplicationContext(), MainActivity.class);
        pauseIntent.setAction("notification-pause");
        PendingIntent pendingPauseIntent = PendingIntent.getActivity(MainActivity.getInstance(), (int)System.currentTimeMillis(), pauseIntent, 0);
        Notification.Action pauseAction = new Notification.Action.Builder(R.drawable.ic_pause_white_24dp, "Pause", pendingPlayIntent).build();

        Intent nextIntent = new Intent(MainActivity.getInstance().getApplicationContext(), MainActivity.class);
        nextIntent.setAction("notification-next");
        PendingIntent pendingNextIntent = PendingIntent.getActivity(MainActivity.getInstance(), (int)System.currentTimeMillis(), nextIntent, 0);
        Notification.Action nextAction = new Notification.Action.Builder(R.drawable.ic_skip_next_white_24dp, "Next", pendingNextIntent).build();

        Intent previousIntent = new Intent(MainActivity.getInstance().getApplicationContext(), MainActivity.class);
        previousIntent.setAction("notification-previous");
        PendingIntent pendingPreviousIntent = PendingIntent.getActivity(MainActivity.getInstance(), (int)System.currentTimeMillis(), previousIntent, 0);
        Notification.Action previousAction = new Notification.Action.Builder(R.drawable.ic_skip_previous_white_24dp, "Previous", pendingPreviousIntent).build();

        ObservableMusicQueue.getInstance().observe(new Observer<MusicQueue>() {
            @Override
            public void onChanged(MusicQueue musicQueue) {
                if(musicQueue != null && musicQueue.currentIndex != null){
                    MusicFile currentSong = musicQueue.getCurrent();

                    Intent intent = new Intent(MainActivity.getInstance().getApplicationContext(), MainActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.getInstance(), (int)System.currentTimeMillis(), intent, 0);

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
                            .setContentIntent(pendingIntent)
                            .addAction(previousAction)
                            .addAction(playAction)
                            .addAction(pauseAction)
                            .addAction(nextAction)
                            .build();
                    //.setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    //.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    notificationManager.notify(NOTIFICATION_ID, notification);
                    SnowglooService.getInstance().startForeground(NOTIFICATION_ID, notification);
                }
            }
        });
    }
}

/* Taken from old exoplayer based manager
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
 */