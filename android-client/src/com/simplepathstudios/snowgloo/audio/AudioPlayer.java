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
    Integer lastDuration;
    Integer lastPosition;
    MusicQueue.PlayerState playerState;
    MusicFile currentSong;
    boolean isSeeking = false;

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
        Integer seekPosition = currentPlayer.getCurrentPosition();
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
        if(playerState == MusicQueue.PlayerState.PLAYING && seekPosition != null){
            Util.log(TAG, "Attempting to resume playback after swapping mode");
            currentPlayer.play(currentSong, seekPosition);
        }
    }

    private void setPlayerState(MusicQueue.PlayerState playerState){
        this.playerState = playerState;
        observableMusicQueue.setPlayerState(playerState);
    }

    public void play(){
        isSeeking = false;
        MusicFile currentQueueSong = observableMusicQueue.getQueue().getCurrent();
        if((currentQueueSong != null && currentSong == null) || (!currentQueueSong.Id.equals(currentSong.Id))){
            Util.log(TAG, "This seems like a new song, play from the beginning "+currentQueueSong.Id);
            currentSong = currentQueueSong;
            lastPosition = null;
            lastDuration = null;
            currentPlayer.play(currentSong, 0);
            setPlayerState(MusicQueue.PlayerState.PLAYING);
        }
        else if(currentQueueSong.Id != null){
            Util.log(TAG, "This song was playing before, attempt to resume "+currentQueueSong.Id);
            currentPlayer.resume(lastPosition);
            setPlayerState(MusicQueue.PlayerState.PLAYING);
        }

    }

    public void pause(){
        Util.log(TAG, "Pausing audio and tracking the duration");
        isSeeking = false;
        lastDuration = this.getSongDuration();
        lastPosition = currentPlayer.getCurrentPosition();
        currentPlayer.pause();
        setPlayerState(MusicQueue.PlayerState.PAUSED);
    }

    public void stop(){
        Util.log(TAG, "Stopping audio by pausing the media handler");
        isSeeking = false;
        currentPlayer.pause();
        setPlayerState(MusicQueue.PlayerState.IDLE);
    }

    public Integer getSongPosition(){
        if(isSeeking){
            return lastPosition;
        }
        Integer position = currentPlayer.getCurrentPosition();
        if(position == null){
            return lastPosition;
        } else {
            lastPosition = position;
        }
        return position;
    }

    public Integer getSongDuration(){
        if(isSeeking){
            return lastPosition;
        }
        Integer duration = currentPlayer.getSongDuration();
        if(duration == null){
            return lastDuration;
        } else {
            lastDuration = duration;
        }
        return duration;
    }

    public void seekTo(int position){
        isSeeking = true;
        Util.log(TAG, "Updating last seek position to " + position);
        lastPosition = position;
        if(playerState == MusicQueue.PlayerState.PLAYING){
            Util.log(TAG, "Since music is playing, apply the seek right now to "+position);
            currentPlayer.seek(position);
            isSeeking = false;
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
