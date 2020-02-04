package com.simplepathstudios.snowgloo.audio;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.simplepathstudios.snowgloo.MainActivity;
import com.simplepathstudios.snowgloo.api.model.MusicFile;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;
import com.simplepathstudios.snowgloo.viewmodel.MusicQueueViewModel;

public class AudioPlayer {
    private static AudioPlayer __instance;
    public static AudioPlayer getInstance(){
        if(__instance == null){
            __instance = new AudioPlayer();
        }
        return __instance;
    }

    IAudioPlayer currentPlayer;
    LocalPlayer localPlayer;
    IAudioPlayer remotePlayer;
    MusicQueueViewModel viewModel;
    MusicQueue queue;
    int lastIndex;

    private AudioPlayer() {
        this.localPlayer = new LocalPlayer();
        this.currentPlayer = this.localPlayer;
        this.remotePlayer = new NullPlayer();
        this.viewModel = new ViewModelProvider(MainActivity.getInstance()).get(MusicQueueViewModel.class);
        this.viewModel.Data.observe(MainActivity.getInstance(), new Observer<MusicQueue>() {
            @Override
            public void onChanged(MusicQueue musicQueue) {
                if(musicQueue != null && musicQueue.currentIndex != null &&  musicQueue.currentIndex  != lastIndex){
                    viewModel.setPlaying(true);
                    currentPlayer.play(musicQueue.getCurrent().AudioUrl);
                    lastIndex = musicQueue.currentIndex;
                }
                queue = musicQueue;
            }
        });
    }

    public void play(){
        MusicFile song = queue.getCurrent();
        viewModel.setPlaying(true);
        this.currentPlayer.play(song.AudioUrl);
    }

    public int getSongPosition(){
        return this.currentPlayer.getCurrentPosition();
    }

    public int getSongDuration(){
        return this.currentPlayer.getSongDuration();
    }

    public void seekTo(int position){
        this.currentPlayer.seek(position);
    }

    public void pause(){
        this.currentPlayer.pause();
        viewModel.setPlaying(false);
    }

    public void resume(){
        viewModel.setPlaying(true);
        this.currentPlayer.resume();
    }

    public void next(){
        viewModel.nextIndex();
    }

    public void previous(){
        viewModel.previousIndex();
    }

    public void destroy(){
        localPlayer.destroy();
        remotePlayer.destroy();
    }
}
