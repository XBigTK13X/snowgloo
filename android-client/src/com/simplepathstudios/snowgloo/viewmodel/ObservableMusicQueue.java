package com.simplepathstudios.snowgloo.viewmodel;

import android.util.Log;

import androidx.lifecycle.Observer;

import com.simplepathstudios.snowgloo.LoadingIndicator;
import com.simplepathstudios.snowgloo.api.ApiClient;
import com.simplepathstudios.snowgloo.api.model.MusicFile;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;
import com.simplepathstudios.snowgloo.api.model.MusicQueuePayload;
import com.simplepathstudios.snowgloo.audio.AudioPlayer;
import com.squareup.picasso.Picasso;

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

    public void previousIndex(){
        if(queue.currentIndex != null && queue.currentIndex > 0){
            queue.currentIndex -= 1;
        }
        queue.updateReason = MusicQueue.UpdateReason.TRACK_CHANGED;
        notifyObservers();
    }

    public void nextIndex(){
        if(queue.currentIndex != null && queue.songs != null && queue.songs.size() - 1 > queue.currentIndex){
            queue.currentIndex += 1;
        }
        queue.updateReason = MusicQueue.UpdateReason.TRACK_CHANGED;
        notifyObservers();
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
            return;
        }
        for(MusicFile item : items){
            boolean found = false;
            for(MusicFile song : queue.songs){
                if(song.Id.equalsIgnoreCase(item.Id)) {
                    found = true;
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
}
