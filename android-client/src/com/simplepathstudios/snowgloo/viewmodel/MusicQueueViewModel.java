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
    public MutableLiveData<MusicQueue> Data;
    public MusicQueueViewModel(){
        Data = new MutableLiveData<MusicQueue>();
        Data.setValue(MusicQueue.EMPTY);
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
        Log.d("MusicQueueViewModel","LoadingIndicator");
        LoadingIndicator.setLoading(true);
        ApiClient.getInstance().getQueue().enqueue(new Callback< MusicQueue >(){

            @Override
            public void onResponse(Call<MusicQueue> call, Response<MusicQueue> response) {
                Log.d("MusicQueueViewModel","Successful load");
                LoadingIndicator.setLoading(false);
                MusicQueue musicQueue = response.body();
                musicQueue.updateReason = MusicQueue.UpdateReason.SERVER_RELOAD;
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
                MusicQueuePayload payload = response.body();
                Data.setValue(payload.queue);
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

    public void setCurrentIndex(Integer currentIndex){
        MusicQueue musicQueue = Data.getValue();
        if(musicQueue.currentIndex != null && musicQueue.currentIndex == currentIndex){
            return;
        }
        musicQueue.currentIndex = currentIndex;
        musicQueue.updateReason = MusicQueue.UpdateReason.CURRENT_INDEX_CHANGED;
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
        MusicQueue data = Data.getValue();
        data.songs.addAll(items);
        data.updateReason = MusicQueue.UpdateReason.ITEM_ADDED;
        save(data);
    }

    public void addItem(MusicFile item){
        MusicQueue data = Data.getValue();
        data.songs.add(item);
        data.updateReason = MusicQueue.UpdateReason.ITEM_ADDED;
        save(data);
    }

    public void shuffle(){
        LoadingIndicator.setLoading(true);
        MusicQueue queue = Data.getValue();
        queue.currentIndex = null;
        Collections.shuffle(queue.songs);
        queue.updateReason = MusicQueue.UpdateReason.SHUFFLE;
        save(queue);
    }
}
