package com.simplepathstudios.snowgloo;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;

import androidx.annotation.Nullable;

import android.content.Context;
import android.util.Log;

import java.io.IOException;


public class BackgroundMusicService extends Service {

    MediaPlayer mPlayer = null;
    private final static int MAX_VOLUME = 100;
    AudioManager.OnAudioFocusChangeListener afChangeListener;
    private int length;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String audioUrl = (String) intent.getExtras().get("audioUrl");
        Log.d("BackgroundMusicService", audioUrl);
        playMusic(Uri.parse(audioUrl));
        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) {
            try {
                mPlayer.stop();
                mPlayer.release();
            } finally {
                mPlayer = null;
            }
        }
    }

    public void onTaskRemoved(Intent rootIntent) {
        stopSelf();
    }

    /*
     * playmusic custom method for manage two different background sounds for application
     * */

    public void playMusic(Uri audioUrl) {
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                try {
                    mPlayer.stop();
                    mPlayer.release();
                    mPlayer = MediaPlayer.create(this, audioUrl);

                    AudioManager am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                    int result = am.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

                    if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        // Start playback.
                        mPlayer.setLooping(true);
                        final float volume = (float) (1 - (Math.log(MAX_VOLUME - 85) / Math.log(MAX_VOLUME)));
                        mPlayer.setVolume(volume, volume);
                        mPlayer.start();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    mPlayer = MediaPlayer.create(this, audioUrl);

                    AudioManager am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                    int result = am.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

                    if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        // Start playback.
                        mPlayer.setLooping(true);
                        final float volume = (float) (1 - (Math.log(MAX_VOLUME - 85) / Math.log(MAX_VOLUME)));
                        mPlayer.setVolume(volume, volume);
                        mPlayer.prepare();
                        mPlayer.start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        } else {
            try {
                mPlayer = MediaPlayer.create(this, audioUrl);

                AudioManager am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                int result = am.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    // Start playback.
                    mPlayer.setLooping(true);
                    final float volume = (float) (1 - (Math.log(MAX_VOLUME - 85) / Math.log(MAX_VOLUME)));
                    mPlayer.setVolume(volume, volume);
                    mPlayer.start();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * MediaPlayer methods
     * */

    public void pauseMusic() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            length = mPlayer.getCurrentPosition();

        }
    }

    public void resumeMusic() {
        if (mPlayer.isPlaying() == false) {
            mPlayer.seekTo(length);
            mPlayer.start();
        }
    }

    public void stopMusic() {
        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;
    }

    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (mPlayer != null) {
            try {
                mPlayer.stop();
                mPlayer.release();
            } finally {
                mPlayer = null;
            }
        }
        return false;
    }

}