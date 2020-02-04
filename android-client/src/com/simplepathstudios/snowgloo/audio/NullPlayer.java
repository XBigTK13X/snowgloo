package com.simplepathstudios.snowgloo.audio;

import android.util.Log;

public class NullPlayer implements IAudioPlayer {
    private static final String TAG = "NullPlayer";
    @Override
    public void play(String url) {
        Log.d(TAG,"play "+url);
    }

    @Override
    public void stop() {
        Log.d(TAG,"stop");
    }

    @Override
    public void pause() {
        Log.d(TAG,"pause");
    }

    @Override
    public void seek(int position) {
        Log.d(TAG,"seek to "+position);
    }

    @Override
    public void destroy() {
        Log.d(TAG,"destroy");
    }

    @Override
    public void resume() {
        Log.d(TAG,"resume");
    }

    @Override
    public void next() {
        Log.d(TAG,"next");
    }

    @Override
    public void previous() {
        Log.d(TAG,"previous");
    }

    @Override
    public boolean isPlaying(){
        Log.d(TAG, "isPlaying");
        return false;
    }

    @Override
    public int getCurrentPosition() {
        Log.d(TAG, "getSongPosition");
        return 0;
    }
    @Override
    public int getSongDuration() {
        Log.d(TAG, "getSongPosition");
        return 0;
    }
}
