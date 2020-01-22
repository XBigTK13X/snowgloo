package com.simplepathstudios.snowgloo.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import com.simplepathstudios.snowgloo.api.model.MusicAlbum;
import com.simplepathstudios.snowgloo.api.model.MusicArtist;
import com.simplepathstudios.snowgloo.api.model.MusicFile;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;
import com.simplepathstudios.snowgloo.api.model.SnowglooUser;


public interface ApiService {
    @GET("api/song/list")
    Call<List<MusicFile>> getSongList();

    @GET("api/album/list")
    Call<List<MusicAlbum>> getAlbumList();

    @GET("api/artist/list")
    Call<List<MusicArtist>> getArtistList();

    @GET("api/artist/view")
    Call<MusicArtist> getArtist(@Query("album") String album);

    @GET("api/album/view")
    Call<MusicArtist> getAlbum(@Query("albumSlug") String albumSlug);

    @GET("api/user/list")
    Call<SnowglooUser> getUserList();

    @GET("api/queue/{username}")
    Call<MusicQueue> getQueue(@Path("username") String username);

    @POST("/api/queue/{username}")
    Call<MusicQueue> setQueue(@Path("username") String username);
}