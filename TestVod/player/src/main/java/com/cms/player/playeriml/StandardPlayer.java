package com.cms.player.playeriml;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.SurfaceHolder;

import com.cms.base.ULog;
import com.cms.player.AbstractPlayer;

import java.io.IOException;
import java.lang.reflect.Method;

public class StandardPlayer extends AbstractPlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnInfoListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private static final String TAG = "StandardPlayer";
    private MediaPlayer mediaPlayer;

    public StandardPlayer(Context context){
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void setDataSource(String url) throws IOException {
        mediaPlayer.setDataSource(url);
    }

    @Override
    public void setDisplay(SurfaceHolder surfaceHolder) {
        mediaPlayer.setDisplay(surfaceHolder);
    }

    @Override
    public void prepareAsync() {
        mediaPlayer.prepareAsync();
    }

    @Override
    public void start() {
        mediaPlayer.start();
    }

    @Override
    public void setSpeed(float speed) throws Exception {
        Method method = MediaPlayer.class.getDeclaredMethod("setSpeed", float.class);
        method.setAccessible(true);
        method.invoke(mediaPlayer, speed);
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
    }

    @Override
    public void seekTo(int timeMs) {
        mediaPlayer.seekTo(timeMs);
    }

    @Override
    public void selectTrack(int trackId){
        ULog.d(TAG, "selectTrack: " + trackId);
        mediaPlayer.selectTrack(trackId);
    }

    @Override
    public void deselectTrack(int trackId){
        ULog.d(TAG, "deselectTrack: " + trackId);
        mediaPlayer.deselectTrack(trackId);
    }

    @Override
    public void setAudioStreamType(int streamtype){
        ULog.d(TAG, "setAudioStreamType: " + streamtype);
        mediaPlayer.setAudioStreamType(streamtype);
    }

    @Override
    public TrackInfo[] getTrackInfo(){
        ULog.d(TAG, "getTrackInfo: ");
        MediaPlayer.TrackInfo[] trackInfos_mp = mediaPlayer.getTrackInfo();
        if(trackInfos_mp == null){
            return null;
        }
        TrackInfo[] trackInfos = new TrackInfo[trackInfos_mp.length];
        for(int i = 0; i < trackInfos_mp.length; i++){
            trackInfos[i] = new TrackInfo(trackInfos_mp[i]);
        }
        return trackInfos;
    }

    @Override
    public int getVideoWidth() {
        return mediaPlayer.getVideoWidth();
    }

    @Override
    public int getVideoHeight() {
        return mediaPlayer.getVideoHeight();
    }

    @Override
    public void stop() {
        mediaPlayer.stop();
    }

    @Override
    public void release() {
        mediaPlayer.release();
    }

    @Override
    public void reset() {
        mediaPlayer.reset();
    }

    @Override
    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    @Override
    public MediaPlayer getObject() {
        return mediaPlayer;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if(onPreparedListener != null)
            onPreparedListener.onPrepared(this);
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        if(onSeekCompleteListener != null)
            onSeekCompleteListener.onSeekComplete(this);
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if(onInfoListener != null)
            return onInfoListener.onInfo(this, what, extra);
        return false;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if(onErrorListener != null)
            onErrorListener.onError(this, what, extra);
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(onCompletionListener != null)
            onCompletionListener.onCompletion(this);
    }
}
