package com.simplepathstudios.snowgloo.audio;

import com.simplepathstudios.snowgloo.SnowglooSettings;
import com.simplepathstudios.snowgloo.Util;
import com.simplepathstudios.snowgloo.api.model.MusicFile;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;
import com.simplepathstudios.snowgloo.api.model.PlaylistList;
import com.simplepathstudios.snowgloo.viewmodel.ObservableMusicQueue;

public class AudioPlayer {
    public enum PlaybackMode {
        LOCAL,
        REMOTE
    }
    private static final String TAG = "AudioPlayer";
    private static AudioPlayer __instance;
    public static AudioPlayer getInstance(){
        if(__instance == null || __instance.isDestroyed()){
            if(__instance != null){
                try{
                    __instance.destroy();
                    __instance = null;
                } catch(Exception swallow){}
            }
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
        this.localPlayer.setVolume(SnowglooSettings.InternalMediaVolume);
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

    public boolean isPlaying(){
        try{
            return currentPlayer.isPlaying();
        } catch(Exception swallow){

        }
        return false;
    }

    public boolean isCasting(){
        return remotePlayer.isCasting();
    }

    public void setPlaybackMode(PlaybackMode mode){
        Util.log(TAG, "Playback mode changed to "+mode);
        Integer seekPosition = currentPlayer.getCurrentPosition();
        if(mode == PlaybackMode.LOCAL){
            try {
                Util.log(TAG, "Try to pause the remote player");
                remotePlayer.pause();
            } catch(Exception swallow){

            }
            currentPlayer = localPlayer;
        }
        else if(mode == PlaybackMode.REMOTE) {
            if(currentPlayer == localPlayer){
                try{
                    Util.log(TAG, "Try to pause the local player");
                    try{
                        localPlayer.pause();
                    } catch(Exception swallow){

                    }
                }
                catch(Exception e){
                    Util.error(TAG, e);
                }
                remotePlayer.readMediaSession();
            }
            currentPlayer = remotePlayer;
        }
        if(playerState == MusicQueue.PlayerState.PLAYING && seekPosition != null){
            try{
                Util.log(TAG, "Attempting to resume playback after swapping mode");
                currentPlayer.play(currentSong, seekPosition);
            } catch(Exception e){
                Util.error(TAG, e);
            }
        }
    }

    public void setPlayerState(MusicQueue.PlayerState playerState){
        this.playerState = playerState;
        observableMusicQueue.setPlayerState(playerState);
    }

    public boolean play(){
        return this.play(false);
    }

    public boolean play(boolean startOver){
        if(localPlayer != null && remotePlayer != null){
            if(localPlayer != null && localPlayer.isPlaying() && remotePlayer != null && remotePlayer.isPlaying()){
                setPlaybackMode(PlaybackMode.REMOTE);
            }
        }
        try{
            isSeeking = false;
            MusicFile currentQueueSong = observableMusicQueue.getQueue().getCurrent();
            if(currentQueueSong != null && currentQueueSong.Id != null){
                if(startOver || (currentSong == null || currentSong.Id == null || !currentQueueSong.Id.equals(currentSong.Id)) || lastPosition == null){
                    Util.log(TAG, "This seems like a new song, play from the beginning "+currentQueueSong.Id);
                    // ExoPlayer causes a double skip when a user hits Next, this avoid that behavior
                    currentSong = currentQueueSong;
                    lastPosition = null;
                    lastDuration = null;
                    currentPlayer.play(currentSong, 0);
                }   else if(currentQueueSong.Id != null){
                    Util.log(TAG, "This song was playing before, attempt to resume "+currentQueueSong.Id);
                    currentPlayer.resume(lastPosition);
                }
                setPlayerState(MusicQueue.PlayerState.PLAYING);
            }
            return true;
        } catch(Exception e){
            Util.error(TAG, e);
            return false;
        }
    }

    public boolean pause(){
        try {
            Util.log(TAG, "Pausing audio and tracking the duration");
            isSeeking = false;
            lastDuration = this.getSongDuration();
            lastPosition = currentPlayer.getCurrentPosition();
            currentPlayer.pause();
            setPlayerState(MusicQueue.PlayerState.PAUSED);
            return true;
        } catch(Exception e){
            Util.error(TAG, e);
            return false;
        }
    }

    public void stop(){
        try{
            Util.log(TAG, "Stopping audio by pausing the media handler");
            isSeeking = false;
            currentPlayer.pause();
            setPlayerState(MusicQueue.PlayerState.IDLE);
        } catch(Exception e){
            Util.error(TAG, e);
        }
    }

    public Integer getSongPosition(){
        if(isSeeking){
            return lastPosition;
        }
        Integer position = null;
        try {
            position = currentPlayer.getCurrentPosition();
        } catch(Exception e){
            Util.error(TAG, e);
        }
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
        Integer duration = null;
        try {
            duration = currentPlayer.getSongDuration();
        } catch(Exception e){
            Util.error(TAG, e);
        }
        if(duration == null){
            return lastDuration;
        } else {
            lastDuration = duration;
        }
        return duration;
    }

    public void seekTo(int position){
        try{
            isSeeking = true;
            Util.log(TAG, "Updating last seek position to " + position);
            lastPosition = position;
            if(playerState == MusicQueue.PlayerState.PLAYING){
                Util.log(TAG, "Since music is playing, apply the seek right now to "+position);
                currentPlayer.seek(position);
                isSeeking = false;
            }
        } catch(Exception e){
            Util.error(TAG, e);
        }
    }

    public void next(){
        try {
            Util.log(TAG, "Maybe going to the next track");
            if(observableMusicQueue.nextIndex()){
                Util.log(TAG, "A new index was found, playing the next track");
                this.play(true);
            } else {
                this.stop();
            }
        } catch(Exception e){
             Util.error(TAG, e);
        }
    }

    public void previous(){
        try {
            Util.log(TAG, "Maybe going to the previous track");
            if(observableMusicQueue.previousIndex()){
                Util.log(TAG, "A new index was found, playing the previous track");
                this.play(true);
            } else {
                this.stop();
            }
        } catch(Exception e){
            Util.error(TAG, e);
        }
    }

    public void setVolume(double volume){
        try {
            currentPlayer.setVolume(volume);
        }
        catch(Exception e){
            Util.error(TAG, e);
        }
    }

    public void destroy(){
        Util.log(TAG, "Destroying the audio players");
        localPlayer.destroy();
        localPlayer = null;
        remotePlayer.destroy();
        remotePlayer = null;
    }

    public boolean isDestroyed(){
        return localPlayer == null && remotePlayer == null;
    }
}