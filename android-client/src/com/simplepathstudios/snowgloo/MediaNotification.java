package com.simplepathstudios.snowgloo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDeepLinkBuilder;

import com.simplepathstudios.snowgloo.api.model.MusicFile;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;
import com.simplepathstudios.snowgloo.viewmodel.ObservableMusicQueue;

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

    private MediaNotification(MainActivity mainActivity){
        String description = "Snowgloo controls and information about playing media.";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_NAME, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = mainActivity.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        ObservableMusicQueue.getInstance().observe(new Observer<MusicQueue>() {
            @Override
            public void onChanged(MusicQueue musicQueue) {
                if(musicQueue != null && musicQueue.currentIndex != null){
                    MusicFile currentSong = musicQueue.getCurrent();
                    PendingIntent nowPlayingPendingIntent = new NavDeepLinkBuilder(mainActivity)
                            .setGraph(R.navigation.main_nav)
                            .setDestination(R.id.now_playing_fragment)
                            .createPendingIntent();

                    notification = new NotificationCompat.Builder(MainActivity.getInstance(), NOTIFICATION_NAME)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(currentSong.Title)
                            .setContentText(currentSong.DisplayAlbum)
                            .setSubText(currentSong.DisplayArtist)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setChannelId(NOTIFICATION_CHANNEL_ID)
                            .setContentIntent(nowPlayingPendingIntent)
                            .build();
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