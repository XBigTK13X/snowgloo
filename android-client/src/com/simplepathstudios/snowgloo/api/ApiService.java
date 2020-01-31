package com.simplepathstudios.snowgloo.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import com.simplepathstudios.snowgloo.api.model.AlbumList;
import com.simplepathstudios.snowgloo.api.model.AlbumView;
import com.simplepathstudios.snowgloo.api.model.ArtistList;
import com.simplepathstudios.snowgloo.api.model.ArtistView;
import com.simplepathstudios.snowgloo.api.model.CoverArt;
import com.simplepathstudios.snowgloo.api.model.MusicFile;
import com.simplepathstudios.snowgloo.api.model.MusicPlaylist;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;
import com.simplepathstudios.snowgloo.api.model.MusicQueuePayload;
import com.simplepathstudios.snowgloo.api.model.PlaylistList;
import com.simplepathstudios.snowgloo.api.model.SearchResults;
import com.simplepathstudios.snowgloo.api.model.ServerInfo;
import com.simplepathstudios.snowgloo.api.model.UserList;


public interface ApiService {
    @GET("api/song/cover-art")
    Call<CoverArt> getCoverArt(@Query("songFilePath") String songFilePath, @Query("albumCoverUrl") String albumCoverUrl);

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

    @DELETE("/api/queue/{username}")
    Call<MusicQueue> clearQueue(@Path("username") String username);

    @GET("/api/system/info")
    Call<ServerInfo> getServerInfo();

    @GET("/api/search")
    Call<SearchResults> search(@Query("query") String query);

    @GET("/api/playlist/list")
    Call<PlaylistList> getPlaylists();

    @GET("/api/playlist/view")
    Call<MusicPlaylist> getPlaylist(@Query("playlistId") String playlistId);
}