package com.simplepathstudios.snowgloo.audio;


import android.net.Uri;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaSeekOptions;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;
import com.simplepathstudios.snowgloo.MainActivity;
import com.simplepathstudios.snowgloo.Util;
import com.simplepathstudios.snowgloo.api.model.MusicFile;

import static com.google.android.gms.cast.MediaSeekOptions.RESUME_STATE_PLAY;
import static com.google.android.gms.cast.MediaSeekOptions.RESUME_STATE_UNCHANGED;
import static com.google.android.gms.cast.MediaStatus.IDLE_REASON_FINISHED;

public class CastPlayer implements IAudioPlayer {
    private static final String TAG = "CastPlayer";

    private SessionManager sessionManager;
    private CastSession castSession;
    private RemoteMediaClient media;
    private Integer lastPlayerState;
    private Integer lastIdleReason;

    public CastPlayer(){
    }

    private MediaInfo prepareMedia(MusicFile musicFile){
        Util.log(TAG, "prepareMedia "+musicFile.Id);
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
    public boolean isPlaying(){
        Util.log(TAG, "isPlaying");
        if(media != null){
            return media.isPlaying();
        }
        return false;
    }

    @Override
    public void play(MusicFile musicFile, int seekPosition) {
        try{
            Util.log(TAG, "play " +musicFile.Id + " at position "+seekPosition);
            sessionManager = MainActivity.getInstance().getCastContext().getSessionManager();
            castSession = sessionManager.getCurrentCastSession();
            if(castSession != null){
                Util.log(TAG, "Cast session is not null " +castSession.getSessionId());
                media = castSession.getRemoteMediaClient();
                @SuppressWarnings("deprecation")
                RemoteMediaClient.Listener idleListener = new RemoteMediaClient.Listener() {
                    @Override
                    public void onStatusUpdated() {
                        if(media != null){
                            MediaStatus mediaStatus = media.getMediaStatus();
                            if(mediaStatus != null){
                                int playerState = mediaStatus.getPlayerState();
                                int idleReason = mediaStatus.getIdleReason();
                                if(lastIdleReason == null || lastPlayerState == null || playerState != lastPlayerState || lastIdleReason != idleReason){
                                    Util.log(TAG, "Media status is "+
                                            Util.messageNumberToText(Util.MessageKind.CastPlayerState, playerState)
                                            + " " +
                                            Util.messageNumberToText(Util.MessageKind.CastPlayerIdleReason, idleReason));
                                    lastIdleReason = idleReason;
                                    lastPlayerState = playerState;
                                    if(playerState == MediaStatus.PLAYER_STATE_IDLE && idleReason == IDLE_REASON_FINISHED){
                                        Util.log(TAG, "Should be going to the next song after " + musicFile.Id);
                                        //noinspection deprecation
                                        media.removeListener(this);
                                        AudioPlayer.getInstance().next();
                                    }
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
                //noinspection deprecation
                media.addListener(idleListener);
                //noinspection deprecation
                media.load(prepareMedia(musicFile), true, seekPosition);
            }
        }
        catch(Exception e){
            Util.error(TAG, e);
        }
    }

    @Override
    public void stop() {
        Util.log(TAG, "stop");
        if(media != null){
            media.stop();
        }
    }

    @Override
    public void pause() {
        Util.log(TAG, "pause");
        if(media != null){
            media.pause();
        }
    }

    @Override
    public void seek(int position) {
        Util.log(TAG, "seek " +position);
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
        Util.log(TAG, "resume " + position);
        media.seek(
                new MediaSeekOptions
                        .Builder()
                        .setPosition(position)
                        .setResumeState(RESUME_STATE_PLAY)
                        .build()
        );
    }

    public boolean isCasting(){
        Util.log(TAG, "isCasting is "+(castSession != null));
        return castSession != null;
    }

    @Override
    public Integer getCurrentPosition() {
        try{
            if(media != null && media.isPlaying()){
                return (int)media.getApproximateStreamPosition();
            }
        } catch(Exception swallow){
        }

        return null;
    }

    @Override
    public Integer getSongDuration() {
        if(media != null && media.isPlaying()){
            return (int)media.getStreamDuration();
        }
        return null;
    }

    @Override
    public void destroy() {
        try{
            if(media != null){
                media.stop();
            }
        } catch(Exception swallow){}

        try{
            if(sessionManager != null){
                sessionManager.endCurrentSession(true);
            }
        } catch(Exception swallow){}
    }
}
