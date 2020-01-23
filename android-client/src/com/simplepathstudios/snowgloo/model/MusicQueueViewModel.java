package com.simplepathstudios.snowgloo.model;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.simplepathstudios.snowgloo.api.ApiClient;
import com.simplepathstudios.snowgloo.api.model.MusicFile;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MusicQueueViewModel extends ViewModel {
    public MutableLiveData<MusicQueue> Data;
    public MusicQueueViewModel(){
        Data = new MutableLiveData<MusicQueue>();
        Data.setValue(MusicQueue.EMPTY);
    }

    public void load(){
        Log.d("MusicQueueViewModel","Loading");
        ApiClient.getInstance().getQueue().enqueue(new Callback< MusicQueue >(){

            @Override
            public void onResponse(Call<MusicQueue> call, Response<MusicQueue> response) {
                Log.d("MusicQueueViewModel","Successful load");
                Data.setValue(response.body());
            }

            @Override
            public void onFailure(Call<MusicQueue> call, Throwable t) {
                Log.e("MusicQueueViewModel","Failed",t);
            }
        });
    }

    public MusicFile getCurrent(){
        MusicQueue queue = Data.getValue();
        if(queue.songs == null || queue.currentIndex == null){
            return MusicFile.EMPTY;
        }
        if(queue.songs.size() > queue.currentIndex && queue.currentIndex > -1){
            return queue.songs.get(queue.currentIndex);
        }
        return MusicFile.EMPTY;
    }

    public void setCurrentIndex(int currentIndex){
        MusicQueue musicQueue = Data.getValue();
        if(musicQueue.currentIndex != null && musicQueue.currentIndex == currentIndex){
            return;
        }
        musicQueue.currentIndex = currentIndex;
        Data.setValue(musicQueue);
    }

    public void removeItem(int position){
        MusicQueue musicQueue = Data.getValue();
        musicQueue.songs.remove(position);
        musicQueue.currentIndex --;
        Data.setValue(musicQueue);
    }

    public void moveItem(MusicFile item, int position) {
        MusicQueue musicQueue = Data.getValue();
        musicQueue.songs.remove(position);
        musicQueue.songs.add(position,item);
        Data.setValue(musicQueue);
    }
}
