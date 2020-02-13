package com.simplepathstudios.snowgloo.viewmodel;

import android.util.Log;

import androidx.lifecycle.Observer;

import com.simplepathstudios.snowgloo.LoadingIndicator;
import com.simplepathstudios.snowgloo.Util;
import com.simplepathstudios.snowgloo.api.ApiClient;
import com.simplepathstudios.snowgloo.api.model.MusicFile;
import com.simplepathstudios.snowgloo.api.model.MusicPlaylist;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;
import com.simplepathstudios.snowgloo.api.model.MusicQueuePayload;

import java.util.ArrayList;
import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.simplepathstudios.snowgloo.api.model.MusicQueue.UpdateReason.PLAYER_STATE_CHANGED;

public class ObservableMusicQueue {
    private static ObservableMusicQueue __instance;
    public static ObservableMusicQueue getInstance(){
        if(__instance == null){
            __instance = new ObservableMusicQueue();
        }
        return __instance;
    }

    private MusicQueue queue;
    private boolean firstLoad;
    private ArrayList<Observer<MusicQueue>> observers;

    public ObservableMusicQueue(){
        observers = new ArrayList<>();
        queue = MusicQueue.EMPTY;
        this.firstLoad = true;
    }

    public void observe(Observer<MusicQueue> observer){
        observers.add(observer);
        observer.onChanged(queue);
    }

    public void load(){
        LoadingIndicator.setLoading(true);
        ApiClient.getInstance().getQueue().enqueue(new Callback< MusicQueue >(){
            @Override
            public void onResponse(Call<MusicQueue> call, Response<MusicQueue> response) {
                LoadingIndicator.setLoading(false);
                queue = response.body();
                if(firstLoad){
                    queue.updateReason = MusicQueue.UpdateReason.SERVER_FIRST_LOAD;
                    queue.playerState = MusicQueue.PlayerState.IDLE;
                } else {
                    queue.updateReason = MusicQueue.UpdateReason.SERVER_RELOAD;
                }

                firstLoad = false;
                notifyObservers(false);
            }

            @Override
            public void onFailure(Call<MusicQueue> call, Throwable t) {
                LoadingIndicator.setLoading(false);
            }
        });
    }

    private void save(){
        ApiClient.getInstance().setQueue(queue).enqueue(new Callback< MusicQueuePayload >(){
            @Override
            public void onResponse(Call<MusicQueuePayload> call, Response<MusicQueuePayload> response) {
                LoadingIndicator.setLoading(false);
            }

            @Override
            public void onFailure(Call<MusicQueuePayload> call, Throwable t) {
                LoadingIndicator.setLoading(false);
            }
        });
    }

    public void clear(){
        LoadingIndicator.setLoading(true);
        ApiClient.getInstance().clearQueue().enqueue(new Callback<MusicQueue>(){
            @Override
            public void onResponse(Call<MusicQueue> call, Response<MusicQueue> response) {
                queue = response.body();
                queue.updateReason = MusicQueue.UpdateReason.CLEAR;
                queue.playerState = MusicQueue.PlayerState.IDLE;
                notifyObservers();
                LoadingIndicator.setLoading(false);
            }

            @Override
            public void onFailure(Call<MusicQueue> call, Throwable t) {
                LoadingIndicator.setLoading(false);
                Log.e("ObservableMusicQueue.clear","Failed",t);
            }
        });
    }

    public boolean previousIndex(){
        boolean result = true;
        if(queue.currentIndex != null){
            queue.currentIndex -= 1;
            queue.updateReason = MusicQueue.UpdateReason.TRACK_CHANGED;
            if(queue.currentIndex < 0){
                queue.currentIndex = null;
                queue.updateReason = MusicQueue.UpdateReason.OUT_OF_TRACKS;
                result = false;
            }
        } else {
            result = false;
        }
        notifyObservers();
        return result;
    }

    public boolean nextIndex(){
        boolean result = true;
        if(queue.songs != null && queue.currentIndex != null){
            queue.currentIndex += 1;
            queue.updateReason = MusicQueue.UpdateReason.TRACK_CHANGED;
            if(queue.currentIndex > queue.songs.size()-1){
                queue.currentIndex = null;
                queue.updateReason = MusicQueue.UpdateReason.OUT_OF_TRACKS;
                result = false;
            }
        }
        if(queue.currentIndex == null){
            result = false;
        }
        notifyObservers();
        return result;
    }

    public void setCurrentIndex(Integer currentIndex){
        if(queue.currentIndex != null && queue.currentIndex == currentIndex){
            return;
        }
        queue.currentIndex = currentIndex;
        queue.updateReason = MusicQueue.UpdateReason.USER_CHANGED_CURRENT_INDEX;
        notifyObservers();
    }

    public void removeItem(int position){
        queue.songs.remove(position);
        if(queue.currentIndex != null){
            if(position < queue.currentIndex){
                queue.currentIndex--;
            }else {
                if(position == queue.currentIndex){
                    queue.currentIndex = null;
                }
            }
        }
        queue.updateReason = MusicQueue.UpdateReason.ITEM_REMOVED;
        notifyObservers();
    }

    public void moveItem(MusicFile item, int fromPosition, int toPosition) {
        if(fromPosition != toPosition){
            queue.songs.remove(fromPosition);
            queue.songs.add(toPosition,item);
            if(queue.currentIndex != null){
                if(queue.currentIndex == fromPosition){
                    queue.currentIndex = toPosition;
                } else{
                    if(queue.currentIndex >= fromPosition && queue.currentIndex <= toPosition){
                        queue.currentIndex--;
                    }
                    if(queue.currentIndex <= fromPosition && queue.currentIndex >= toPosition){
                        queue.currentIndex++;
                    }
                }
            }
            queue.updateReason = MusicQueue.UpdateReason.ITEM_MOVED;
            notifyObservers();
        }
    }

    public void addItems(ArrayList<MusicFile> items){
        if(items == null){
            return ;
        }
        int foundCount = 0;
        for(MusicFile item : items){
            boolean found = false;
            for(MusicFile song : queue.songs){
                if(song.Id.equalsIgnoreCase(item.Id)) {
                    found = true;
                    foundCount++;
                    break;
                }
            }
            if (!found) {
                queue.songs.add(item);
            }
        }

        queue.updateReason = MusicQueue.UpdateReason.ITEM_ADDED;
        queue.currentIndex = queue.currentIndex == null ? queue.songs.size() - items.size():queue.currentIndex;
        notifyObservers();
        if(foundCount == 0){
            Util.toast("All songs added to queue.");
        }
        else if(foundCount == items.size()) {
            Util.toast("No songs added, they are already queued up.");
        }
        else {
            Util.toast("Added " + (items.size() - foundCount) + " songs that weren't already queued up.");
        }
    }

    public void addItem(MusicFile item){
        if (item == null) {
            return;
        }
        boolean found = false;
        for(MusicFile song : queue.songs) {
            if(song.Id.equalsIgnoreCase(item.Id)) {
                found = true;
                break;
            }
        }
        if (!found) {
            queue.songs.add(item);
        }

        queue.updateReason = MusicQueue.UpdateReason.ITEM_ADDED;
        queue.currentIndex = queue.currentIndex == null ? queue.songs.size() - 1 : queue.currentIndex;
        notifyObservers();
        if(!found){
            Util.toast("Added to the queue.");
        } else {
            Util.toast("Not added to queue because it is already there.");
        }
    }

    public void shuffle(){
        LoadingIndicator.setLoading(true);
        queue.currentIndex = 0;
        Collections.shuffle(queue.songs);
        queue.playerState = MusicQueue.PlayerState.IDLE;
        queue.updateReason = MusicQueue.UpdateReason.SHUFFLE;
        notifyObservers();
    }

    public void setPlayerState(MusicQueue.PlayerState playerState){
        if(queue.playerState != playerState){
            queue.playerState = playerState;
            queue.updateReason = PLAYER_STATE_CHANGED;
            notifyObservers(false);
        }
    }

    public MusicQueue getQueue(){
        return queue;
    }

    private void notifyObservers(){
        notifyObservers(true);
    }

    private void notifyObservers(boolean persistChanges){
        if(persistChanges){
            save();
        }
        for(Observer<MusicQueue> observer: observers){
            observer.onChanged(queue);
        }
    }

    public Call saveQueueAsPlaylist(String playlistName) {
        MusicPlaylist playlist = new MusicPlaylist();
        playlist.name = playlistName;
        playlist.songs = queue.songs;
        if(playlist.songs.size() > 0){
            return ApiClient.getInstance().savePlaylist(playlist);
        }
        return null;
    }

    public Call updatePlaylistFromQueue(String playlistId, String playlistName){
        if(queue.songs.size() > 0){
            MusicPlaylist playlist = new MusicPlaylist();
            playlist.name = playlistName;
            playlist.id = playlistId;
            playlist.songs = queue.songs;
            return ApiClient.getInstance().savePlaylist(playlist);
        }
        return null;
    }

    public Call renamePlaylist(MusicPlaylist playlist, String newName){
        playlist.name = newName;
        return ApiClient.getInstance().savePlaylist(playlist);
    }
}
