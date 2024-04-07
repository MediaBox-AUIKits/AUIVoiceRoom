package com.aliyun.auikits.voiceroom.module.seat.callback;

import org.json.JSONObject;

public interface SeatManagerCallback {

    //上麦响应结果
    void onResponseJoinSeat(JSONObject rs);

    //下麦响应结果
    void onResponseLeaveSeat(JSONObject rs);

    //麦位查询响应结果
    void onResponseQuerySeatList(JSONObject rs);
}
