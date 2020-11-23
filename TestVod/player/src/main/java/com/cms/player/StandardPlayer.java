package com.cms.player;

import android.media.MediaPlayer;
import android.view.Surface;

import com.cms.base.ULog;

public class StandardPlayer extends AbstractMediaPlayer implements
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener {
    private static final String TAG = "StandardPlayer";
    private MediaPlayer mp;

    public StandardPlayer() {
        mp = new MediaPlayer();
        mp.setOnInfoListener(this);
        mp.setOnErrorListener(this);
        mp.setOnSeekCompleteListener(this);
        mp.setOnPreparedListener(this);
        mp.setOnCompletionListener(this);
    }
    @Override
    public void setSurface(Surface surface){
        ULog.i(TAG, "enter setSurface()");
        mp.setSurface(surface);
        ULog.i(TAG, "exit setSurface()");
    }

    @Override
    public void setDataSource(String path) throws Exception {
        ULog.i(TAG, "enter setDataSource(" + path + ")");
        mp.setDataSource(path);
        ULog.i(TAG, "exit setDataSource");
    }

    @Override
    public void prepareAsync() {
        ULog.i(TAG, "enter prepareAsync()");
        mp.prepareAsync();
        ULog.i(TAG, "exit prepareAsync()");
    }

    @Override
    public void start() {
        ULog.i(TAG, "start start()");
        mp.start();
        ULog.i(TAG, "exit start()");
    }

    @Override
    public void pause() {
        ULog.i(TAG, "start pause()");
        mp.pause();
        ULog.i(TAG, "exit pause()");
    }

    @Override
    public void resume() {
        ULog.i(TAG, "start resume()");
        mp.start();
        ULog.i(TAG, "exit resume()");
    }

    @Override
    public void stop() {
        ULog.i(TAG, "start stop()");
        mp.stop();
        ULog.i(TAG, "exit stop()");
    }

    @Override
    public void reset() {
        ULog.i(TAG, "start reset()");
        mp.reset();
        ULog.i(TAG, "exit reset()");
    }

    @Override
    public void release() {
        ULog.i(TAG, "start release()");
        mp.release();
        ULog.i(TAG, "exit release()");
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    @Override
    public void onPrepared(MediaPlayer mp) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }
}
