package com.simplepathstudios.snowgloo.audio;

import com.simplepathstudios.snowgloo.api.model.MusicFile;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;
import com.simplepathstudios.snowgloo.viewmodel.ObservableMusicQueue;

import android.util.Log;

public class AudioPlayer {
    public enum PlaybackMode {
        LOCAL,
        REMOTE
    }
    private static final String TAG = "AudioPlayer";
    private static AudioPlayer __instance;
    public static AudioPlayer getInstance(){
        if(__instance == null){
            __instance = new AudioPlayer();
        }
        return __instance;
    }

    IAudioPlayer currentPlayer;
    LocalPlayer localPlayer;
    CastPlayer remotePlayer;
    ObservableMusicQueue observableMusicQueue;
    MusicQueue queue;
    MusicFile currentSong;
    int pausedDuration = 0;
    int lastSeekPercent;

    private AudioPlayer() {
        this.localPlayer = new LocalPlayer();
        this.remotePlayer = new CastPlayer();
        if(this.remotePlayer.isCasting()){
            this.currentPlayer = this.remotePlayer;
        }
        else {
            this.currentPlayer = this.localPlayer;
        }
        this.observableMusicQueue = ObservableMusicQueue.getInstance();
    }

    public void setPlaybackMode(PlaybackMode mode){
        if((mode == PlaybackMode.LOCAL && currentPlayer == localPlayer) ||(mode == PlaybackMode.REMOTE && currentPlayer == remotePlayer)){
            return;
        }
        int seekPosition = currentPlayer.getCurrentPosition();
        if(mode == PlaybackMode.LOCAL){
            if(currentPlayer == remotePlayer){
                remotePlayer.pause();
            }
            currentPlayer = localPlayer;
        }
        else if(mode == PlaybackMode.REMOTE) {
            if(currentPlayer == localPlayer){
                localPlayer.pause();
            }
            currentPlayer = remotePlayer;
        }
        if(queue.playerState == MusicQueue.PlayerState.PLAYING){
            currentPlayer.play(queue.getCurrent(), seekPosition);
        }
    }

    public void handleUpdate(MusicQueue musicQueue){
        queue = musicQueue;
        if(queue != null && queue.currentIndex != null){
            Log.d(TAG, "Music queue changed. currentIndex is " + queue.currentIndex + " with "+queue.songs.size()+ " songs playing on "+(currentPlayer == remotePlayer?"Chromecast":"Local Device"));
            MusicFile musicFile = queue.getCurrent();
            if(musicFile != null && (queue.updateReason == MusicQueue.UpdateReason.USER_CHANGED_CURRENT_INDEX || queue.updateReason == MusicQueue.UpdateReason.SHUFFLE)){
                this.play();
            }
        }
        if(queue.updateReason == MusicQueue.UpdateReason.CLEAR || queue.updateReason == MusicQueue.UpdateReason.OUT_OF_TRACKS){
            this.stop();
        }
    }

    public void play(){
        MusicFile song = queue.getCurrent();
        if(song == null || currentSong == null || song.Id == null || !song.Id.equals(currentSong.Id)){
            currentSong = song;
            currentPlayer.play(song, 0);
            observableMusicQueue.setPlayerState(MusicQueue.PlayerState.PLAYING);
        }
        else {
            int position = (int)(pausedDuration * ((float)lastSeekPercent/100));
            currentPlayer.resume(position);
            observableMusicQueue.setPlayerState(MusicQueue.PlayerState.PLAYING);
        }

    }

    public void pause(){
        pausedDuration = this.getSongDuration();
        currentPlayer.pause();
        observableMusicQueue.setPlayerState(MusicQueue.PlayerState.PAUSED);
    }

    public void stop(){
        currentPlayer.pause();
        observableMusicQueue.setPlayerState(MusicQueue.PlayerState.IDLE);
    }
    public int getSongPosition(){
        return currentPlayer.getCurrentPosition();
    }

    public int getSongDuration(){
        return currentPlayer.getSongDuration();
    }

    public void seekTo(int percent){
        lastSeekPercent = percent;
        if(queue.playerState == MusicQueue.PlayerState.PLAYING){
            int position = (int)(getSongDuration() * ((float)percent/100));
            currentPlayer.seek(position);
        }
    }

    public void next(){
        if(observableMusicQueue.nextIndex()){
            this.play();
        }
    }

    public void previous(){
        if(observableMusicQueue.previousIndex()){
            this.play();
        }

    }

    public void destroy(){
        localPlayer.destroy();
        remotePlayer.destroy();
    }
}
