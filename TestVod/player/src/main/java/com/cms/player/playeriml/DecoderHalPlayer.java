package com.cms.player.playeriml;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.SurfaceHolder;

import com.cms.player.AbstractPlayer;
import com.cms.player.decoderhal.DecoderHalNativeInterface;

import java.io.IOException;

public class DecoderHalPlayer extends AbstractPlayer {
    private static final String TAG = "MediaPlayerDecoderHal";
    private String url;
    private SurfaceHolder surfaceHolder;
    private DecoderHalNativeInterface decoderHalNativeInterface = new DecoderHalNativeInterface();

    public DecoderHalPlayer(Context context) {

    }

    @Override
    public void setDataSource(String url) throws IOException {

    }

    @Override
    public void setDisplay(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void prepareAsync() {

    }

    @Override
    public void start() {

    }
    @Override
    public void setSpeed(float speed) throws Exception{
        throw new UnsupportedOperationException("do not support setSpeed for exoplayer");
    }

    @Override
    public void pause() {

    }

    @Override
    public void seekTo(int timeMs) {

    }

    @Override
    public void stop() {

    }

    @Override
    public void reset() {

    }

    @Override
    public void release() {

    }

    @Override
    public void selectTrack(int trackId) {

    }

    @Override
    public void deselectTrack(int trackId) {

    }

    @Override
    public void setAudioStreamType(int streamtype) {

    }

    @Override
    public TrackInfo[] getTrackInfo() {
        return new TrackInfo[0];
    }

    @Override
    public int getVideoWidth() {
        return 0;
    }

    @Override
    public int getVideoHeight() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return 0;
    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public MediaPlayer getObject() {
        return new MediaPlayer();
    }
}
