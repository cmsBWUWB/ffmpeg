package com.cms.exoplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.SurfaceHolder;

import com.cms.player.AbstractPlayer;

import java.io.IOException;

public class ExoPlayer extends AbstractPlayer implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnSeekCompleteListener {
    private static final String TAG = "ExoPlayer";
    private ZTEExoPlayer zteExoPlayer;

    public ExoPlayer(Context context){
        zteExoPlayer = ZTEExoPlayer.getPlayer(context);
        zteExoPlayer.setOnCompletionListener(this);
        zteExoPlayer.setOnPreparedListener(this);
        zteExoPlayer.setOnErrorListener(this);
        zteExoPlayer.setOnInfoListener(this);
        zteExoPlayer.setOnSeekCompleteListener(this);
    }

    @Override
    public void setDataSource(String url) throws IOException {
        zteExoPlayer.setDataSource(url);
    }

    @Override
    public void setDisplay(SurfaceHolder surfaceHolder) {
        zteExoPlayer.setDisplay(surfaceHolder);
    }
    @Override
    public void prepareAsync() {
        zteExoPlayer.prepareAsync();
    }

    @Override
    public void start() {
        zteExoPlayer.start();
    }

    @Override
    public void setSpeed(float speed) throws Exception{
        throw new UnsupportedOperationException("do not support setSpeed for exoplayer");
    }

    @Override
    public void pause() {
        zteExoPlayer.pause();
    }

    @Override
    public void seekTo(int timeMs) {
        zteExoPlayer.seekTo(timeMs);
    }

    @Override
    public void selectTrack(int trackId){
        zteExoPlayer.selectTrack(trackId);
    }

    @Override
    public void deselectTrack(int trackId){
        zteExoPlayer.deselectTrack(trackId);
    }

    @Override
    public void setAudioStreamType(int streamtype){
        zteExoPlayer.setAudioStreamType(streamtype);
    }

    @Override
    public TrackInfo[] getTrackInfo(){
        MediaPlayer.TrackInfo[] trackInfos_mp = zteExoPlayer.getTrackInfo();
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
        return zteExoPlayer.getVideoWidth();
    }

    @Override
    public int getVideoHeight() {
        return zteExoPlayer.getVideoHeight();
    }

    @Override
    public void stop() {
        zteExoPlayer.stop();
    }

    @Override
    public void release() {
        zteExoPlayer.release();
    }

    @Override
    public void reset() {
        zteExoPlayer.reset();
    }

    @Override
    public int getCurrentPosition() {
        return zteExoPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return zteExoPlayer.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return zteExoPlayer.isPlaying();
    }

    @Override
    public MediaPlayer getObject() {
        return new MediaPlayer();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(onCompletionListener != null)
            onCompletionListener.onCompletion(this);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if(onPreparedListener != null)
            onPreparedListener.onPrepared(this);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if(onErrorListener != null)
            return onErrorListener.onError(this, what, extra);
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if(onInfoListener != null)
            return onInfoListener.onInfo(this, what, extra);
        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        if(onSeekCompleteListener != null)
            onSeekCompleteListener.onSeekComplete(this);
    }
}
