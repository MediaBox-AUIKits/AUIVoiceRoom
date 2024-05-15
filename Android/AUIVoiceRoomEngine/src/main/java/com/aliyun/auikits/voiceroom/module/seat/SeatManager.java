package com.aliyun.auikits.voiceroom.module.seat;

import com.aliyun.auikits.voiceroom.bean.RoomInfo;
import com.aliyun.auikits.voiceroom.callback.ActionCallback;

import java.util.List;

public interface SeatManager {

    //上麦
    void joinSeat(SeatInfo seat, ActionCallback callback);

    //下麦
    void leaveSeat(SeatInfo seat, ActionCallback callback);

    //查询麦位列表
    void getSeatList(String roomId, ActionCallback callback);
}
