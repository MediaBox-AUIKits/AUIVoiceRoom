package com.aliyun.auikits.voicechat.model.entity.network;

import java.util.ArrayList;
import java.util.List;

public class ImTokenRequest {
    public String user_id;
    public String device_id;
    public String device_type = "android";
    public List<String> im_server = new ArrayList<>();
    public String role;

    public ImTokenRequest(String user_id, String device_id, String role) {
        this.user_id = user_id;
        this.device_id = device_id;
        this.role = role;
        this.im_server.add("aliyun_new");
    }
}
