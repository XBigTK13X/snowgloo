package com.simplepathstudios.snowgloo.audio;

import androidx.lifecycle.ViewModelProvider;

import com.simplepathstudios.snowgloo.MainActivity;
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
    int lastIndex;
    int seekPosition;

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

    public void handleUpdate(MusicQueue musicQueue){
        if(musicQueue != null && musicQueue.currentIndex != null){
            Log.d(TAG, "Music queue changed. currentIndex is " + musicQueue.currentIndex + " with "+musicQueue.songs.size()+ " songs playing on "+(currentPlayer == remotePlayer?"Chromecast":"Local Device"));
            MusicFile musicFile = musicQueue.getCurrent();
            if(musicFile != null && musicQueue.updateReason == MusicQueue.UpdateReason.USER_CHANGED_CURRENT_INDEX){
                this.play();
            }
        }
        queue = musicQueue;
    }

    public void play(){
        MusicFile song = queue.getCurrent();
        observableMusicQueue.setPlaying(true);
        seekPosition = 0;
        currentPlayer.play(song, seekPosition);
    }

    public void pause(){
        observableMusicQueue.setPlaying(false);
        currentPlayer.pause();
    }

    public int getSongPosition(){
        return currentPlayer.getCurrentPosition();
    }

    public int getSongDuration(){
        return currentPlayer.getSongDuration();
    }

    public void seekTo(int position){
        currentPlayer.seek(position);
    }

    public void resume(){
        observableMusicQueue.setPlaying(true);
        currentPlayer.resume();
    }

    public void next(){
        observableMusicQueue.nextIndex();
        this.play();
    }

    public void previous(){
        observableMusicQueue.previousIndex();
        this.play();
    }

    public void setPlaybackMode(PlaybackMode mode){
        if((mode == PlaybackMode.LOCAL && currentPlayer == localPlayer) ||(mode == PlaybackMode.REMOTE && currentPlayer == remotePlayer)){
            return;
        }
        seekPosition = currentPlayer.getCurrentPosition();
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
        currentPlayer.play(queue.getCurrent(), seekPosition);
    }

    public boolean isPlaying(){
        return currentPlayer.isPlaying();
    }

    public void destroy(){
        localPlayer.destroy();
        remotePlayer.destroy();
    }
}
