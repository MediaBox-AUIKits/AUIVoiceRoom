package com.aliyun.auikits.voicechat.base.image;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.LibraryGlideModule;

import java.io.InputStream;


@GlideModule
public class OkHttpGlideModule extends LibraryGlideModule {
    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        super.registerComponents(context, glide, registry);

        // 替换Glide的默认网络请求库为OkHttp
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory());
    }
}
