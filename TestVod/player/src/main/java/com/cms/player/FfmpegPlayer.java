package com.cms.player;

import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;

public class FfmpegPlayer extends AbstractMediaPlayer {
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private Handler eventHandler;
    private HandlerThread eventHandlerThread;
    public FfmpegPlayer(){
        eventHandlerThread = new HandlerThread("eventHandlerThread");
        eventHandlerThread.start();
        eventHandler = new Handler(eventHandlerThread.getLooper());
    }
    public native String testFfmpeg(String sourcePath, String targetPath);

    @Override
    public void setSurface(Surface surface) {

    }

    @Override
    public void setDataSource(String path) throws Exception {

    }

    @Override
    public void prepareAsync() {

    }

    @Override
    public void start() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

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
}
