package com.cms.player.playeriml;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.cms.player.AbstractPlayer;

import java.io.IOException;

public class FfmpegPlayer extends AbstractPlayer {
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
    public native String testFfmpeg(String sourcePath, String targetPath);

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
    public void setSpeed(float speed) throws Exception {

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
        return null;
    }
}
