package com.simplepathstudios.snowgloo.audio;


import android.net.Uri;
import android.util.Log;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaSeekOptions;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;
import com.simplepathstudios.snowgloo.MainActivity;
import com.simplepathstudios.snowgloo.api.model.MusicFile;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;
import com.simplepathstudios.snowgloo.viewmodel.ObservableMusicQueue;

import static com.google.android.gms.cast.MediaSeekOptions.RESUME_STATE_PLAY;
import static com.google.android.gms.cast.MediaSeekOptions.RESUME_STATE_UNCHANGED;
import static com.google.android.gms.cast.MediaStatus.IDLE_REASON_FINISHED;
import static com.google.android.gms.cast.MediaStatus.PLAYER_STATE_PLAYING;

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
            @SuppressWarnings("deprecation")
            RemoteMediaClient.Listener idleListener = new RemoteMediaClient.Listener() {
                @Override
                public void onStatusUpdated() {
                    if(media != null){
                        MediaStatus mediaStatus = media.getMediaStatus();

                        if(mediaStatus != null){
                            Log.d(TAG, "remote media status updated playerState=>"+mediaStatus.getPlayerState());
                            int playerState = mediaStatus.getPlayerState();
                            int idleReason = mediaStatus.getIdleReason();
                            if(playerState == MediaStatus.PLAYER_STATE_IDLE && idleReason == IDLE_REASON_FINISHED){
                                Log.d(TAG, "Should be going to the next song after " + musicFile.Id);
                                //noinspection deprecation
                                media.removeListener(this);
                                AudioPlayer.getInstance().next();
                            }
                        }
                    }
                }
                @Override
                public void onMetadataUpdated() { }
                @Override
                public void onQueueStatusUpdated() {}
                @Override
                public void onPreloadStatusUpdated() {}
                @Override
                public void onSendingRemoteMediaRequest() {}
                @Override
                public void onAdBreakStatusUpdated() {}
            };
            media.addListener(idleListener);
            media.load(prepareMedia(musicFile), true, seekPosition);
        }
    }

    @Override
    public void stop() {
        if(media != null){
            media.stop();
        }
    }

    @Override
    public void pause() {
        if(media != null){
            media.pause();
        }
    }

    @Override
    public void seek(int position) {
        media.seek(
                new MediaSeekOptions
                        .Builder()
                        .setPosition(position)
                        .setResumeState(RESUME_STATE_UNCHANGED)
                        .build()
        );
    }

    @Override
    public void resume(int position) {
        media.seek(
                new MediaSeekOptions
                        .Builder()
                        .setPosition(position)
                        .setResumeState(RESUME_STATE_PLAY)
                        .build()
        );
    }

    public boolean isCasting(){
        return castSession != null;
    }

    @Override
    public int getCurrentPosition() {
        if(media != null && media.isPlaying()){
            return (int)media.getApproximateStreamPosition();
        }
        return 0;
    }

    @Override
    public int getSongDuration() {
        if(media != null && media.isPlaying()){
            return (int)media.getStreamDuration();
        }
        return 0;
    }

    @Override
    public void destroy() {
        if(media != null){
            media.stop();
        }
        if(sessionManager != null){
            sessionManager.endCurrentSession(true);
        }
    }

}
