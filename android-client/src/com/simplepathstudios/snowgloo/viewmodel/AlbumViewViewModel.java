package com.simplepathstudios.snowgloo.viewmodel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.simplepathstudios.snowgloo.LoadingIndicator;
import com.simplepathstudios.snowgloo.api.ApiClient;
import com.simplepathstudios.snowgloo.api.model.AlbumView;
import com.simplepathstudios.snowgloo.api.model.ArtistList;
import com.simplepathstudios.snowgloo.api.model.MusicAlbum;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlbumViewViewModel extends ViewModel {
    public MutableLiveData<AlbumView> Data;
    public AlbumViewViewModel(){
        Data = new MutableLiveData<AlbumView>();
    }

    public void load(String albumSlug){
        ApiClient.getInstance().getAlbumView(albumSlug).enqueue(new Callback< AlbumView >(){
            @Override
            public void onResponse(Call<AlbumView> call, Response<AlbumView> response) {
                LoadingIndicator.setLoading(false);
                Data.setValue(response.body());
            }

            @Override
            public void onFailure(Call<AlbumView> call, Throwable t) {
                Log.e("ArtistListViewModel","Failed",t);
                LoadingIndicator.setLoading(false);
            }
        });
    }
}