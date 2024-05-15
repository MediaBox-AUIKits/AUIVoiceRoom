package com.aliyun.auikits.rtc;

import android.text.TextUtils;

import com.aliyun.auikits.voiceroom.bean.UserInfo;

public class RtcChannel {
    public final String appId;
    public final String gslb;
    public final String token;
    public final long timestamp;
    public final String channelId;
    public final String userId;

    private RtcChannel(String appId, String gslb, String token, long timestamp, String channelId, String user){
        this.appId = appId;
        this.gslb = gslb;
        this.token = token;
        this.timestamp = timestamp;
        this.channelId = channelId;
        this.userId = user;
    }

    public static class Builder{
        private String appId;
        private String gslb;
        private String token;
        private Long timestamp;
        private String channelId;
        private String uid;

        public RtcChannel build(){
            if(TextUtils.isEmpty(appId)
                    || TextUtils.isEmpty(gslb)
                    || TextUtils.isEmpty(token)
                    || TextUtils.isEmpty(channelId)
                    || timestamp == null
                    || TextUtils.isEmpty(uid))
                throw new IllegalStateException("you should config all the parameters");
            return new RtcChannel(appId, gslb, token, timestamp, channelId, uid);
        }

        public Builder appId(String id){
            this.appId = id;
            return this;
        }

        public Builder gslb(String g){
            this.gslb = g;
            return this;
        }

        public Builder token(String t){
            this.token = t;
            return this;
        }

        public Builder timestamp(long t){
            this.timestamp = t;
            return this;
        }

        public Builder channel(String id){
            this.channelId = id;
            return this;
        }

        public Builder uid(String u){
            this.uid = u;
            return this;
        }
    }
}
