package com.simplepathstudios.snowgloo.audio;

import android.media.MediaPlayer;
import android.util.Log;

import com.simplepathstudios.snowgloo.api.model.MusicFile;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;

public class LocalPlayer implements IAudioPlayer {
    private static final String TAG = "LocalPlayer";
    private MediaPlayer media;
    private String currentSongId;

    public LocalPlayer(){
        try {
            media = new MediaPlayer();
            media.setOnErrorListener(new MediaPlayer.OnErrorListener() {

                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    if (extra == MediaPlayer.MEDIA_ERROR_SERVER_DIED
                            || extra == MediaPlayer.MEDIA_ERROR_MALFORMED) {
                        Log.d(TAG,"erroronplaying");
                    } else if (extra == MediaPlayer.MEDIA_ERROR_IO) {
                        Log.d(TAG,"erroronplaying");
                        return false;
                    }
                    else {
                        Log.d(TAG, "onError playing");
                    }
                    return false;
                }
            });
            media.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {

                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    Log.d(TAG,"onBufferingUpdate" + percent);

                }
            });

            media.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                public void onPrepared(MediaPlayer mp) {
                    media.start();
                    Log.d(TAG,"playing");
                }
            });
            media.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.d(TAG,"completed");

                }
            });
            media.setOnInfoListener(new MediaPlayer.OnInfoListener() {
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
    public void play(MusicFile musicFile, int seekPosition) {
        if(currentSongId == null || !currentSongId.equals(musicFile.Id)){
            try{
                media.reset();
                media.setDataSource(musicFile.AudioUrl);
                media.prepareAsync();
                media.seekTo(seekPosition);
                currentSongId = musicFile.Id;
            } catch(Exception e){
                Log.e(TAG, "An error occurred while playing",e);
            }
        }
        else {
            media.seekTo(seekPosition);
            media.start();
        }
    }

    @Override
    public void stop() {
        media.stop();
    }

    @Override
    public void pause() {
        media.pause();
    }

    @Override
    public void seek(int percent) {
        media.seekTo((int)(getSongDuration() * ((float)percent/100)));
    }

    @Override
    public void resume() {
        media.start();
    }

    @Override
    public boolean isPlaying(){
        return media.isPlaying();
    }

    @Override
    public int getCurrentPosition() {
        return media.getCurrentPosition();
    }

    @Override
    public int getSongDuration(){
        if(isPlaying()){
            return media.getDuration();
        }
        return 0;
    }

    @Override
    public void destroy() {
        media.stop();
        media.release();
    }
}
