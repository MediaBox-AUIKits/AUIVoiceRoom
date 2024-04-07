package com.aliyuncs.aui.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.aui.common.utils.PageUtils;
import com.aliyuncs.aui.common.utils.Result;
import com.aliyuncs.aui.common.utils.ValidatorUtils;
import com.aliyuncs.aui.dto.InvokeResult;
import com.aliyuncs.aui.dto.MeetingMemberInfo;
import com.aliyuncs.aui.dto.req.*;
import com.aliyuncs.aui.dto.res.NewImTokenResponseDto;
import com.aliyuncs.aui.dto.res.RoomInfoDto;
import com.aliyuncs.aui.dto.res.RtcAuthTokenResponse;
import com.aliyuncs.aui.service.RoomInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 聊天室管理的Controller
 *
 * @author chunlei.zcl
 */
@RestController
@RequestMapping("/api/chatroom")
@Slf4j
public class RoomInfoController {

    @Resource
    private HttpServletRequest request;

    @Resource
    private RoomInfoService roomInfoService;

    /**
     * 获取Im的token
     */
    @RequestMapping("/token")
    public Result getImToken(@RequestBody ImTokenRequestDto imTokenRequestDto) {

        ValidatorUtils.validateEntity(imTokenRequestDto);

        Map<String, Object> result = new HashMap<>();

        NewImTokenResponseDto newImTokenResponseDto = roomInfoService.getNewImToken(imTokenRequestDto);
        if (newImTokenResponseDto != null) {
            result.put("aliyun_im", newImTokenResponseDto);
        }
        return Result.ok(result);
    }

    @RequestMapping("/create")
    public Result createRoomInfo(@RequestBody RoomCreateRequestDto roomCreateRequestDto) {

        ValidatorUtils.validateEntity(roomCreateRequestDto);

        RoomInfoDto roomInfo = roomInfoService.createRoomInfo(roomCreateRequestDto);
        if (roomInfo != null) {
            String jsonStr = JSONObject.toJSONString(roomInfo);
            Map<String, Object> map = JSON.parseObject(jsonStr, Map.class);
            Result result = Result.ok();
            result.putAll(map);
            return result;
        }

        return Result.error();
    }

    /**
     * 信息
     */
    @RequestMapping("/get")
    public Result get(@RequestBody RoomGetRequestDto roomGetRequestDto) {

        ValidatorUtils.validateEntity(roomGetRequestDto);

        RoomInfoDto roomInfo = roomInfoService.get(roomGetRequestDto);
        if (roomInfo != null) {
            String jsonStr = JSONObject.toJSONString(roomInfo);
            Map<String, Object> map = JSON.parseObject(jsonStr, Map.class);
            Result result = Result.ok();
            result.putAll(map);
            return result;
        }
        return Result.notFound();
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public Result list(@RequestBody RoomListRequestDto roomListRequestDto) {

        ValidatorUtils.validateEntity(roomListRequestDto);
        Map<String, Object> map = new HashMap<>();
        PageUtils page = roomInfoService.list(roomListRequestDto);
        if (page != null && CollectionUtils.isNotEmpty(page.getList())) {
            map.put("rooms", page.getList());
        }
        return Result.ok(map);
    }

    @RequestMapping("/dismiss")
    public Result dismiss(@RequestBody RoomUpdateStatusRequestDto roomUpdateStatusRequestDto) {

        ValidatorUtils.validateEntity(roomUpdateStatusRequestDto);
        InvokeResult result = roomInfoService.dismiss(roomUpdateStatusRequestDto);
        Map<String, Object> map = new HashMap<>();
        if (result.isSuccess()) {
            map.put("success", true);
        } else {
            map.put("success", false);
            map.put("reason", result.getReason());
        }
        return Result.ok(map);
    }


    @RequestMapping("/joinMic")
    public Result joinMic(@RequestBody JoinMicRequestDto joinMicRequestDto) {

        ValidatorUtils.validateEntity(joinMicRequestDto);
        if (!joinMicRequestDto.valid()) {
            return Result.invalidParam();
        }

        try {
            MeetingMemberInfo.Members members = roomInfoService.joinMic(joinMicRequestDto);
            if (members != null) {
                String jsonStr = JSONObject.toJSONString(members);
                Map<String, Object> map = JSON.parseObject(jsonStr, Map.class);
                Result result = Result.ok();
                result.putAll(map);
                return result;
            }
            return Result.error();
        } catch (RuntimeException e) {
            Map<String, Object> map = new HashMap<>();
            if (e.getMessage().equals("NotAvailable")) {
                map.put("reason", 1);
                map.put("desc", "麦位已满");
            } else if (e.getMessage().equals("AlreadyJoined"))  {
                map.put("reason", 2);
                map.put("desc", "用户已经上麦");
            }
            return Result.ok(map);
        }
    }

    @RequestMapping("/leaveMic")
    public Result leaveMic(@RequestBody LeaveMicRequestDto leaveMicRequestDto) {

        ValidatorUtils.validateEntity(leaveMicRequestDto);
        if (!leaveMicRequestDto.valid()) {
            return Result.invalidParam();
        }

        MeetingMemberInfo.Members members = roomInfoService.leaveMic(leaveMicRequestDto);
        if (members != null) {
            String jsonStr = JSONObject.toJSONString(members);
            Map<String, Object> map = JSON.parseObject(jsonStr, Map.class);
            Result result = Result.ok();
            result.putAll(map);
            return result;
        }
        return Result.error();
    }




    @RequestMapping("/update")
    public Result update(@RequestBody RoomUpdateRequestDto roomUpdateRequestDto) {

        ValidatorUtils.validateEntity(roomUpdateRequestDto);
        RoomInfoDto roomInfo = roomInfoService.update(roomUpdateRequestDto);
        if (roomInfo != null) {
            String jsonStr = JSONObject.toJSONString(roomInfo);
            Map<String, Object> map = JSON.parseObject(jsonStr, Map.class);
            Result result = Result.ok();
            result.putAll(map);
            return result;
        }
        return Result.error();
    }

    @RequestMapping("/getMeetingInfo")
    public Result getMeetingInfo(@RequestBody MeetingGetRequestDto meetingGetRequestDto) {

        ValidatorUtils.validateEntity(meetingGetRequestDto);
        MeetingMemberInfo.Members members = roomInfoService.getMeetingInfo(meetingGetRequestDto);

        Result result = Result.ok();
        Map<String, Object> map;
        if (members != null) {
            String jsonStr = JSONObject.toJSONString(members);
            map = JSON.parseObject(jsonStr, Map.class);
        } else {
            map = new HashMap<>();
        }
        result.putAll(map);
        return result;
    }

    @RequestMapping("/getRtcAuthToken")
    public Result getRtcAuthToken(@RequestBody RtcAuthTokenRequestDto rtcAuthTokenRequestDto) {

        ValidatorUtils.validateEntity(rtcAuthTokenRequestDto);

        RtcAuthTokenResponse rtcAuthToken = roomInfoService.getRtcAuthToken(rtcAuthTokenRequestDto);
        if (rtcAuthToken != null) {
            String jsonStr = JSONObject.toJSONString(rtcAuthToken);
            Map<String, Object> map = JSON.parseObject(jsonStr, Map.class);
            Result result = Result.ok();
            result.putAll(map);
            return result;
        }

        return Result.error();
    }


}
