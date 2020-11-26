package com.cms.player.decoderhal;

import android.view.Surface;

public class DecoderHalNativeInterface {
    static {
        System.loadLibrary("DecoderHalJni");
    }
    public static class VideoWindow{
        public int x;
        public int y;
        public int width;
        public int height;
    }
    public static class ProgramInfo {
        public byte streamType;
        public int aCodecType;
        public int vCodecType;
        public short vPid;
        public short aPid;
    }
    public native String stringFromJNI();
    public native boolean nativeLoadLibDecoder();
    public native boolean nativeDecInit(int decoderId);
    public native boolean nativeDecOpen(int decoderId);
    public native boolean nativeDecStart(int decoderId, ProgramInfo programInfo, VideoWindow videoWindow);
    public native boolean nativeDecData(int decoderId, byte[] buffer, int offset, int length);
    public native boolean nativeDecStop(int decoderId);
    public native boolean nativeDecClose(int decoderId);
    public native boolean nativeDecUnInit(int decoderId);
    public native boolean nativeDecSetSurface(int decoderId, Surface surface);

}
