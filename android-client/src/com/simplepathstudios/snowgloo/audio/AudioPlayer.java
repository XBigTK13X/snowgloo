package com.simplepathstudios.snowgloo.audio;

import com.simplepathstudios.snowgloo.Util;
import com.simplepathstudios.snowgloo.api.model.MusicFile;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;
import com.simplepathstudios.snowgloo.viewmodel.ObservableMusicQueue;

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
    int pausedDuration = 0;
    int lastSeekPosition = 0;
    int lastDuration;
    int lastPosition;
    MusicQueue.PlayerState playerState;
    MusicFile currentSong;

    private AudioPlayer() {
        this.localPlayer = new LocalPlayer();
        this.remotePlayer = new CastPlayer();
        if(this.remotePlayer.isCasting()){
            Util.log(TAG, "Launching with the cast player");
            this.currentPlayer = this.remotePlayer;
        }
        else {
            Util.log(TAG, "Launching with the local player");
            this.currentPlayer = this.localPlayer;
        }
        this.observableMusicQueue = ObservableMusicQueue.getInstance();
    }

    public void setPlaybackMode(PlaybackMode mode){
        if((mode == PlaybackMode.LOCAL && currentPlayer == localPlayer) ||(mode == PlaybackMode.REMOTE && currentPlayer == remotePlayer)){
            return;
        }
        Util.log(TAG, "Playback mode changed to "+mode);
        int seekPosition = currentPlayer.getCurrentPosition();
        if(mode == PlaybackMode.LOCAL){
            if(currentPlayer == remotePlayer){
                Util.log(TAG, "Playback in progress on remote, pausing");
                remotePlayer.pause();
            }
            currentPlayer = localPlayer;
        }
        else if(mode == PlaybackMode.REMOTE) {
            if(currentPlayer == localPlayer){
                Util.log(TAG, "Playback in progress on local, pausing");
                localPlayer.pause();
            }
            currentPlayer = remotePlayer;
        }
        if(playerState == MusicQueue.PlayerState.PLAYING){
            Util.log(TAG, "Attempting to resume playback after swapping mode");
            currentPlayer.play(currentSong, seekPosition);
        }
    }

    public void play(){
        MusicFile currentQueueSong = observableMusicQueue.getQueue().getCurrent();
        if((currentQueueSong != null && currentSong == null) || (!currentQueueSong.Id.equals(currentSong.Id))){
            Util.log(TAG, "This seems like a new song, play from the beginning "+currentQueueSong.Id);
            currentSong = currentQueueSong;
            lastPosition = 0;
            lastDuration = 0;
            currentPlayer.play(currentSong, 0);
            observableMusicQueue.setPlayerState(MusicQueue.PlayerState.PLAYING);
        }
        else {
            Util.log(TAG, "This song was playing before, attempt to resume "+currentQueueSong.Id);
            currentPlayer.resume(lastSeekPosition);
            observableMusicQueue.setPlayerState(MusicQueue.PlayerState.PLAYING);
        }

    }

    public void pause(){
        Util.log(TAG, "Pausing audio and tracking the duration");
        pausedDuration = this.getSongDuration();
        currentPlayer.pause();
        observableMusicQueue.setPlayerState(MusicQueue.PlayerState.PAUSED);
    }

    public void stop(){
        Util.log(TAG, "Stopping audio by pausing the media handler");
        currentPlayer.pause();
        observableMusicQueue.setPlayerState(MusicQueue.PlayerState.IDLE);
    }

    public int getSongPosition(){
        int position = currentPlayer.getCurrentPosition();
        if(position == 0){
            return lastPosition;
        } else {
            lastPosition = position;
        }
        return currentPlayer.getCurrentPosition();
    }

    public int getSongDuration(){
        int duration = currentPlayer.getSongDuration();
        if(duration == 0){
            return lastDuration;
        } else {
            lastDuration = duration;
        }
        return duration;
    }

    public void seekTo(int position){
        Util.log(TAG, "Updating last seek position to " + position);
        lastSeekPosition = position;
        if(playerState == MusicQueue.PlayerState.PLAYING){
            Util.log(TAG, "Since music is playing, apply the seek now");
            currentPlayer.seek(position);
        }
    }

    public void next(){
        Util.log(TAG, "Maybe going to the next track");
        if(observableMusicQueue.nextIndex()){
            Util.log(TAG, "A new index was found, playing the next track");
            this.play();
        } else {
            this.stop();
        }
    }

    public void previous(){
        Util.log(TAG, "Maybe going to the previous track");
        if(observableMusicQueue.previousIndex()){
            Util.log(TAG, "A new index was found, playing the previous track");
            this.play();
        } else {
            this.stop();
        }
    }

    public void destroy(){
        Util.log(TAG, "Destroying the audio player");
        localPlayer.destroy();
        remotePlayer.destroy();
    }
}
