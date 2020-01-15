/*
 * Copyright (C) 2016 Google LLC. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.simplepathstudios.snowgloo.browser;

import com.simplepathstudios.snowgloo.api.ApiClient;
import com.simplepathstudios.snowgloo.api.model.MediaFile;
import com.simplepathstudios.snowgloo.utils.MediaItem;

import android.content.Context;
import androidx.loader.content.AsyncTaskLoader;
import android.util.Log;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoItemLoader extends AsyncTaskLoader<List<MediaItem>> {

    private static final String TAG = "VideoItemLoader";
    private final String mUrl;

    public VideoItemLoader(Context context, String url) {
        super(context);
        this.mUrl = url;
    }

    @Override
    public List<MediaItem> loadInBackground() {
        try {
            ApiClient.getInstance().listFiles().enqueue(new Callback<List<MediaFile>>(){
                @Override
                public void onResponse(Call<List<MediaFile>> call, Response<List<MediaFile>> response) {
                    List<MediaFile> files = response.body();
                    int x = 0;
                }

                @Override
                public void onFailure(Call<List<MediaFile>> call, Throwable t) {
                    int y = 0;
                }
            });
            return VideoProvider.buildMedia(mUrl);
        } catch (Exception e) {
            Log.e(TAG, "Failed to fetch media data", e);
            return null;
        }
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

}
