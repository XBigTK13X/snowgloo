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
    MusicQueue queue;
    MusicFile currentSong;
    int pausedDuration = 0;
    int lastSeekPercent;

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
        if(queue.playerState == MusicQueue.PlayerState.PLAYING){
            Util.log(TAG, "Attempting to resume playback after swapping mode");
            currentPlayer.play(queue.getCurrent(), seekPosition);
        }
    }

    public void handleUpdate(MusicQueue musicQueue){
        queue = musicQueue;
        if(queue != null && queue.currentIndex != null){
            Util.log(TAG, "Music queue changed because " + queue.updateReason + ". currentIndex is " + queue.currentIndex + " with "+queue.songs.size()+ " songs playing on "+(currentPlayer == remotePlayer?"Chromecast":"Local Device"));
            MusicFile musicFile = queue.getCurrent();
            if(musicFile != null && (queue.updateReason == MusicQueue.UpdateReason.USER_CHANGED_CURRENT_INDEX || queue.updateReason == MusicQueue.UpdateReason.SHUFFLE)){
                Util.log(TAG, "Decided to call play from observer for reason "+queue.updateReason);
                this.play();
            }
        }
        if(queue.updateReason == MusicQueue.UpdateReason.CLEAR || queue.updateReason == MusicQueue.UpdateReason.OUT_OF_TRACKS){
            Util.log(TAG, "Deciding to stop because of reason " +queue.updateReason);
            this.stop();
        }
    }

    public void play(){
        MusicFile song = queue.getCurrent();
        if(song == null || currentSong == null || song.Id == null || !song.Id.equals(currentSong.Id)){
            Util.log(TAG, "This seems like a new song, play from the beginning");
            currentSong = song;
            currentPlayer.play(song, 0);
            observableMusicQueue.setPlayerState(MusicQueue.PlayerState.PLAYING);
        }
        else {
            Util.log(TAG, "This song was playing before, attempt to resume");
            int position = (int)(pausedDuration * ((float)lastSeekPercent/100));
            currentPlayer.resume(position);
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
        return currentPlayer.getCurrentPosition();
    }

    public int getSongDuration(){
        return currentPlayer.getSongDuration();
    }

    public void seekTo(int percent){
        Util.log(TAG, "Updating last seek percent to "+percent);
        lastSeekPercent = percent;
        if(queue.playerState == MusicQueue.PlayerState.PLAYING){
            Util.log(TAG, "Since music is playing, apply the seek now");
            int position = (int)(getSongDuration() * ((float)percent/100));
            currentPlayer.seek(position);
        }
    }

    public void next(){
        Util.log(TAG, "Maybe going to the next track");
        if(observableMusicQueue.nextIndex()){
            Util.log(TAG, "A new index was found, playing the next track");
            this.play();
        }
    }

    public void previous(){
        Util.log(TAG, "Maybe going to the previous track");
        if(observableMusicQueue.previousIndex()){
            Util.log(TAG, "A new index was found, playing the previous track");
            this.play();
        }

    }

    public void destroy(){
        Util.log(TAG, "Destroying the audio player");
        localPlayer.destroy();
        remotePlayer.destroy();
    }
}
