package com.simplepathstudios.snowgloo.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import com.simplepathstudios.snowgloo.api.model.AlbumList;
import com.simplepathstudios.snowgloo.api.model.AlbumView;
import com.simplepathstudios.snowgloo.api.model.ArtistList;
import com.simplepathstudios.snowgloo.api.model.ArtistView;
import com.simplepathstudios.snowgloo.api.model.MusicFile;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;
import com.simplepathstudios.snowgloo.api.model.MusicQueuePayload;
import com.simplepathstudios.snowgloo.api.model.ServerInfo;
import com.simplepathstudios.snowgloo.api.model.UserList;


public interface ApiService {
    @GET("api/song/list")
    Call<List<MusicFile>> getSongList();

    @GET("api/album/list")
    Call<AlbumList> getAlbumList();

    @GET("api/artist/list")
    Call<ArtistList> getArtistList();

    @GET("api/artist/view")
    Call<ArtistView> getArtist(@Query("artist") String artist);

    @GET("api/album/view")
    Call<AlbumView> getAlbum(@Query("albumSlug") String albumSlug);

    @GET("api/user/list")
    Call<UserList> getUserList();

    @GET("api/queue/{username}")
    Call<MusicQueue> getQueue(@Path("username") String username);

    @POST("/api/queue/{username}")
    Call<MusicQueuePayload> setQueue(@Path("username") String username, @Body MusicQueuePayload queue);

    @GET("/api/system/info")
    Call<ServerInfo> getServerInfo();
}