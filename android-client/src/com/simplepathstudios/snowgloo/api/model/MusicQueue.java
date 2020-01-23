package com.simplepathstudios.snowgloo.api.model;

import java.util.ArrayList;
import java.util.List;

public class MusicQueue {
    public static final MusicQueue EMPTY = new MusicQueue();
    public List<MusicFile> songs = new ArrayList<MusicFile>();
    public Integer currentIndex = 0;
}
