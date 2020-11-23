package com.cms.player;

import android.view.Surface;

/**
 * 该层的意义：
 * 1. 打印
 * 2. 状态变化
 * 3. 播放计时器
 */
public class MediaPlayerBase implements
        AbstractMediaPlayer.OnErrorListener,
        AbstractMediaPlayer.OnInfoListener,
        AbstractMediaPlayer.OnPreparedListener,
        AbstractMediaPlayer.OnPlayCompletedListener,
        AbstractMediaPlayer.OnSeekCompletedListener {
    private AbstractMediaPlayer amp;
    private OnPreparedListener onPreparedListener;
    private OnInfoListener onInfoListener;
    private OnSeekCompletedListener onSeekCompletedListener;
    private OnErrorListener onErrorListener;
    private OnPlayCompletedListener onPlayCompletedListener;

    private OnStateChangeLisntener onStateChangeLisntener;
    private OnPlayTimeChangeListener onPlayTimeChangeListener;

    private enum State{
        Idle,
        Init, PrepareAsync, Prepared,
        Playing, Pause, Buffering, Seeking,
        PlayCompleted, Stop, Error,
        End
    }
    private State state = State.Idle;

    public enum PlayerType{
        STANDARD_PLAYER,
        FFMPEG_PLAYER
    }
    private PlayerType playerType = PlayerType.STANDARD_PLAYER;

    public MediaPlayerBase(PlayerType playerType){
        if(playerType == null){
            amp = new StandardPlayer();
            playerType = PlayerType.STANDARD_PLAYER;
            return;
        }
        switch (playerType){
            case STANDARD_PLAYER:
                amp = new StandardPlayer();
                playerType = PlayerType.STANDARD_PLAYER;
                break;
            case FFMPEG_PLAYER:
                amp = new FfmpegPlayer();
                playerType = PlayerType.FFMPEG_PLAYER;
                break;
        }
        amp.setOnPreparedListener(this);
        amp.setOnInfoListener(this);
        amp.setOnSeekCompletedListener(this);
        amp.setOnErrorListener(this);
        amp.setOnPlayCompletedListener(this);
    }

    public void setSurface(Surface surface){
        amp.setSurface(surface);
    }
    public void setDataSource(String path) throws Exception{
        amp.setDataSource(path);
    }
    public void prepareAsync(){
        amp.prepareAsync();
    }
    public void start(){
        amp.start();
    }
    public void pause(){
        amp.pause();
    }
    public void resume(){
        amp.resume();
    }
    public void stop(){
        amp.stop();
    }
    public void reset(){
        amp.reset();
    }
    public void release(){
        amp.release();
    }
    @Override
    public void onPrepared(AbstractMediaPlayer amp) {
        if(onPreparedListener != null)
            onPreparedListener.onPrepared(this);
    }
    @Override
    public void onInfo(AbstractMediaPlayer amp, int what, int ext1) {
        if(onInfoListener != null)
            onInfoListener.onInfo(this, what, ext1);
    }
    @Override
    public void onSeekCompleted(AbstractMediaPlayer amp) {
        if(onSeekCompletedListener != null)
            onSeekCompletedListener.onSeekCompleted(this);
    }
    @Override
    public void onError(AbstractMediaPlayer amp, int what, int ext1) {
        if(onErrorListener != null)
            onErrorListener.onError(this, what, ext1);
    }
    @Override
    public void onPlayCompleted(AbstractMediaPlayer amp) {
        if(onPlayCompletedListener != null)
            onPlayCompletedListener.onPlayCompleted(this);
    }

    public void setOnPreparedListener(OnPreparedListener onPreparedListener){
        this.onPreparedListener = onPreparedListener;
    }
    public interface OnPreparedListener{
        void onPrepared(MediaPlayerBase mpb);
    }
    public void setOnInfoListener(OnInfoListener onInfoListener){
        this.onInfoListener = onInfoListener;
    }
    public interface OnInfoListener{
        void onInfo(MediaPlayerBase mpb, int what, int ext1);
    }
    public void setOnSeekCompletedListener(OnSeekCompletedListener onSeekCompletedListener){
        this.onSeekCompletedListener = onSeekCompletedListener;
    }
    public interface OnSeekCompletedListener{
        void onSeekCompleted(MediaPlayerBase mpb);
    }
    public void setOnErrorListener(OnErrorListener onErrorListener){
        this.onErrorListener = onErrorListener;
    }
    public interface OnErrorListener{
        void onError(MediaPlayerBase mpb, int what, int ext1);
    }
    public void setOnPlayCompletedListener(OnPlayCompletedListener onPlayCompletedListener){
        this.onPlayCompletedListener = onPlayCompletedListener;
    }
    public interface OnPlayCompletedListener{
        void onPlayCompleted(MediaPlayerBase mpb);
    }

    private interface OnStateChangeLisntener{
        void onStateChange(State oldState, State newState);
    }

    private interface OnPlayTimeChangeListener{
        void onPlayTimeChange(long playTime);
    }
}
