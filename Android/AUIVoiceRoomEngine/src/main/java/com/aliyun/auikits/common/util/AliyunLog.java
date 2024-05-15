package com.aliyun.auikits.common.util;

import android.util.Log;

public final class AliyunLog {
    private static boolean LOG_ON = true;

    public static final boolean getLOG_ON() {
        return AliyunLog.LOG_ON;
    }

    public static final void setLOG_ON(boolean z) {
        AliyunLog.LOG_ON = z;
    }

    public static final void d(String tag, String msg) {
        if (getLOG_ON()) {
            Log.d(tag, msg);
        }
    }

    public static final void e(String tag, String msg) {
        if (getLOG_ON()) {
            Log.e(tag, msg);
        }
    }

    public static final void i(String tag, String msg) {
        if (getLOG_ON()) {
            Log.i(tag, msg);
        }
    }

    public static final void w(String tag, String msg) {
        if (getLOG_ON()) {
            Log.w(tag, msg);
        }
    }
}
