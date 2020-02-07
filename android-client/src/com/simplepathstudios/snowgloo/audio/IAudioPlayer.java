package com.simplepathstudios.snowgloo.audio;

import com.simplepathstudios.snowgloo.api.model.MusicFile;

interface IAudioPlayer {
    void play(MusicFile musicFile, int seekPosition);
    void stop();
    void pause();
    void seek(int position);
    void resume(int position);
    int getCurrentPosition();
    int getSongDuration();
    void destroy();
    boolean isPlaying();
}
