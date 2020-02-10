package com.simplepathstudios.snowgloo.api.model;

public class MusicFile {
    public static final MusicFile EMPTY = new MusicFile(){{
        DisplayAlbum = "";
        DisplayArtist = "";
        Title = "No music playing";
        Id = null;
    }};

    public String Album;
    public String AlbumSlug;
    public String DisplayAlbum;
    public String Artist;
    public String DisplayArtist;
    public String AudioUrl;
    public Integer ReleaseYear;
    public Float ReleaseYearSort;
    public String CoverArt;
    public String Title;
    public Integer Disc;
    public Integer Track;
    public String Id;
    public String LocalFilePath;
    private String oneLineMetadata;

    public MusicFile(){}

    public String getOneLineMetadata(){
        if(oneLineMetadata == null){
            if(Id == null){
                oneLineMetadata = String.format("%s", this.Title);
            } else {
                oneLineMetadata = String.format("%s - %s - %s", this.Title, this.DisplayAlbum, this.DisplayArtist);
            }
        }
        return oneLineMetadata;
    }
}
