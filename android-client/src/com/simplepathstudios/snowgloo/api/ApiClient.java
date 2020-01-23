package com.simplepathstudios.snowgloo.api;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.simplepathstudios.snowgloo.SnowglooSettings;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static ApiClient __instance;
    public static ApiClient getInstance(){
        if(__instance == null){
            __instance = new ApiClient();
        }
        return __instance;
    }

    private ApiService httpClient;
    private String username = "Snowman";
    private ApiClient(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SnowglooSettings.ServerUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.httpClient = retrofit.create(ApiService.class);
    }

    public MusicQueue getQueue(){
        try{
            return this.httpClient.getQueue(this.username).execute().body();
        } catch(Exception e){
            Log.e("ApiClient.getQueue","Unable to retrieve queue", e);
        }
        return MusicQueue.EMPTY;
    }
}
