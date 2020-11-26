package com.cms.player;

import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

import java.io.IOException;

public abstract class AbstractPlayer {
    protected OnCompletionListener onCompletionListener;
    protected OnPreparedListener onPreparedListener;
    protected OnErrorListener onErrorListener;
    protected OnInfoListener onInfoListener;
    protected OnSeekCompleteListener onSeekCompleteListener;

    abstract public void setDataSource(String url) throws IOException;
    abstract public void setDisplay(SurfaceHolder surfaceHolder);
    abstract public void prepareAsync();
    abstract public void start();
    abstract public void setSpeed(float speed) throws Exception;
    abstract public void pause();
    abstract public void seekTo(int timeMs);
    abstract public void stop();
    abstract public void reset();
    abstract public void release();
    abstract public void selectTrack(int trackId);
    abstract public void deselectTrack(int trackId);
    abstract public void setAudioStreamType(int streamtype);
    abstract public TrackInfo[] getTrackInfo();

    abstract public int getVideoWidth();
    abstract public int getVideoHeight();

    abstract public int getCurrentPosition();
    abstract public int getDuration();
    abstract public boolean isPlaying();


    public interface OnCompletionListener{
        void onCompletion(AbstractPlayer ap);
    }
    public void setOnCompletionListener(OnCompletionListener onCompletionListener){
        this.onCompletionListener = onCompletionListener;
    }
    public interface OnPreparedListener{
        void onPrepared(AbstractPlayer ap);
    }
    public void setOnPreparedListener(OnPreparedListener onPreparedListener){
        this.onPreparedListener = onPreparedListener;
    }
    public interface OnErrorListener{
        boolean onError(AbstractPlayer ap, int what, int extra);
    }
    public void setOnErrorListener(OnErrorListener onErrorListener){
        this.onErrorListener = onErrorListener;
    }
    public interface OnInfoListener{
        boolean onInfo(AbstractPlayer ap, int what, int extra);
    }
    public void setOnInfoListener(OnInfoListener onInfoListener){
        this.onInfoListener = onInfoListener;
    }
    public interface OnSeekCompleteListener{
        void onSeekComplete(AbstractPlayer ap);
    }
    public void setOnSeekCompleteListener(OnSeekCompleteListener onSeekCompleteListener){
        this.onSeekCompleteListener = onSeekCompleteListener;
    }

    // 只是为了快速合入，后面要修正改法
    public abstract MediaPlayer getObject();

    public static class TrackInfo{
        public static final int MEDIA_TRACK_TYPE_UNKNOWN = 0;
        public static final int MEDIA_TRACK_TYPE_VIDEO = 1;
        public static final int MEDIA_TRACK_TYPE_AUDIO = 2;
        public static final int MEDIA_TRACK_TYPE_TIMEDTEXT = 3;
        public static final int MEDIA_TRACK_TYPE_SUBTITLE = 4;
        public static final int MEDIA_TRACK_TYPE_METADATA = 5;

        protected int mTrackType;
        protected MediaFormat mFormat;

        public TrackInfo(MediaPlayer.TrackInfo trackInfo){
            mTrackType = trackInfo.getTrackType();
            mFormat = trackInfo.getFormat();
        }
        public int getTrackType(){
            return mTrackType;
        }
        public MediaFormat getFormat(){
            return mFormat;
        }
        public String getLanguage() {
            String language = mFormat.getString(MediaFormat.KEY_LANGUAGE);
            return language == null ? "und" : language;
        }
        @Override
        @NonNull public String toString() {
            StringBuilder out = new StringBuilder(128);
            out.append(getClass().getName());
            out.append('{');
            switch (mTrackType) {
                case MEDIA_TRACK_TYPE_VIDEO:
                    out.append("VIDEO");
                    break;
                case MEDIA_TRACK_TYPE_AUDIO:
                    out.append("AUDIO");
                    break;
                case MEDIA_TRACK_TYPE_TIMEDTEXT:
                    out.append("TIMEDTEXT");
                    break;
                case MEDIA_TRACK_TYPE_SUBTITLE:
                    out.append("SUBTITLE");
                    break;
                default:
                    out.append("UNKNOWN");
                    break;
            }
            out.append(", ").append(mFormat.toString());
            out.append("}");
            return out.toString();
        }
    }
}
