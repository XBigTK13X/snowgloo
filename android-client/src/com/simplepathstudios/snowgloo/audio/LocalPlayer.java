package com.simplepathstudios.snowgloo.audio;

import android.media.MediaPlayer;
import android.util.Log;

import com.simplepathstudios.snowgloo.Util;
import com.simplepathstudios.snowgloo.api.model.MusicFile;

public class LocalPlayer implements IAudioPlayer {
    private static final String TAG = "LocalPlayer";
    private MediaPlayer media;
    private MusicFile currentSong;
    private int currentSeekPosition;
    private boolean mediaPrepared;

    public LocalPlayer(){
        try {
            media = new MediaPlayer();
            media.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Util.log(TAG,"an error occurred in media "+Util.messageNumberToText(Util.MessageKind.MediaPlayerError, what)+" " +Util.messageNumberToText(Util.MessageKind.MediaPlayerErrorExtra, extra));
                    // If an error occurs, returning true prevents a call to the onCompletionListener
                    return true;
                }
            });

            media.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    Util.log(TAG,"started playback from prepared listener for "+currentSong.Id);
                    mediaPrepared = true;
                    media.seekTo(currentSeekPosition);
                    media.start();
                }
            });
            media.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Util.log(TAG,"trying to play what comes after " + currentSong.Id);
                    AudioPlayer.getInstance().next();
                }
            });
        } catch (Exception e) {
            Log.e(TAG,"An error occurred with the local media player",e);
        }
    }

    @Override
    public boolean isPlaying(){
        if(media != null && mediaPrepared){
            return media.isPlaying();
        }
        return false;
    }

    @Override
    public void play(MusicFile musicFile, int seekPosition) {
        if(musicFile != null){
            try{
                currentSeekPosition = seekPosition;
                media.reset();
                media.setDataSource(musicFile.AudioUrl);
                media.prepareAsync();
                currentSong = musicFile;
            } catch(Exception e){
                Log.e(TAG, "An error occurred while playing",e);
            }
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
        if(media != null && media.isPlaying()){
            media.pause();
        }

    }

    @Override
    public void seek(int position) {
        if(media != null){
            media.seekTo(position);
        }
    }

    @Override
    public void resume(int position) {
       this.play(currentSong, position);
    }

    @Override
    public int getCurrentPosition() {
        try{
            if(media != null && mediaPrepared &&  media.isPlaying()){
                return media.getCurrentPosition();
            }
        } catch(Exception swallow){

        }
        return 0;
    }

    @Override
    public int getSongDuration(){
        if(media != null && mediaPrepared && media.isPlaying()){
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
