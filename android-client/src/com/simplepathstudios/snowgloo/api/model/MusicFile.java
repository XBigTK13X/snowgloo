package com.simplepathstudios.snowgloo.api.model;

import android.os.Bundle;

import com.simplepathstudios.snowgloo.utils.Utils;

public class MusicFile {
    public String Album;
    public String Artist;
    public String AudioUrl;
    public Integer Duration;
    public String CoverImageUrl;
    public String LocalFilePath;
    public String Title;

    public MusicFile(MusicFile item) {
        this.Album = item.Album;
        this.Artist = item.Artist;
        this.AudioUrl = item.AudioUrl;
        this.Duration = item.Duration;
        this.CoverImageUrl = item.CoverImageUrl;
        this.LocalFilePath = item.LocalFilePath;
        this.Title = item.Title;
    }

    public Bundle toBundle(){
        String json = Utils.toJSON(this);
        Bundle wrapper = new Bundle();
        wrapper.putString("media-file-json",json);
        return wrapper;
    }

    public static final MusicFile fromBundle(Bundle wrapper){
        if(wrapper == null){
            return null;
        }
        String json = wrapper.getString("media-file-json");
        MusicFile item = (MusicFile)Utils.fromJSON(json, MusicFile.class);
        return item;
    }
}
