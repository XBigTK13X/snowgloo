package com.simplepathstudios.snowgloo.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import com.simplepathstudios.snowgloo.api.model.MediaFile;


public interface ApiService {
    final String AUTH_HEADER_KEY = "X-Emby-Authorization";

    @GET("api/files")
    Call<List<MediaFile>> listFiles();
}