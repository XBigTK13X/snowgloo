package com.simplepathstudios.snowgloo.audio;

interface IAudioPlayer {
    void play(String url);
    void stop();
    void pause();
    void seek(int position);
    void destroy();
    void resume();
    void next();
    void previous();
    boolean isPlaying();
    int getCurrentPosition();
    int getSongDuration();
}
