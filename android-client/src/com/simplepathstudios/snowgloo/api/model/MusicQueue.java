package com.simplepathstudios.snowgloo.api.model;

import com.simplepathstudios.snowgloo.Util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class MusicQueue {
    public static final String TAG = "MusicQueue";
    public static final MusicQueue EMPTY = new MusicQueue();

    private ArrayList<MusicFile> songs = new ArrayList<MusicFile>();
    private HashMap<MusicFile, Boolean> lookup = new HashMap<MusicFile, Boolean>();
    public Integer currentIndex = null;
    public UpdateReason updateReason = UpdateReason.SERVER_RELOAD;
    public PlayerState playerState;

    public boolean isReady(){
        return songs != null;
    }

    public int getSize(){
        if(songs == null){
            return 0;
        }
        return songs.size();
    }

    public boolean add(MusicFile song){
        return add(song, null);
    }

    public boolean add(MusicFile song, Integer position){
        if(lookup.containsKey(song)){
            return false;
        }
        lookup.put(song, true);
        if(position == null){
            songs.add(song);
        } else {
            songs.add(position, song);
        }

        return true;
    }

    public void remove(int songIndex){
        MusicFile song = songs.get(songIndex);
        lookup.remove(song.Id);
        songs.remove(songIndex);
    }

    public ArrayList<MusicFile> getAll(){
        return songs;
    }

    public MusicFile getSong(int index){
        return songs.get(index);
    }

    public MusicFile getCurrent(){
        if(songs == null || currentIndex == null || songs.size() == 0 || currentIndex == -1 || currentIndex > songs.size() - 1 || currentIndex < 0) {
            return MusicFile.EMPTY;
        }
        return songs.get(currentIndex);
    }

    public void shuffle(){
        Collections.shuffle(songs);
    }

    public enum PlayerState {
        PAUSED, PLAYING, IDLE
    }

    public enum UpdateReason{
        SHUFFLE, CLEAR, ITEM_ADDED, ITEM_MOVED, ITEM_REMOVED, SERVER_RELOAD, USER_CHANGED_CURRENT_INDEX, TRACK_CHANGED, SERVER_FIRST_LOAD, PLAYER_STATE_CHANGED, OUT_OF_TRACKS;
    }
}
