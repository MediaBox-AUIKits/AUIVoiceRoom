package com.aliyun.auikits.voicechat.base.network;

import java.util.concurrent.ConcurrentHashMap;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitManager {

    private static ConcurrentHashMap<String, Retrofit> retrofitMap = new ConcurrentHashMap<>();

    public static Retrofit getRetrofit(String host) {
        if(retrofitMap.containsKey(host)) {
            return retrofitMap.get(host);
        } else {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(host)
                    .client(DefaultOkHttpFactory.getHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .build();
            retrofitMap.put(host, retrofit);
            return retrofit;
        }
    }
}
