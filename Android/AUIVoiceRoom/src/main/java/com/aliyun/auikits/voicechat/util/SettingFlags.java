package com.aliyun.auikits.voicechat.util;

import com.orhanobut.hawk.Hawk;

public class SettingFlags {
    public static <T> void setFlag(String key, T value) {
        Hawk.put(key, value);
    }

    public static <T> T getFlag(String key) {
        return Hawk.get(key);
    }

    public static <T> T getFlag(String key, T defaultValue) {
        return Hawk.get(key,defaultValue);
    }

    public static void deleteFlag(String key) {
        Hawk.delete(key);
    }

}
