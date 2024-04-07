package com.aliyun.auikits.voicechat.demo;

import android.app.Application;

import com.bumptech.glide.Glide;
import com.orhanobut.hawk.Hawk;

public class AUIChatApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Hawk.init(this)
                .build();

    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Glide.get(this).onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Glide.get(this).onTrimMemory(level);
    }
}
