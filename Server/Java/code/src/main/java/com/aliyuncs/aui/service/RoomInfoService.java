package com.aliyuncs.aui.service;

import com.aliyuncs.aui.common.utils.PageUtils;
import com.aliyuncs.aui.dto.InvokeResult;
import com.aliyuncs.aui.dto.MeetingMemberInfo;
import com.aliyuncs.aui.dto.req.*;
import com.aliyuncs.aui.dto.res.NewImTokenResponseDto;
import com.aliyuncs.aui.dto.res.RoomInfoDto;
import com.aliyuncs.aui.dto.res.RtcAuthTokenResponse;
import com.aliyuncs.aui.entity.RoomInfoEntity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 房间服务
 *
 * @author chunlei.zcl
 */
public interface RoomInfoService extends IService<RoomInfoEntity> {

    /**
     * 获取新IM的token
     *
     * @author chunlei.zcl
     */
    NewImTokenResponseDto getNewImToken(ImTokenRequestDto imTokenRequestDto);


    /**
     * 创建房间
     *
     * @author chunlei.zcl
     */
    RoomInfoDto createRoomInfo(RoomCreateRequestDto roomCreateRequestDto);

    /**
     * 获取房间详情
     *
     * @author chunlei.zcl
     */
    RoomInfoDto get(RoomGetRequestDto roomGetRequestDto);

    /**
     * 批量获取房间详情
     *
     * @author chunlei.zcl
     */
    PageUtils list(RoomListRequestDto roomListRequestDto);

    /**
     * 关闭房间（直播间）
     *
     * @author chunlei.zcl
     */
    InvokeResult dismiss(RoomUpdateStatusRequestDto roomUpdateStatusRequestDto);

    /**
     * 修改房间（直播间）
     *
     * @author chunlei.zcl
     */
    RoomInfoDto update(RoomUpdateRequestDto roomUpdateRequestDto);

    /**
     * 获取连麦信息
     *
     * @author chunlei.zcl
     */
    MeetingMemberInfo.Members getMeetingInfo(MeetingGetRequestDto meetingGetRequestDto);

    RtcAuthTokenResponse getRtcAuthToken(RtcAuthTokenRequestDto rtcAuthTokenRequestDto);

    /**
     * 上麦
     *
     * @author chunlei.zcl
     */
    MeetingMemberInfo.Members joinMic(JoinMicRequestDto joinMicRequestDto);

    /**
     * 下麦
     *
     * @author chunlei.zcl
     */
    MeetingMemberInfo.Members leaveMic(LeaveMicRequestDto leaveMicRequestDto);
}

