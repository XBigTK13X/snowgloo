package com.simplepathstudios.snowgloo.api.model;

import java.util.ArrayList;
import java.util.List;

public class MusicQueue {
    public static final MusicQueue EMPTY = new MusicQueue();

    public ArrayList<MusicFile> songs = new ArrayList<MusicFile>();
    public Integer currentIndex = -1;

    public MusicFile getCurrent(){
        if(songs == null || currentIndex == null || songs.size() == 0 || currentIndex == -1) {
            return MusicFile.EMPTY;
        }
        return songs.get(currentIndex);
    }
}
