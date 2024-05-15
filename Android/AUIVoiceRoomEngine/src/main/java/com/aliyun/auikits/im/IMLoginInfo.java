package com.aliyun.auikits.im;

import android.text.TextUtils;

public class IMLoginInfo {
    public String device_id;
    public final String app_id;
    public final String app_token;
    public final String app_sign;
    public String auth_user_id;
    public final String auth_once;
    public final long auth_timestamp;
    public final String auth_role;
    public String nick_name;
    public String avatar;

    private IMLoginInfo(String aid, String atoken, String asign,
                        String aonce, long atimestamp, String arole){
        this.app_id = aid;
        this.app_token = atoken;
        this.app_sign = asign;
        this.auth_once = aonce;
        this.auth_timestamp = atimestamp;
        this.auth_role = arole;
    }

    public static class Builder{
        private String device_id;
        private String app_id;
        private String app_token;
        private String app_sign;
        private String auth_user_id;
        private String auth_once;
        private Long auth_timestamp;
        private String auth_role;
        private String nick_name;
        private String avatar;

        public Builder appId(String appId){
            this.app_id = appId;
            return this;
        }

        public Builder appToken(String token){
            this.app_token = token;
            return this;
        }

        public Builder appSign(String appSign){
            this.app_sign = appSign;
            return this;
        }

        public Builder userId(String userId){
            this.auth_user_id = userId;
            return this;
        }

        public Builder once(String once){
            this.auth_once = once;
            return this;
        }

        public Builder timestamp(long t){
            this.auth_timestamp = t;
            return this;
        }

        public Builder role(String r){
            this.auth_role = r;
            return this;
        }

        public Builder nickName(String nickName){
            this.nick_name = nickName;
            return this;
        }

        public Builder avatar(String avatar){
            this.avatar = avatar;
            return this;
        }

        public Builder deviceId(String id){
            this.device_id = id;
            return this;
        }

        public IMLoginInfo build(){
            if(TextUtils.isEmpty(app_id)
                    || TextUtils.isEmpty(app_token)
                    || TextUtils.isEmpty(app_sign)
                    || TextUtils.isEmpty(auth_once)
                    || TextUtils.isEmpty(auth_role)
                    || TextUtils.isEmpty(auth_user_id)
                    || auth_timestamp == null
                    || TextUtils.isEmpty(nick_name)
                    || TextUtils.isEmpty(avatar)
                    || TextUtils.isEmpty(device_id))
                throw new IllegalArgumentException("field not fill");
            IMLoginInfo loginInfo = new IMLoginInfo(app_id, app_token, app_sign,
                    auth_once, auth_timestamp, auth_role);
            loginInfo.auth_user_id = auth_user_id;
            loginInfo.device_id = device_id;
            loginInfo.nick_name = nick_name;
            loginInfo.avatar = avatar;
            loginInfo.device_id = device_id;
            return loginInfo;
        }
    }
}
