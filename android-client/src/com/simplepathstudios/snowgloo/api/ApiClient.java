package com.simplepathstudios.snowgloo.api;

import com.simplepathstudios.snowgloo.SnowglooSettings;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;
import com.simplepathstudios.snowgloo.api.model.MusicQueuePayload;

import retrofit2.Call;
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

    public Call getQueue(){
        return this.httpClient.getQueue(this.username);
    }

    public Call setQueue(MusicQueue queue){
        MusicQueuePayload payload = new MusicQueuePayload();
        payload.queue = queue;
        return this.httpClient.setQueue(this.username, payload);
    }

    public Call getArtistList(){
        return this.httpClient.getArtistList();
    }

    public Call getArtistView(String artist){
        return this.httpClient.getArtist(artist);
    }

    public Call getAlbumView(String albumSlug){
        return this.httpClient.getAlbum(albumSlug);
    }
}
