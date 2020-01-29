package com.simplepathstudios.snowgloo.api.model;

import java.util.ArrayList;

public class MusicQueue {
    public static final MusicQueue EMPTY = new MusicQueue();

    public ArrayList<MusicFile> songs = new ArrayList<MusicFile>();
    public Integer currentIndex = null;
    public UpdateReason updateReason = UpdateReason.SERVER_RELOAD;

    public MusicFile getCurrent(){
        if(songs == null || currentIndex == null || songs.size() == 0 || currentIndex == -1 || currentIndex > songs.size() - 1) {
            return MusicFile.EMPTY;
        }
        return songs.get(currentIndex);
    }

    public enum UpdateReason{
        SHUFFLE, CLEAR, ITEM_ADDED, ITEM_MOVED, ITEM_REMOVED, CURRENT_INDEX_CHANGED, SERVER_RELOAD;
    }
}
