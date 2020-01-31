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

    public String getCurrentUser(){
        return username;
    }

    public Call getCoverArt(String songFilePath, String albumCoverUrl){
        return this.httpClient.getCoverArt(songFilePath, albumCoverUrl);
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

    public Call getAlbumList(){
        return this.httpClient.getAlbumList();
    }

    public Call getUserList(){
        return this.httpClient.getUserList();
    }

    public Call getServerInfo(){
        return this.httpClient.getServerInfo();
    }

    public Call search(String query){
        return this.httpClient.search(query);
    }

    public Call clearQueue(){
        return this.httpClient.clearQueue(this.username);
    }

    public Call getPlaylist(String playlistId){
        return this.httpClient.getPlaylist(playlistId);
    }

    public Call getPlaylists(){
        return this.httpClient.getPlaylists();
    }
}
