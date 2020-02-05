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
        Log.d(TAG, "Setting up the notification manager");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "Operating system supports notification channels.");
            __instance = new MediaNotification(mainActivity);
        }
    }

    public Notification notification;

    private MediaNotification(MainActivity mainActivity){
        String description = "Snowgloo controls and information about playing media.";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
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
                            .setContentText(currentSong.Album)
                            .setSubText(currentSong.Artist)
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
