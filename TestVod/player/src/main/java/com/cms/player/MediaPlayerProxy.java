package com.cms.player;

import android.content.Context;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

import com.cms.base.ULog;

import java.io.IOException;

/**
 * 增加了状态管理，简化了SurfaceView的配置
 * 将一些繁琐的状态判断装进这个黑箱子，对外部来说，外部只能控制播放流程
 */
public class MediaPlayerProxy implements SurfaceHolder.Callback,
        MediaPlayerBase.OnPreparedListener,
        MediaPlayerBase.OnCompletionListener,
        MediaPlayerBase.OnSeekCompleteListener,
        MediaPlayerBase.OnInfoListener,
        MediaPlayerBase.OnErrorListener {
    private static final String TAG = "MediaPlayerProxy";

    private String playerType;
    private MediaPlayerBase mediaPlayerBase;
    private SurfaceHolder surfaceHolder;
    private boolean isSurfaceViewPrepared = false;
    private Context context;

    private boolean needDelayPrepareAsync = false;
    private String delayUrl;

    private boolean pauseFlag = true;

    private MediaPlayerBase.OnPreparedListener onPreparedListener;
    private MediaPlayerBase.OnSeekCompleteListener onSeekCompleteListener;
    private MediaPlayerBase.OnInfoListener onInfoListener;
    private MediaPlayerBase.OnErrorListener onErrorListener;
    private MediaPlayerBase.OnCompletionListener onCompletionListener;

    private MediaPlayerBase.PositionChangeListener positionChangeListener;
    private MediaPlayerBase.StatusChangeListener statusChangeListener;

    /**
     * 需要在主线程中创建，否则surfaceHolder的配置会存在问题
     */
    public MediaPlayerProxy(Context context, SurfaceHolder surfaceHolder, @NonNull String playerType) {
        this.context = context;
        this.playerType = playerType;
        this.surfaceHolder = surfaceHolder;
        if(surfaceHolder.getSurface().isValid()){
            isSurfaceViewPrepared = true;
        }
        surfaceHolder.addCallback(this);
        mediaPlayerBase = new MediaPlayerBase(context, playerType);

        mediaPlayerBase.setOnPreparedListener(this);
        mediaPlayerBase.setOnSeekCompleteListener(this);
        mediaPlayerBase.setOnCompletionListener(this);
        mediaPlayerBase.setOnInfoListener(this);
        mediaPlayerBase.setOnErrorListener(this);
    }

    public void setOnPreparedListener(MediaPlayerBase.OnPreparedListener onPreparedListener){
        this.onPreparedListener = onPreparedListener;
    }
    public void setOnInfoListener(MediaPlayerBase.OnInfoListener onInfoListener){
        this.onInfoListener = onInfoListener;
    }
    public void setOnSeekCompleteListener(MediaPlayerBase.OnSeekCompleteListener onSeekCompleteListener){
        this.onSeekCompleteListener = onSeekCompleteListener;
    }
    public void setOnCompletionListener(MediaPlayerBase.OnCompletionListener onCompletionListener){
        this.onCompletionListener = onCompletionListener;
    }
    public void setOnErrorListener(MediaPlayerBase.OnErrorListener onErrorListener){
        this.onErrorListener = onErrorListener;
    }
    public void setStatusChangeListener(MediaPlayerBase.StatusChangeListener statusChangeListener){
        this.statusChangeListener = statusChangeListener;
        mediaPlayerBase.setStatusChangeListener(statusChangeListener);
    }
    public void setPositionChangeListener(MediaPlayerBase.PositionChangeListener positionChangeListener){
        this.positionChangeListener = positionChangeListener;
        mediaPlayerBase.setPositionChangeListener(positionChangeListener);
    }

    public void release() {
        switch (mediaPlayerBase.getStatus()){
            case End:
                break;
            default:
                mediaPlayerBase.release();
        }
        surfaceHolder.removeCallback(this);
    }

    public void playOrPause() {
        if(!isPrepared()){
            return;
        }
        if(pauseFlag){
            pauseMediaPlayer();
        }else{
            resumeMediaPlayer();
        }
    }

    public void play(String url){
        ULog.i(TAG, "play: url=" + url);
        startPlay(url);
    }

    public void stopPlay(){
        switch (mediaPlayerBase.getStatus()){
            case End:
            case Idle:
            case Error:
            case Stopped:
                break;
            case PlayCompleted:
                mediaPlayerBase.stop();
                break;
            case Preparing:
                mediaPlayerBase.reset();
                break;
            default:
                mediaPlayerBase.stop();
                break;
        }
    }

    private void initMediaPlayer() {
        switch (mediaPlayerBase.getStatus()) {
            case Error:
                mediaPlayerBase.release();
            case End:
                mediaPlayerBase = new MediaPlayerBase(context, playerType);
                mediaPlayerBase.setOnPreparedListener(this);
                mediaPlayerBase.setOnSeekCompleteListener(this);
                mediaPlayerBase.setOnCompletionListener(this);
                mediaPlayerBase.setOnInfoListener(this);
                mediaPlayerBase.setOnErrorListener(this);
                mediaPlayerBase.setStatusChangeListener(statusChangeListener);
                mediaPlayerBase.setPositionChangeListener(positionChangeListener);
                break;
            case Idle:
                return;
            default:
                mediaPlayerBase.reset();
                break;
        }
    }

    private void startPlay(String url){
        stopPlay();
        initMediaPlayer();
        if (isSurfaceViewPrepared) {
            mediaPlayerBase.setDisplay(surfaceHolder);
            try {
                mediaPlayerBase.setDataSource(url);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            mediaPlayerBase.prepareAsync();
        }else{
            needDelayPrepareAsync = true;
            delayUrl = url;
        }
    }

    private boolean isPrepared(){
        switch (mediaPlayerBase.getStatus()){
            case Error:
            case End:
            case Idle:
            case Preparing:
                return false;
            default:
                return true;
        }
    }
    public int getDuration(){
        if(isPrepared()){
            return mediaPlayerBase.getDuration();
        }else{
            return 0;
        }
    }

    public void seekTo(int targetPos){
        if(isPrepared()){
            mediaPlayerBase.seekTo(targetPos);
        }
    }

    public int getCurrentPosition(){
        if(isPrepared()){
            return mediaPlayerBase.getCurrentPosition();
        }else{
            return 0;
        }
    }

    public void pauseMediaPlayer(){
        if(isPrepared()){
            mediaPlayerBase.pause();
            pauseFlag = false;
        }
    }

    public void resumeMediaPlayer(){
        if(isPrepared()){
            mediaPlayerBase.start();
            pauseFlag = true;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        ULog.i(TAG, "surfaceCreated: SurfaceHolder@" + System.identityHashCode(holder));
        isSurfaceViewPrepared = true;
        switch (mediaPlayerBase.getStatus()) {
            case Idle:
            case Prepared:
                mediaPlayerBase.setDisplay(holder);
                if(needDelayPrepareAsync){
                    try {
                        mediaPlayerBase.setDataSource(delayUrl);
                    } catch (IOException e) {
                        e.printStackTrace();
                        needDelayPrepareAsync = false;
                        return;
                    }
                    mediaPlayerBase.prepareAsync();
                    needDelayPrepareAsync = false;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        ULog.i(TAG, "surfaceChanged: SurfaceHolder@" + System.identityHashCode(holder) + "; width=" + width + "; height=" + height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        ULog.i(TAG, "surfaceDestroyed: SurfaceHolder@" + System.identityHashCode(holder));
        isSurfaceViewPrepared = false;
    }

    @Override
    public void onPrepared(MediaPlayerBase mpb) {
        mediaPlayerBase.start();
        pauseFlag = true;

        if(onPreparedListener != null){
            onPreparedListener.onPrepared(mpb);
        }
    }

    @Override
    public void onCompletion(MediaPlayerBase mpb) {
        if(onCompletionListener != null){
            onCompletionListener.onCompletion(mpb);
        }
    }

    @Override
    public boolean onError(MediaPlayerBase mpb, int what, int extra) {
        if(onErrorListener != null){
            return onErrorListener.onError(mpb, what, extra);
        }
        return true;
    }

    @Override
    public boolean onInfo(MediaPlayerBase mpb, int what, int extra) {
        if(onInfoListener != null){
            onInfoListener.onInfo(mpb, what, extra);
        }
        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayerBase mpb) {
        if(onSeekCompleteListener != null){
            onSeekCompleteListener.onSeekComplete(mpb);
        }
    }
}
