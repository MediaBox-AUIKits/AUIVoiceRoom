package com.aliyun.auikits.voicechat.base.feed;

import java.util.Map;

public interface IUiEventHandler {
    void onUIEvent(int eventId, Map<String,Object> param, Map<String,Object> result);
}
