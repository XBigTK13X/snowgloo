package com.simplepathstudios.snowgloo.viewmodel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.simplepathstudios.snowgloo.LoadingIndicator;
import com.simplepathstudios.snowgloo.api.ApiClient;
import com.simplepathstudios.snowgloo.api.model.MusicFile;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;
import com.simplepathstudios.snowgloo.api.model.MusicQueuePayload;

import java.util.ArrayList;
import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MusicQueueViewModel extends ViewModel {
    public enum SelectionMode {
        UserChoice,
        PlayerAction
    }

    public MutableLiveData<MusicQueue> Data;
    private boolean firstLoad;
    public MusicQueueViewModel(){
        Data = new MutableLiveData<MusicQueue>();
        Data.setValue(MusicQueue.EMPTY);
        this.firstLoad = true;
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
                Log.d("MusicQueueViewModel","Successful load");
                LoadingIndicator.setLoading(false);
                MusicQueue musicQueue = response.body();
                musicQueue.updateReason = firstLoad ? MusicQueue.UpdateReason.SERVER_FIRST_LOAD : MusicQueue.UpdateReason.SERVER_RELOAD;
                firstLoad = false;
                Data.setValue(musicQueue);
            }

            @Override
            public void onFailure(Call<MusicQueue> call, Throwable t) {
                Log.e("MusicQueueViewModel","Failed",t);
                LoadingIndicator.setLoading(false);
            }
        });
    }

    private void save(MusicQueue musicQueue){
        ApiClient.getInstance().setQueue(musicQueue).enqueue(new Callback< MusicQueuePayload >(){
            @Override
            public void onResponse(Call<MusicQueuePayload> call, Response<MusicQueuePayload> response) {
                Log.d("MusicQueueViewModel.save","Successful save");
                Data.setValue(musicQueue);
                LoadingIndicator.setLoading(false);
            }

            @Override
            public void onFailure(Call<MusicQueuePayload> call, Throwable t) {
                Log.e("MusicQueueViewModel.save","Failed",t);
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
                Data.setValue(musicQueue);
                LoadingIndicator.setLoading(false);
            }

            @Override
            public void onFailure(Call<MusicQueue> call, Throwable t) {
                LoadingIndicator.setLoading(false);
                Log.e("MusicQueueViewModel.clear","Failed",t);
            }
        });
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
}
