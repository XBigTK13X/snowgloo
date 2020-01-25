package com.simplepathstudios.snowgloo.viewmodel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.simplepathstudios.snowgloo.LoadingIndicator;
import com.simplepathstudios.snowgloo.api.ApiClient;
import com.simplepathstudios.snowgloo.api.model.ArtistList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArtistListViewModel extends ViewModel {
    public MutableLiveData<ArtistList> Data;
    public ArtistListViewModel(){
        Data = new MutableLiveData<ArtistList>();
    }

    public void load(){
        ApiClient.getInstance().getArtistList().enqueue(new Callback< ArtistList >(){

            @Override
            public void onResponse(Call<ArtistList> call, Response<ArtistList> response) {
                LoadingIndicator.setLoading(false);
                Data.setValue(response.body());
            }

            @Override
            public void onFailure(Call<ArtistList> call, Throwable t) {
                Log.e("ArtistListViewModel","Failed",t);
                LoadingIndicator.setLoading(false);
            }
        });
    }
}