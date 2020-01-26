package com.simplepathstudios.snowgloo.viewmodel;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.simplepathstudios.snowgloo.LoadingIndicator;
import com.simplepathstudios.snowgloo.SnowglooSettings;
import com.simplepathstudios.snowgloo.api.ApiClient;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;
import com.simplepathstudios.snowgloo.api.model.ServerInfo;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServerInfoViewModel extends ViewModel {
    public MutableLiveData<ServerInfo> Data;
    public ServerInfoViewModel(){
        Data = new MutableLiveData<>();
    }

    public void load() {
        Log.d("ServerInfoViewModel", "LoadingIndicator");
        ApiClient.getInstance().getServerInfo().enqueue(new Callback<ServerInfo>() {

            @Override
            public void onResponse(Call<ServerInfo> call, Response<ServerInfo> response) {
                Log.d("ServerInfoViewModel.load", "Successful load");
                LoadingIndicator.setLoading(false);
                Data.setValue(response.body());
            }

            @Override
            public void onFailure(Call<ServerInfo> call, Throwable t) {
                Log.e("ServerInfoViewModel.load", "Failed load", t);
                LoadingIndicator.setLoading(false);
            }
        });
    }
}
