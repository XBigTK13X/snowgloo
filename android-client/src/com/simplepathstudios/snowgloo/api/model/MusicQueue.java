package com.simplepathstudios.snowgloo.api.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class MusicQueue {
    public static final MusicQueue EMPTY = new MusicQueue();

    public ArrayList<MusicFile> songs = new ArrayList<MusicFile>();
    public Integer currentIndex = null;
    public UpdateReason updateReason = UpdateReason.SERVER_RELOAD;
    public PlayerState playerState;

    public MusicFile getCurrent(){
        if(songs == null || currentIndex == null || songs.size() == 0 || currentIndex == -1 || currentIndex > songs.size() - 1 || currentIndex < 0) {
            return MusicFile.EMPTY;
        }
        return songs.get(currentIndex);
    }

    public enum PlayerState {
        PAUSED, PLAYING, IDLE
    }

    public enum UpdateReason{
        SHUFFLE, CLEAR, ITEM_ADDED, ITEM_MOVED, ITEM_REMOVED, SERVER_RELOAD, USER_CHANGED_CURRENT_INDEX, TRACK_CHANGED, SERVER_FIRST_LOAD, PLAYER_STATE_CHANGED;

        public static boolean shouldSeek(UpdateReason reason){
            switch(reason){
                case ITEM_MOVED:
                case ITEM_REMOVED:
                case ITEM_ADDED:
                case SERVER_FIRST_LOAD:
                    return true;
                default:
                    return false;
            }
        }

        public static boolean shouldPlay(UpdateReason reason){
            switch(reason){
                case ITEM_ADDED:
                case ITEM_MOVED:
                case ITEM_REMOVED:
                case USER_CHANGED_CURRENT_INDEX:
                case TRACK_CHANGED:
                case SHUFFLE:
                    return true;
                default:
                    return false;
            }
        }
    }
}
