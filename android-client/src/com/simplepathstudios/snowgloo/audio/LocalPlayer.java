package com.simplepathstudios.snowgloo.audio;

import android.media.MediaPlayer;
import android.util.Log;

import com.simplepathstudios.snowgloo.api.model.MusicFile;

public class LocalPlayer implements IAudioPlayer {
    private static final String TAG = "LocalPlayer";
    private MediaPlayer media;
    private MusicFile currentSong;

    public LocalPlayer(){
        try {
            media = new MediaPlayer();
            media.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    // If an error occurs, returning true prevents a call to the onCompletionListener
                    return true;
                }
            });

            media.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    Log.d(TAG,"started playback from prepared listener for "+currentSong.Id);
                    media.start();
                }
            });
            media.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.d(TAG,"trying to play what comes after " + currentSong.Id);
                    AudioPlayer.getInstance().next();
                }
            });
        } catch (Exception e) {
            Log.e(TAG,"An error occurred with the local media player",e);
        }
    }

    @Override
    public void play(MusicFile musicFile, int seekPosition) {
        if(currentSong == null || !currentSong.Id.equals(musicFile.Id)){
            try{
                media.reset();
                media.setDataSource(musicFile.AudioUrl);
                media.prepareAsync();
                media.seekTo(seekPosition);
                currentSong = musicFile;
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
        if(media != null){
            media.stop();
        }

    }

    @Override
    public void pause() {
        if(media != null){
            media.pause();
        }

    }

    @Override
    public void seek(int position) {
        media.seekTo(position);
    }

    @Override
    public void resume(int position) {
        media.seekTo(position);
        media.start();
    }

    @Override
    public int getCurrentPosition() {
        try{
            if(media != null && media.isPlaying()){
                return media.getCurrentPosition();
            }
        } catch(Exception swallow){

        }
        return 0;
    }

    @Override
    public int getSongDuration(){
        if(media != null && media.isPlaying()){
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
