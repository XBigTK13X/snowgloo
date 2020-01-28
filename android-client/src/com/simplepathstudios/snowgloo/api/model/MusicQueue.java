package com.simplepathstudios.snowgloo.api.model;

import com.google.android.exoplayer2.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class MusicQueue {
    public static final MusicQueue EMPTY = new MusicQueue();

    public ArrayList<MusicFile> songs = new ArrayList<MusicFile>();
    public Integer currentIndex = null;
    public UpdateReason updateReason = UpdateReason.INITIALIZE;
    public Integer lastMoveFrom;
    public Integer lastMoveTo;

    public MusicFile getCurrent(){
        if(songs == null || currentIndex == null || songs.size() == 0 || currentIndex == -1 || currentIndex > songs.size() - 1) {
            return MusicFile.EMPTY;
        }
        return songs.get(currentIndex);
    }

    public enum UpdateReason{
        SHUFFLE, CLEAR, ITEM_ADDED, ITEM_MOVED, ITEM_REMOVED, CURRENT_INDEX_CHANGED, INITIALIZE;
    }
}
