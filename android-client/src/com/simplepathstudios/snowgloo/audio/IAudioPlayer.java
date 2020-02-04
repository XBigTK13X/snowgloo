package com.simplepathstudios.snowgloo.audio;

import com.simplepathstudios.snowgloo.api.model.MusicFile;

interface IAudioPlayer {
    void play(MusicFile musicFile, int seekPosition);
    void stop();
    void pause();
    void seek(int percent);
    void destroy();
    void resume();
    boolean isPlaying();
    int getCurrentPosition();
    int getSongDuration();
}
