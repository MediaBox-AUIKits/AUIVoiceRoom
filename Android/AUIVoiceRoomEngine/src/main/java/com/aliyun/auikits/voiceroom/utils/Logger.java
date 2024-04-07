package com.aliyun.auikits.voiceroom.utils;

import android.util.Log;

public class Logger {
    public static boolean LOG_ON = true;
    public static void d(String tag, String msg){
        if(LOG_ON)
            Log.d(tag, msg);
    }

    public static void i(String tag, String msg){
        if(LOG_ON)
            Log.i(tag, msg);
    }

    public static void e(String tag, String msg){
        if(LOG_ON)
            Log.e(tag, msg);
    }

    public static void w(String tag, String msg){
        if(LOG_ON)
            Log.w(tag, msg);
    }
}
