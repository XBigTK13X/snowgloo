package com.simplepathstudios.snowgloo.api;

import com.google.android.exoplayer2.util.Log;
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
            Log.e("ApiClient", "ApiClient is not ready");
        }
        return __instance;
    }

    public static void retarget(String serverUrl, String username){
        __instance = new ApiClient(serverUrl, username);
    }

    private ApiService httpClient;
    private String username;
    private ApiClient(String serverUrl, String username){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.username = username;
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

    public Call getUserList(){
        return this.httpClient.getUserList();
    }
}
