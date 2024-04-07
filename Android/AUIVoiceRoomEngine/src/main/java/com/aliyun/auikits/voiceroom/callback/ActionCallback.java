package com.aliyun.auikits.voiceroom.callback;

import java.util.Map;

public interface ActionCallback {
    void onResult(int code, String msg, Map<String, Object> params);
}
