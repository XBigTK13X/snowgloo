package com.simplepathstudios.snowgloo.viewmodel;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.simplepathstudios.snowgloo.LoadingIndicator;
import com.simplepathstudios.snowgloo.api.ApiClient;
import com.simplepathstudios.snowgloo.api.model.MusicFile;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;
import com.simplepathstudios.snowgloo.api.model.MusicQueuePayload;

import java.io.ObjectInputValidation;
import java.util.ArrayList;
import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ObservableMusicQueue {
    private static ObservableMusicQueue __instance;
    public static ObservableMusicQueue getInstance(){
        if(__instance == null){
            __instance = new ObservableMusicQueue();
        }
        return __instance;
    }

    public enum SelectionMode {
        UserChoice,
        PlayerAction
    }

    public MutableLiveData<MusicQueue> Data;
    private boolean firstLoad;
    private ArrayList<Observer<MusicQueue>> observers;

    public ObservableMusicQueue(){
        Data = new MutableLiveData<MusicQueue>();
        observers = new ArrayList<>();
        update(MusicQueue.EMPTY);
        this.firstLoad = true;
    }

    public void observe(Observer<MusicQueue> observer){
        observers.add(observer);
    }

    public MusicFile getCurrent(){
        MusicQueue queue = Data.getValue();
        if(queue.songs == null || queue.currentIndex == null){
            return MusicFile.EMPTY;
        }
        if(queue.currentIndex != null && queue.songs.size() > queue.currentIndex){
            return queue.songs.get(queue.currentIndex);
        }
        return MusicFile.EMPTY;
    }

    public void load(){
        LoadingIndicator.setLoading(true);
        ApiClient.getInstance().getQueue().enqueue(new Callback< MusicQueue >(){
            @Override
            public void onResponse(Call<MusicQueue> call, Response<MusicQueue> response) {
                Log.d("ObservableMusicQueue","Successful load");
                LoadingIndicator.setLoading(false);
                MusicQueue musicQueue = response.body();
                musicQueue.updateReason = firstLoad ? MusicQueue.UpdateReason.SERVER_FIRST_LOAD : MusicQueue.UpdateReason.SERVER_RELOAD;
                firstLoad = false;
                update(musicQueue);
            }

            @Override
            public void onFailure(Call<MusicQueue> call, Throwable t) {
                Log.e("ObservableMusicQueue","Failed",t);
                LoadingIndicator.setLoading(false);
            }
        });
    }

    private void save(MusicQueue musicQueue){
        ApiClient.getInstance().setQueue(musicQueue).enqueue(new Callback< MusicQueuePayload >(){
            @Override
            public void onResponse(Call<MusicQueuePayload> call, Response<MusicQueuePayload> response) {
                Log.d("ObservableMusicQueue.save","Successful save");
                update(musicQueue);
                LoadingIndicator.setLoading(false);
            }

            @Override
            public void onFailure(Call<MusicQueuePayload> call, Throwable t) {
                Log.e("ObservableMusicQueue.save","Failed",t);
                LoadingIndicator.setLoading(false);
            }
        });
    }

    public void clear(){
        LoadingIndicator.setLoading(true);
        ApiClient.getInstance().clearQueue().enqueue(new Callback<MusicQueue>(){
            @Override
            public void onResponse(Call<MusicQueue> call, Response<MusicQueue> response) {
                MusicQueue musicQueue = response.body();
                musicQueue.updateReason = MusicQueue.UpdateReason.CLEAR;
                update(musicQueue);
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
        MusicQueue data = Data.getValue();
        if(data.currentIndex != null && data.currentIndex > 0){
            data.currentIndex -= 1;
        }
        update(data);
    }

    public void nextIndex(){
        MusicQueue data = Data.getValue();
        if(data.currentIndex != null && data.songs != null && data.songs.size() - 1 > data.currentIndex){
            data.currentIndex += 1;
        }
        update(data);
    }

    public void setCurrentIndex(Integer currentIndex, SelectionMode selectionMode){
        MusicQueue musicQueue = Data.getValue();
        if(musicQueue.currentIndex != null && musicQueue.currentIndex == currentIndex){
            return;
        }
        musicQueue.currentIndex = currentIndex;
        musicQueue.updateReason = selectionMode == SelectionMode.UserChoice ? MusicQueue.UpdateReason.USER_CHANGED_CURRENT_INDEX : MusicQueue.UpdateReason.TRACK_CHANGED;
        save(musicQueue);
    }

    public void removeItem(int position){
        MusicQueue musicQueue = Data.getValue();
        musicQueue.songs.remove(position);
        if(musicQueue.currentIndex != null){
            if(position < musicQueue.currentIndex){
                musicQueue.currentIndex--;
            }else {
                if(position == musicQueue.currentIndex){
                    musicQueue.currentIndex = null;
                }
            }
        }
        musicQueue.updateReason = MusicQueue.UpdateReason.ITEM_REMOVED;
        save(musicQueue);
    }

    public void moveItem(MusicFile item, int fromPosition, int toPosition) {
        if(fromPosition != toPosition){
            MusicQueue musicQueue = Data.getValue();
            musicQueue.songs.remove(fromPosition);
            musicQueue.songs.add(toPosition,item);
            if(musicQueue.currentIndex != null){
                if(musicQueue.currentIndex == fromPosition){
                    musicQueue.currentIndex = toPosition;
                } else{
                    if(musicQueue.currentIndex >= fromPosition && musicQueue.currentIndex <= toPosition){
                        musicQueue.currentIndex--;
                    }
                    if(musicQueue.currentIndex <= fromPosition && musicQueue.currentIndex >= toPosition){
                        musicQueue.currentIndex++;
                    }
                }
            }
            musicQueue.updateReason = MusicQueue.UpdateReason.ITEM_MOVED;
            save(musicQueue);
        }
    }

    public void addItems(ArrayList<MusicFile> items){
        if(items == null){
            return;
        }
        MusicQueue data = Data.getValue();
        for(MusicFile item : items){
            boolean found = false;
            for(MusicFile song : data.songs){
                if(song.Id.equalsIgnoreCase(item.Id)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                data.songs.add(item);
            }
        }

        data.updateReason = MusicQueue.UpdateReason.ITEM_ADDED;
        data.currentIndex = data.currentIndex == null ? data.songs.size() - items.size():data.currentIndex;
        save(data);
    }

    public void addItem(MusicFile item){
        if (item == null) {
            return;
        }
        boolean found = false;
        MusicQueue data = Data.getValue();
        for(MusicFile song : data.songs) {
            if(song.Id.equalsIgnoreCase(item.Id)) {
                found = true;
                break;
            }
        }
        if (!found) {
            data.songs.add(item);
        }

        data.updateReason = MusicQueue.UpdateReason.ITEM_ADDED;
        data.currentIndex = data.currentIndex == null ? data.songs.size() - 1 : data.currentIndex;
        save(data);
    }

    public void shuffle(){
        LoadingIndicator.setLoading(true);
        MusicQueue queue = Data.getValue();
        queue.currentIndex = 0;
        Collections.shuffle(queue.songs);
        queue.updateReason = MusicQueue.UpdateReason.SHUFFLE;
        save(queue);
    }

    public void setPlaying(boolean playing){
        MusicQueue data = Data.getValue();
        data.isPlaying = playing;
        update(data);
    }

    private void update(MusicQueue musicQueue){
        MusicQueue current = Data.getValue();
        // TODO This seems to be properly called, but the condition is failing for some reason
        if(current == null || current.isPlaying != musicQueue.isPlaying || current.currentIndex != musicQueue.currentIndex || current.songs.size() != musicQueue.songs.size()){
            Data.postValue(musicQueue);
            for(Observer<MusicQueue> observer: observers){
                observer.onChanged(musicQueue);
            }
        }
    }
}
