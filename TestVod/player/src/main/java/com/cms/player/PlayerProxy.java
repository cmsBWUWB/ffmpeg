package com.cms.player;

import android.view.Surface;

public class PlayerProxy implements MediaPlayerBase.OnPreparedListener {
    private MediaPlayerBase mpb;
    private Surface surface;

    public PlayerProxy(Surface surface){
        this.surface = surface;
    }
    public void startPlay(String path) throws Exception {
        if(mpb == null){
            mpb = new MediaPlayerBase(MediaPlayerBase.PlayerType.STANDARD_PLAYER);
        }
        mpb.reset();

        if(surface != null && surface.isValid())
            mpb.setSurface(surface);
        mpb.setDataSource(path);
        mpb.prepareAsync();
        mpb.setOnPreparedListener(this);
    }
    public void pausePlay(){
        mpb.pause();
    }
    public void resumePlay(){
        mpb.start();
    }
    public void stopPlay(){
        mpb.stop();
        mpb.reset();
        mpb.release();
    }

    @Override
    public void onPrepared(MediaPlayerBase mpb) {
        mpb.start();
    }
}
