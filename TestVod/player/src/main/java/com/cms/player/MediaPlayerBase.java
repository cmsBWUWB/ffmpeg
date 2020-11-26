package com.cms.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

import com.cms.base.ULog;
import com.cms.player.playeriml.DecoderHalPlayer;
import com.cms.player.playeriml.ExoPlayer;
import com.cms.player.playeriml.StandardPlayer;

import java.io.IOException;

public class MediaPlayerBase implements
        AbstractPlayer.OnPreparedListener,
        AbstractPlayer.OnSeekCompleteListener,
        AbstractPlayer.OnInfoListener,
        AbstractPlayer.OnCompletionListener,
        AbstractPlayer.OnErrorListener {
    private static final String TAG = "MediaPlayerBase";

    private AutoUpdatePlayTimeHandler autoUpdatePlayTimeHandler;
    private EventHandler eventHandler;
    private MediaStatus mediaStatus = MediaStatus.Idle;
    private AbstractPlayer abstractPlayer;

    private OnPreparedListener onPreparedListener;
    private OnSeekCompleteListener onSeekCompleteListener;
    private OnInfoListener onInfoListener;
    private OnCompletionListener onCompletionListener;
    private OnErrorListener onErrorListener;

    private PositionChangeListener positionChangeListener;
    private StatusChangeListener statusChangeListener;


    public enum PLAYER{
        MEDIA_PLAYER_STANDARD,
        MEDIA_PLAYER_EXOPLAYER,
        MEDIA_PLAYER_DECODERHAL
    }

    public MediaPlayerBase(Context context, PLAYER player){
        autoUpdatePlayTimeHandler = new AutoUpdatePlayTimeHandler(Looper.getMainLooper());
        eventHandler = new EventHandler(Looper.getMainLooper());
        switch (player){
            case MEDIA_PLAYER_STANDARD:
                abstractPlayer = new StandardPlayer(context);
                break;
            case MEDIA_PLAYER_EXOPLAYER:
                abstractPlayer = new ExoPlayer(context);
                break;
            case MEDIA_PLAYER_DECODERHAL:
                abstractPlayer = new DecoderHalPlayer(context);
                break;
            default:
                abstractPlayer = new StandardPlayer(context);
                break;
        }
        abstractPlayer.setOnPreparedListener(this);
        abstractPlayer.setOnSeekCompleteListener(this);
        abstractPlayer.setOnInfoListener(this);
        abstractPlayer.setOnCompletionListener(this);
        abstractPlayer.setOnErrorListener(this);
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
        boolean onInfo(MediaPlayerBase mpb, int what, int ext1);
    }
    public void setOnSeekCompleteListener(OnSeekCompleteListener onSeekCompleteListener){
        this.onSeekCompleteListener = onSeekCompleteListener;
    }
    public interface OnSeekCompleteListener{
        void onSeekComplete(MediaPlayerBase mpb);
    }
    public void setOnErrorListener(OnErrorListener onErrorListener){
        this.onErrorListener = onErrorListener;
    }
    public interface OnErrorListener{
        boolean onError(MediaPlayerBase mpb, int what, int ext1);
    }
    public void setOnCompletionListener(OnCompletionListener onCompletionListener){
        this.onCompletionListener = onCompletionListener;
    }
    public interface OnCompletionListener{
        void onCompletion(MediaPlayerBase mpb);
    }

    public void setPositionChangeListener(PositionChangeListener positionChangeListener){
        this.positionChangeListener = positionChangeListener;
    }
    public void setStatusChangeListener(StatusChangeListener statusChangeListener){
        this.statusChangeListener = statusChangeListener;
    }

    public void setDataSource(String url) throws IOException{
        ULog.d(TAG, "setDataSource enter: " + url);
        changeStatus(MediaStatus.Init, 0, 0, url);
        abstractPlayer.setDataSource(url);
        ULog.d(TAG, "setDataSource exit: " + url);
    }
    public void setDisplay(SurfaceHolder surfaceHolder){
        ULog.d(TAG, "setDisplay enter: " + surfaceHolder);
        abstractPlayer.setDisplay(surfaceHolder);
        ULog.d(TAG, "setDisplay exit: " + surfaceHolder);
    }

    public void prepareAsync(){
        ULog.d(TAG, "prepareAsync enter: ");
        changeStatus(MediaStatus.Preparing);
        abstractPlayer.prepareAsync();
        ULog.d(TAG, "prepareAsync exit: ");
    }
    public void start(){
        ULog.d(TAG, "start enter: ");
        autoUpdatePlayTimeHandler.start(getCurrentPosition());
        changeStatus(MediaStatus.Playing);
        abstractPlayer.start();
        ULog.d(TAG, "start exit: ");
    }
    public void setSpeed(float speed) throws Exception{
        ULog.d(TAG, "setSpeed enter: ");
        abstractPlayer.setSpeed(speed);
        ULog.d(TAG, "setSpeed exit: ");
    }

    public void pause(){
        ULog.d(TAG, "pause enter: ");
        autoUpdatePlayTimeHandler.set(getCurrentPosition());
        autoUpdatePlayTimeHandler.stop();
        changeStatus(MediaStatus.Pausing);
        abstractPlayer.pause();
        ULog.d(TAG, "pause exit: ");
    }
    public void seekTo(int timeMs){
        ULog.d(TAG, "seekTo enter: " + timeMs);
        autoUpdatePlayTimeHandler.stop();
        changeStatus(MediaStatus.Seeking, 0, 0, timeMs);
        abstractPlayer.seekTo(timeMs);
        ULog.d(TAG, "seekTo exit: " + timeMs);
    }

    public void selectTrack(int trackId){
        abstractPlayer.selectTrack(trackId);
    }
    public void deselectTrack(int trackId){
        abstractPlayer.deselectTrack(trackId);
    }
    public void setAudioStreamType(int streamtype){
        abstractPlayer.setAudioStreamType(streamtype);
    }
    public AbstractPlayer.TrackInfo[] getTrackInfo(){
        return abstractPlayer.getTrackInfo();
    }

    public int getVideoWidth(){
        return abstractPlayer.getVideoWidth();
    }

    public int getVideoHeight(){
        return abstractPlayer.getVideoHeight();
    }

    public void stop(){
        ULog.d(TAG, "stop enter: ");
        autoUpdatePlayTimeHandler.stop();
        changeStatus(MediaStatus.Stopped);
        abstractPlayer.stop();
        ULog.d(TAG, "stop exit: ");
    }
    public void release(){
        ULog.d(TAG, "release enter: ");
        autoUpdatePlayTimeHandler.set(0);
        changeStatus(MediaStatus.End);
        abstractPlayer.release();
        ULog.d(TAG, "release exit: ");
    }
    public void reset(){
        ULog.d(TAG, "reset enter: ");
        autoUpdatePlayTimeHandler.set(0);
        changeStatus(MediaStatus.Idle);
        abstractPlayer.reset();
        ULog.d(TAG, "reset exit: ");
    }

    private void changeStatus(MediaStatus newStatus){
        changeStatus(newStatus, 0, 0, null);
    }

    private void changeStatus(MediaStatus newStatus, int extra1, int extra2, Object obj){
        ULog.d(TAG, "changeStatus: prevStatus=" + mediaStatus.name() + ", newStatus=" + newStatus.name());
        StatusChange statusChange = new StatusChange(mediaStatus, newStatus, obj);
        Message msg = eventHandler.obtainMessage(EventHandler.EVENT_STATUS_CHANGED);
        msg.obj = statusChange;
        eventHandler.sendMessage(msg);
        mediaStatus = newStatus;
    }

    public int getCurrentPosition(){
        return abstractPlayer.getCurrentPosition();
    }
    public int getDuration(){
        return abstractPlayer.getDuration();
    }
    public boolean isPlaying(){
        return abstractPlayer.isPlaying();
    }

    public MediaStatus getStatus(){
        return mediaStatus;
    }

    public void onPrepared(AbstractPlayer ap) {
        ULog.i(TAG, "onPrepared: [MediaPlayer:" + ap + "]");
        changeStatus(MediaStatus.Prepared);
        if(onPreparedListener != null){
            onPreparedListener.onPrepared(this);
        }
    }

    public boolean onError(AbstractPlayer ap, int what, int extra) {
        ULog.i(TAG, "onError: [mediaplayer:" + ap + "], [what:" + what + "], [extra:" + extra + "]");
        autoUpdatePlayTimeHandler.stop();
        changeStatus(MediaStatus.Error, what, extra, null);
        if(onErrorListener != null){
            return onErrorListener.onError(this, what, extra);
        }
        return true;
    }

    public void onSeekComplete(AbstractPlayer ap) {
        ULog.i(TAG, "onSeekComplete: [MediaPlayer:" + ap + "]");
        autoUpdatePlayTimeHandler.start(getCurrentPosition());
        abstractPlayer.start();
        changeStatus(isPlaying() ? MediaStatus.Playing : MediaStatus.Pausing);
        if(onSeekCompleteListener != null){
            onSeekCompleteListener.onSeekComplete(this);
        }
    }

    public void onCompletion(AbstractPlayer ap) {
        ULog.i(TAG, "onCompletion: [MediaPlayer:" + ap + "]");
        autoUpdatePlayTimeHandler.set(0);
        autoUpdatePlayTimeHandler.stop();
        changeStatus(MediaStatus.PlayCompleted);
        if(onCompletionListener != null){
            onCompletionListener.onCompletion(this);
        }
    }

    public enum MediaStatus {
        Idle, Init,
        Preparing, Prepared,
        Playing, Pausing, PlayCompleted, Stopped,
        Buffering, Seeking,
        Error, End;
        public boolean canPlay(){
            switch (this){
                case Idle:
                case Init:
                case Preparing:
                case Stopped:
                case Error:
                case End:
                    return false;
                default:
                    return true;
            }
        }
    }

public interface PositionChangeListener{
        void onPositionChange(int timeSec);
    }

    public interface StatusChangeListener{
        void onStatusChanged(MediaPlayerBase mediaPlayerBase, MediaStatus prevStatus, MediaStatus currentStatus, int extra1, int extra2, Object obj);
    }

    public boolean onInfo(AbstractPlayer ap, int what, int extra) {
        ULog.i(TAG, "onInfo: [mediaplayer:" + ap + "], [what:" + what + "], [extra:" + extra + "]");
        switch (what){
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                autoUpdatePlayTimeHandler.stop();
                changeStatus(MediaStatus.Buffering);
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                if (isPlaying()){
                    autoUpdatePlayTimeHandler.start(getCurrentPosition());
                }else{
                    autoUpdatePlayTimeHandler.set(getCurrentPosition());
                    autoUpdatePlayTimeHandler.stop();
                }
                changeStatus(MediaStatus.Playing);
                break;
        }
        if (onInfoListener != null){
            return onInfoListener.onInfo(this, what, extra);
        }
        return true;
    }
    private static class StatusChange{
        MediaStatus prevStatus;
        MediaStatus newStatus;
        Object obj;
        StatusChange(MediaStatus prevStatus, MediaStatus newStatus, Object obj){
            this.prevStatus = prevStatus;
            this.newStatus = newStatus;
            this.obj = obj;
        }
    }

    public class EventHandler extends Handler{
        final static int EVENT_STATUS_CHANGED = 0;

        EventHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if(msg.what == EVENT_STATUS_CHANGED){
                StatusChange statusChange = (StatusChange) msg.obj;
                if(statusChangeListener != null) {
                    statusChangeListener.onStatusChanged(MediaPlayerBase.this, statusChange.prevStatus, statusChange.newStatus, 0, 0, statusChange.obj);
                }
            }
            super.handleMessage(msg);
        }
    }

    public class AutoUpdatePlayTimeHandler extends Handler {
        private final static int WHAT_SET = 0;
        private final static int WHAT_START = 1;

        private int updateTimeSec = 0;

        private long realPlayTimeMs = 0;
        private long autoStartSystemTimeMs = -1;

        AutoUpdatePlayTimeHandler(Looper looper){
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case WHAT_SET:
                    updateTimeSec = (int) (realPlayTimeMs / 1000);
                    if(positionChangeListener != null) {
                        positionChangeListener.onPositionChange(updateTimeSec);
                    }
                    break;
                case WHAT_START:
                    realPlayTimeMs = System.currentTimeMillis() - autoStartSystemTimeMs;
                    updateTimeSec = (int) (realPlayTimeMs / 1000);
                    if(positionChangeListener != null) {
                        positionChangeListener.onPositionChange(updateTimeSec);
                    }
                    this.sendEmptyMessageDelayed(WHAT_START, 1000 - realPlayTimeMs % 1000);
                    break;
            }
        }
        public void start(int timeMs){
            this.removeCallbacksAndMessages(null);
            realPlayTimeMs = timeMs;
            autoStartSystemTimeMs = System.currentTimeMillis() - realPlayTimeMs;
            this.sendEmptyMessage(WHAT_START);
        }
        public void resume(){
            this.removeCallbacksAndMessages(null);
            autoStartSystemTimeMs = System.currentTimeMillis() - realPlayTimeMs;
            this.sendEmptyMessage(WHAT_START);
        }
        public void set(int timeMs){
            this.removeCallbacksAndMessages(null);
            this.realPlayTimeMs = timeMs;
            this.sendEmptyMessage(WHAT_SET);
        }
        public void stop(){
            this.removeCallbacksAndMessages(null);
            realPlayTimeMs = System.currentTimeMillis() - autoStartSystemTimeMs;
        }
    }

    //只是为了快速合入，后面要修正改法
    public MediaPlayer getMediaPlayerForSubtitle(){
        return abstractPlayer.getObject();
    }
}
