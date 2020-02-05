package com.simplepathstudios.snowgloo.audio;


import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.Cast.Listener;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaSeekOptions;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.images.WebImage;
import com.simplepathstudios.snowgloo.MainActivity;
import com.simplepathstudios.snowgloo.api.model.MusicFile;

import static com.google.android.gms.cast.MediaSeekOptions.RESUME_STATE_UNCHANGED;
import static com.google.android.gms.cast.MediaStatus.IDLE_REASON_FINISHED;

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
            RemoteMediaClient.Listener idleListener = new RemoteMediaClient.Listener() {
                @Override
                public void onStatusUpdated() {
                    if(media != null){
                        MediaStatus mediaStatus = media.getMediaStatus();

                        MediaInfo mediaInfo = media.getMediaInfo();
                        if(mediaStatus != null){
                            Log.d(TAG, "remote media status updated playerState=>"+mediaStatus.getPlayerState());
                            int playerState = mediaStatus.getPlayerState();
                            int idleReason = mediaStatus.getIdleReason();
                            if(playerState == MediaStatus.PLAYER_STATE_IDLE && idleReason == IDLE_REASON_FINISHED){
                                Log.d(TAG, "Should be going to the next song after " + musicFile.Id);
                                media.removeListener(this);
                                AudioPlayer.getInstance().next();
                            }
                        }
                        else {
                            Log.d(TAG, "remote media status changed => media status is null");
                        }
                    }
                    else {
                        Log.d(TAG, "remote media status changed => media player is null");
                    }
                }

                @Override
                public void onMetadataUpdated() {
                    Log.d(TAG, "remote media metadata updated");
                }

                @Override
                public void onQueueStatusUpdated() {
                    Log.d(TAG, "remote media queue status updated");
                }

                @Override
                public void onPreloadStatusUpdated() {
                    Log.d(TAG, "remote media preload status updated");
                }

                @Override
                public void onSendingRemoteMediaRequest() {
                    Log.d(TAG, "remote media sending remote media request updated");
                }

                @Override
                public void onAdBreakStatusUpdated() {
                    Log.d(TAG, "remote media ad break status updated");
                }
            };
            media.addListener(idleListener);
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
