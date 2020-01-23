package com.simplepathstudios.snowgloo.api.model;

import android.os.Bundle;

import com.simplepathstudios.snowgloo.Utils;

public class MusicFile {
    public static final MusicFile EMPTY = new MusicFile(){{
        DisplayAlbum = "";
        DisplayArtist = "";
        Title = "No music playing";
    }};

    public String Album;
    public String DisplayAlbum;
    public String Artist;
    public String DisplayArtist;
    public String AudioUrl;
    public Integer Duration;
    public String CoverImageUrl;
    public String LocalFilePath;
    public String Title;

    public MusicFile(){}

    public String getMetadata(){
        return String.format("%s\n%s\n%s", this.Title, this.DisplayAlbum, this.DisplayArtist);
    }
}
