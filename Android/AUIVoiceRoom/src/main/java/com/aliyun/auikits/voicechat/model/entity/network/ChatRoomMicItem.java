package com.aliyun.auikits.voicechat.model.entity.network;


import android.text.TextUtils;

import com.aliyun.auikits.voicechat.util.GsonHolder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;


public class ChatRoomMicItem {
    public static final String KEY_USER_NICK = "user_nick";
    public static final String KEY_USER_AVATAR = "user_avatar";

    public String user_id;
    public int index;
    @SerializedName("extends")
    public String exd;
    public boolean joined;
    public long join_time;

    public String getAvatar() {
        return getExtValue(KEY_USER_AVATAR);
    }

    public String getName() {
        return getExtValue(KEY_USER_NICK);
    }

    private String getExtValue(String key) {
        if(!TextUtils.isEmpty(exd)) {
            JsonObject extJsonObj = GsonHolder.gson.fromJson(exd, JsonObject.class);
            if(extJsonObj != null && extJsonObj.has(key)) {
                JsonElement jsonElement = extJsonObj.get(key);
                if(jsonElement != null) {
                    return jsonElement.getAsString();
                }
            }

        }

        return "";
    }

}
