package com.simplepathstudios.snowgloo.api.model;

import android.os.Bundle;

import com.google.gson.Gson;
import com.simplepathstudios.snowgloo.utils.Utils;

public class MediaFile {
    public String Album;
    public String Artist;
    public String AudioUrl;
    public Integer Duration;
    public String CoverImageUrl;
    public String Path;
    public String Title;

    public Bundle toBundle(){
        String json = Utils.toJSON(this);
        Bundle wrapper = new Bundle();
        wrapper.putString("media-file-json",json);
        return wrapper;
    }

    public static final MediaFile fromBundle(Bundle wrapper){
        if(wrapper == null){
            return null;
        }
        String json = wrapper.getString("media-file-json");
        MediaFile item = (MediaFile)Utils.fromJSON(json,MediaFile.class);
        return item;
    }
}
