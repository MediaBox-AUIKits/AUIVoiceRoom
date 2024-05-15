package com.aliyun.auikits.common.util;

import android.os.Handler;
import android.os.Looper;

import com.aliyun.auikits.voiceroom.callback.ActionCallback;

import java.util.Map;

public class CommonUtil {
    public static Handler UI_HANDLER = new Handler(Looper.getMainLooper());
    public static void actionCallback(ActionCallback callback, Looper targetLooper, int code, String msg, Map<String, Object> params){
        if(callback == null) return;
        if(Looper.myLooper() == targetLooper){
            callback.onResult(code, msg, params);
        }else{
            UI_HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    callback.onResult(code, msg, params);
                }
            });
        }
    }

    public static void actionCallback(ActionCallback callback, int code, String msg, Map<String, Object> params){
        actionCallback(callback, Looper.getMainLooper(), code, msg, params);
    }

    public static void actionCallback(ActionCallback callback, int code, String msg){
        actionCallback(callback, Looper.getMainLooper(), code, msg, null);
    }

    public static void runOnUI(Runnable runnable){
        if(Looper.myLooper() == Looper.getMainLooper()){
            runnable.run();
        }else{
            UI_HANDLER.post(runnable);
        }
    }

    public static String getCallMethodName() {
        return getCallMethodName(1);
    }

    public static String getCallMethodName(int backTrackCount){
        Exception e = new Exception();
        e.fillInStackTrace();
        if(e.getStackTrace().length > backTrackCount + 1)
            return e.getStackTrace()[backTrackCount + 1].getMethodName();
        return "null";
    }

    public static String getCallClassName() {
        return getCallClassName(1);
    }

    public static String getCallClassName(int backTrackCount){
        Exception e = new Exception();
        e.fillInStackTrace();
        if(e.getStackTrace().length > backTrackCount + 1)
            return e.getStackTrace()[backTrackCount + 1].getClassName();
        return "null";
    }
}
