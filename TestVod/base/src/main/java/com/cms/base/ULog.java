package com.cms.base;

import android.util.Log;

public class ULog {
    private static final String TAG = "SunPlayer";
    public static void v(String TAG, String message){
        Log.v(ULog.TAG, "[" + TAG + "] " + message);
    }
    public static void i(String TAG, String message){
        Log.i(ULog.TAG, "[" + TAG + "] " + message);
    }
    public static void d(String TAG, String message){
        Log.d(ULog.TAG, "[" + TAG + "] " + message);
    }
    public static void w(String TAG, String message){
        Log.w(ULog.TAG, "[" + TAG + "] " + message);
    }
    public static void e(String TAG, String message){
        Log.e(ULog.TAG, "[" + TAG + "] " + message);
    }
}
