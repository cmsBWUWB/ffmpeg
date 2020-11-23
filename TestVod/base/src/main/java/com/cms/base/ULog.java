package com.cms.base;

import android.util.Log;

public class ULog {
    private static final String TAG = "SunPlayer";
    public static void i(String TAG, String message){
        Log.i(ULog.TAG, "[" + TAG + "] " + message);
    }
}
