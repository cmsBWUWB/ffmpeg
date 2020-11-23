package com.cms.player;

import android.view.Surface;

public abstract class AbstractMediaPlayer {
    protected OnPreparedListener onPreparedListener;
    protected OnInfoListener onInfoListener;
    protected OnSeekCompletedListener onSeekCompletedListener;
    protected OnErrorListener onErrorListener;
    protected OnPlayCompletedListener onPlayCompletedListener;

    public abstract void setSurface(Surface surface);
    public abstract void setDataSource(String path) throws Exception;
    public abstract void prepareAsync();
    public abstract void start();
    public abstract void pause();
    public abstract void resume();
    public abstract void stop();
    public abstract void reset();
    public abstract void release();

    public void setOnPreparedListener(OnPreparedListener onPreparedListener){
        this.onPreparedListener = onPreparedListener;
    }
    public interface OnPreparedListener{
        void onPrepared(AbstractMediaPlayer amp);
    }
    public void setOnInfoListener(OnInfoListener onInfoListener){
        this.onInfoListener = onInfoListener;
    }
    public interface OnInfoListener{
        void onInfo(AbstractMediaPlayer amp, int what, int ext1);
    }
    public void setOnSeekCompletedListener(OnSeekCompletedListener onSeekCompletedListener){
        this.onSeekCompletedListener = onSeekCompletedListener;
    }
    public interface OnSeekCompletedListener{
        void onSeekCompleted(AbstractMediaPlayer amp);
    }
    public void setOnErrorListener(OnErrorListener onErrorListener){
        this.onErrorListener = onErrorListener;
    }
    public interface OnErrorListener{
        void onError(AbstractMediaPlayer amp, int what, int ext1);
    }
    public void setOnPlayCompletedListener(OnPlayCompletedListener onPlayCompletedListener){
        this.onPlayCompletedListener = onPlayCompletedListener;
    }
    public interface OnPlayCompletedListener{
        void onPlayCompleted(AbstractMediaPlayer amp);
    }
}
