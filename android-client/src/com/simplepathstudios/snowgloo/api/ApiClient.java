package com.simplepathstudios.snowgloo.api;

import android.content.Context;
import android.provider.Settings;

import com.simplepathstudios.snowgloo.SnowglooSettings;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static ApiService __instance;
    public static ApiService getInstance(){
        if(__instance == null){
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(SnowglooSettings.ServerUrl())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            __instance = retrofit.create(ApiService.class);
        }
        return __instance;
    }

    private ApiClient(){

    }
}
