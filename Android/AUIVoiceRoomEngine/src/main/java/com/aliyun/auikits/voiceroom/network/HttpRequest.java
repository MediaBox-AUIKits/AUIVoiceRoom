package com.aliyun.auikits.voiceroom.network;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class HttpRequest {
    private static HttpRequest instance;
    private OkHttpClient mOkHttpClient = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).build();
    private static Object mObj = new Object();

    public static HttpRequest getInstance() {
        if (HttpRequest.instance == null) {
            synchronized (mObj) {
                if (HttpRequest.instance == null) {
                    HttpRequest.instance = new HttpRequest();
                }
            }
        }
        return HttpRequest.instance;
    }

    public final OkHttpClient getClient() {
        return this.mOkHttpClient;
    }

    public void post(String postUrl, Map<String, String> headers, JSONObject jsonObject, Callback callback){
        Request.Builder url = new Request.Builder().url(postUrl);
        for(String key : headers.keySet()){
            url = url.header(key, headers.get(key));
        }
        RequestBody.Companion companion = RequestBody.Companion;
        MediaType parse = MediaType.Companion.parse("application/json");
        Request request = url.post(companion.create(parse, jsonObject.toString())).build();
        HttpRequest companion2 = HttpRequest.getInstance();
        companion2.getClient().newCall(request).enqueue(callback);
    }
}