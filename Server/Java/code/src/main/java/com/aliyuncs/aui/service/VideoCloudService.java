package com.aliyuncs.aui.service;

import com.aliyuncs.aui.dto.req.ImTokenRequestDto;
import com.aliyuncs.aui.dto.res.NewImTokenResponseDto;
import com.aliyuncs.aui.dto.res.RoomInfoDto;

/**
 * 视频云服务
 *
 * @author chunlei.zcl
 */
public interface VideoCloudService {

    /**
     * 获取新Im的Token。
     * @author chunlei.zcl
     */
    NewImTokenResponseDto getNewImToken(ImTokenRequestDto imTokenRequestDto);

    /**
     * 创建新im的消息组。
     * @author chunlei.zcl
     */
    String createNewImMessageGroup(String groupId, String creatorId);

    /**
     * 查询频道内在线人数。接口地址：https://help.aliyun.com/zh/live/developer-reference/api-describechannelparticipants
     * @author chunlei.zcl
     */
    RoomInfoDto.Metrics getChannelDetails(String channelId);


    /**
     * 给N对N连麦做测试
     * @param channelId
     * @param userId
     * @param timestamp
     * @return
     */
    String getSpecialRtcAuth(String channelId, String userId, long timestamp);
}
