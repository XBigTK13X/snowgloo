package com.simplepathstudios.snowgloo.api;

import com.simplepathstudios.snowgloo.LoadingIndicator;
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
        LoadingIndicator.setLoading(true);
        return this.httpClient.getQueue(this.username);
    }

    public Call setQueue(MusicQueue queue){
        LoadingIndicator.setLoading(true);
        MusicQueuePayload payload = new MusicQueuePayload();
        payload.queue = queue;
        return this.httpClient.setQueue(this.username, payload);
    }

    public Call getArtistList(){
        LoadingIndicator.setLoading(true);
        return this.httpClient.getArtistList();
    }

    public Call getArtistView(String artist){
        LoadingIndicator.setLoading(true);
        return this.httpClient.getArtist(artist);
    }

    public Call getAlbumView(String albumSlug){
        LoadingIndicator.setLoading(true);
        return this.httpClient.getAlbum(albumSlug);
    }

    public Call getAlbumList(){
        LoadingIndicator.setLoading(true);
        return this.httpClient.getAlbumList();
    }
}
