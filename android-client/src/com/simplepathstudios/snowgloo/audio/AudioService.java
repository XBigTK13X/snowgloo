package com.simplepathstudios.snowgloo.audio;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AudioService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    public void onDestroy() {
        AudioPlayer.getInstance().destroy();
        stopSelf();
        super.onDestroy();
    }
}
