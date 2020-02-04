package com.simplepathstudios.snowgloo.audio;


import android.media.browse.MediaBrowser;
import android.net.Uri;
import android.provider.MediaStore;

import com.google.android.exoplayer2.ext.cast.MediaItem;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaSeekOptions;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;
import com.simplepathstudios.snowgloo.MainActivity;
import com.simplepathstudios.snowgloo.api.model.MusicFile;

import static com.google.android.gms.cast.MediaSeekOptions.RESUME_STATE_UNCHANGED;

public class CastPlayer implements IAudioPlayer {
    private static final String TAG = "CastPlayer";

    private SessionManager sessionManager;
    private CastSession castSession;
    private RemoteMediaClient media;

    public CastPlayer(){

    }

    private MediaInfo prepareMedia(MusicFile musicFile){
        MediaMetadata metadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);
        metadata.putString(MediaMetadata.KEY_TITLE, musicFile.Title);
        metadata.putString(MediaMetadata.KEY_ARTIST, musicFile.DisplayArtist);
        metadata.putString(MediaMetadata.KEY_ALBUM_TITLE, musicFile.DisplayAlbum);
        metadata.addImage(new WebImage(Uri.parse(musicFile.CoverArt)));
        return new MediaInfo.Builder(musicFile.AudioUrl)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType("audio/mpeg")
                .setMetadata(metadata)
                .build();

    }

    @Override
    public void play(MusicFile musicFile, int seekPosition) {
        sessionManager = CastContext.getSharedInstance(MainActivity.getInstance()).getSessionManager();
        castSession = sessionManager.getCurrentCastSession();
        if(castSession != null){
            media = castSession.getRemoteMediaClient();
            media.load(prepareMedia(musicFile), true, seekPosition);
        }
    }

    @Override
    public void stop() {
        media.stop();
    }

    @Override
    public void pause() {
        media.pause();
    }

    @Override
    public void seek(int percent) {
        media.seek(
                new MediaSeekOptions
                    .Builder()
                    .setPosition((int)(this.getSongDuration() * ((float)percent/100)))
                    .setResumeState(RESUME_STATE_UNCHANGED)
                    .build()
        );
    }

    @Override
    public void destroy() {
        media.stop();
        sessionManager.endCurrentSession(true);
    }

    @Override
    public void resume() {
        media.play();
    }

    @Override
    public boolean isPlaying() {
        return media.isPlaying();
    }

    public boolean isCasting(){
        return castSession != null;
    }

    @Override
    public int getCurrentPosition() {
        return (int)media.getApproximateStreamPosition();
    }

    @Override
    public int getSongDuration() {
        return (int)media.getStreamDuration();
    }
}
