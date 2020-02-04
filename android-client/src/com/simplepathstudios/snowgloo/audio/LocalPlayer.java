package com.simplepathstudios.snowgloo.audio;

import android.media.MediaPlayer;
import android.util.Log;

public class LocalPlayer implements IAudioPlayer {
    private static final String TAG = "LocalPlayer";
    private MediaPlayer mediaPlayer;
    private String currentUrl;



    public LocalPlayer(){
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    if (extra == MediaPlayer.MEDIA_ERROR_SERVER_DIED
                            || extra == MediaPlayer.MEDIA_ERROR_MALFORMED) {
                        Log.d(TAG,"erroronplaying");
                    } else if (extra == MediaPlayer.MEDIA_ERROR_IO) {
                        Log.d(TAG,"erroronplaying");
                        return false;
                    }
                    return false;
                }
            });
            mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {

                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    Log.d(TAG,"onBufferingUpdate" + percent);

                }
            });

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                    Log.d(TAG,"playing");
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.d(TAG,"completed");
                }
            });
            mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    return false;
                }
            });
        } catch (Exception e) {
            Log.e(TAG,"An error occurred with the local media player",e);
        }
    }

    @Override
    public void play(String url) {
        if(currentUrl != url){
            try{
                mediaPlayer.reset();
                mediaPlayer.setDataSource(url);
                mediaPlayer.prepareAsync();
                currentUrl = url;
            } catch(Exception e){
                Log.e(TAG, "An error occurred while playing",e);
            }
        }
        else {
            mediaPlayer.start();
        }
    }

    @Override
    public void stop() {
        mediaPlayer.stop();
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
    }

    @Override
    public void seek(int percent) {
        mediaPlayer.seekTo((int)(mediaPlayer.getDuration() * ((float)percent/100)));
    }

    @Override
    public void resume() {
        mediaPlayer.start();
    }

    @Override
    public void next() {
    }

    @Override
    public void previous() {
    }

    @Override
    public boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }

    @Override
    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    @Override
    public int getSongDuration(){
        if(isPlaying()){
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    @Override
    public void destroy() {
        mediaPlayer.stop();
        mediaPlayer.release();
    }
}
