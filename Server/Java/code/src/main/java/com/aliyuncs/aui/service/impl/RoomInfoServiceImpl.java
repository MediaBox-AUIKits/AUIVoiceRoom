package com.aliyuncs.aui.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.aui.common.utils.PageUtils;
import com.aliyuncs.aui.dao.RoomInfoDao;
import com.aliyuncs.aui.dto.InvokeResult;
import com.aliyuncs.aui.dto.MeetingMemberInfo;
import com.aliyuncs.aui.dto.enums.ChatroomStatus;
import com.aliyuncs.aui.dto.req.*;
import com.aliyuncs.aui.dto.res.NewImTokenResponseDto;
import com.aliyuncs.aui.dto.res.RoomInfoDto;
import com.aliyuncs.aui.dto.res.RtcAuthTokenResponse;
import com.aliyuncs.aui.entity.RoomInfoEntity;
import com.aliyuncs.aui.service.RoomInfoService;
import com.aliyuncs.aui.service.VideoCloudService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;

/**
 * 房间服务实现类
 *
 * @author chunlei.zcl
 */
@Service("roomInfosService")
@Slf4j
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoDao, RoomInfoEntity> implements RoomInfoService {

    private static final int MAX_RETRY_TIMES = 20;

    /**
     * token秘钥
     */
    private static final String TOKEN_SECRET = "323assa2323.dqe223b434";

    @Value("${biz.live_mic.app_id}")
    private String liveMicAppId;

    private static ThreadPoolExecutor THREAD_POOL = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<>());

    @Resource
    private VideoCloudService videoCloudService;

    @Override
    public NewImTokenResponseDto getNewImToken(ImTokenRequestDto imTokenRequestDto) {

        return videoCloudService.getNewImToken(imTokenRequestDto);
    }

    @Override
    public RoomInfoDto createRoomInfo(RoomCreateRequestDto roomCreateRequestDto) {

        long start = System.currentTimeMillis();

        String groupId = UUID.randomUUID().toString().replaceAll("-", "");
        groupId = videoCloudService.createNewImMessageGroup(groupId, roomCreateRequestDto.getAnchor());

        if (StringUtils.isEmpty(groupId)) {
            log.error("createMessageGroup error. author:{}", roomCreateRequestDto.getAnchor());
            return null;
        }

        Date now = new Date();
        RoomInfoEntity roomInfoEntity = RoomInfoEntity.builder()
                .id(groupId)
                .createdAt(now)
                .updatedAt(now)
                .startedAt(now)
                .title(roomCreateRequestDto.getTitle())
                .notice(roomCreateRequestDto.getNotice())
                .status(ChatroomStatus.StatusOn.getVal())
                .anchorId(roomCreateRequestDto.getAnchor())
                .extendsInfo(roomCreateRequestDto.getExtendsInfo())
                .chatId(groupId)
                .anchorNick(roomCreateRequestDto.getAnchorNick())
                .meetingInfo(JSONObject.toJSONString(MeetingMemberInfo.init()))
                .showCode(getAvailableRoomNumber())
                .build();

        // insert db
        boolean saved = this.save(roomInfoEntity);
        if (!saved) {
            log.error("save db error. roomInfoEntity:{}", JSONObject.toJSONString(roomInfoEntity));
            return null;
        }

        RoomInfoDto roomInfoDto = new RoomInfoDto();
        BeanUtils.copyProperties(roomInfoEntity, roomInfoDto);

        log.info("createRoomInfo. roomCreateRequestDto:{}, roomInfoDto:{}, consume:{}", JSONObject.toJSONString(roomCreateRequestDto),
                JSONObject.toJSONString(roomInfoDto), (System.currentTimeMillis() - start));
        return roomInfoDto;
    }

    @Override
    public RoomInfoDto get(RoomGetRequestDto roomGetRequestDto) {

        RoomInfoEntity roomInfoEntity = this.getById(roomGetRequestDto.getId());
        if (roomInfoEntity == null) {
            log.warn("get roomInfoEntity is null. roomGetRequestDto:{}", JSONObject.toJSONString(roomGetRequestDto));
            return null;
        }

        RoomInfoDto roomInfoDto = new RoomInfoDto();
        BeanUtils.copyProperties(roomInfoEntity, roomInfoDto);

        RoomInfoDto.Metrics metrics = videoCloudService.getChannelDetails(roomInfoEntity.getId());

        if (metrics == null) {
            metrics = RoomInfoDto.Metrics.builder().onlineCount(0L).build();
        }
        roomInfoDto.setMetrics(metrics);

        return roomInfoDto;
    }


    @Override
    public PageUtils list(RoomListRequestDto roomListRequestDto) {

        Page<RoomInfoEntity> page = new Page<>(roomListRequestDto.getPageNum(), roomListRequestDto.getPageSize());
        QueryWrapper<RoomInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(RoomInfoEntity::getStatus, ChatroomStatus.StatusOn.getVal())
                .orderByDesc(RoomInfoEntity::getCreatedAt);

        Page<RoomInfoEntity> roomInfoEntityPage = this.page(page, queryWrapper);
        if (roomInfoEntityPage == null || CollectionUtils.isEmpty(roomInfoEntityPage.getRecords())) {
            log.warn("list. roomInfoEntityPage or roomInfoEntityPage.getRecords is empty");
            return null;
        }

        List<Future<RoomInfoDto>> futureList = new ArrayList<>(roomInfoEntityPage.getRecords().size());
        CountDownLatch countDownLatch = new CountDownLatch(roomInfoEntityPage.getRecords().size());

        for (RoomInfoEntity record : roomInfoEntityPage.getRecords()) {
            RoomGetRequestDto roomGetRequestDto = new RoomGetRequestDto();
            roomGetRequestDto.setId(record.getId());
            roomGetRequestDto.setUserId(record.getAnchorId());
            Future<RoomInfoDto> future = THREAD_POOL.submit(() -> getRoomInfo(roomGetRequestDto, countDownLatch));
            futureList.add(future);
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error(String.format("list InterruptedException. roomListRequestDto:%s", JSONObject.toJSONString(roomListRequestDto)), e);
            return null;
        }

        List<RoomInfoDto> roomInfoDtos = new ArrayList<>(futureList.size());
        for (Future<RoomInfoDto> roomInfoDtoFuture : futureList) {
            try {
                roomInfoDtos.add(roomInfoDtoFuture.get());
            } catch (Exception e) {
                log.error(String.format("roomInfoDtoFuture.get() Exception. roomListRequestDto:%s", JSONObject.toJSONString(roomListRequestDto)), e);
            }
        }

        return new PageUtils(roomInfoDtos, (int) roomInfoEntityPage.getTotal(), (int) roomInfoEntityPage.getSize(), (int) roomInfoEntityPage.getCurrent());
    }

    @Override
    public InvokeResult dismiss(RoomUpdateStatusRequestDto roomUpdateStatusRequestDto) {

        boolean valid = verifyPermission(roomUpdateStatusRequestDto.getId(), roomUpdateStatusRequestDto.getUserId());
        if (!valid) {
            log.warn("id:{} dismiss need owner, userId:{}", roomUpdateStatusRequestDto.getId(), roomUpdateStatusRequestDto.getUserId());
            return InvokeResult.builder().success(false).reason("NoPermit").build();
        }

        boolean result = updateStatus(roomUpdateStatusRequestDto.getId(), ChatroomStatus.StatusOff);
        if (result) {
            return InvokeResult.builder().success(true).build();
        }
        return InvokeResult.builder().success(false).reason("Unknown").build();
    }

    @Override
    public RoomInfoDto update(RoomUpdateRequestDto roomUpdateRequestDto) {

        RoomInfoEntity roomInfoEntity = new RoomInfoEntity();
        roomInfoEntity.setId(roomUpdateRequestDto.getId());
        if (StringUtils.isNotEmpty(roomUpdateRequestDto.getTitle())) {
            roomInfoEntity.setTitle(roomUpdateRequestDto.getTitle());
        }
        if (StringUtils.isNotEmpty(roomUpdateRequestDto.getNotice())) {
            roomInfoEntity.setNotice(roomUpdateRequestDto.getNotice());
        }
        if (StringUtils.isNotEmpty(roomUpdateRequestDto.getExtendsInfo())) {
            roomInfoEntity.setExtendsInfo(roomUpdateRequestDto.getExtendsInfo());
        }
        roomInfoEntity.setUpdatedAt(new Date());
        if (this.updateById(roomInfoEntity)) {
            RoomInfoEntity re = this.getById(roomUpdateRequestDto.getId());
            if (re != null) {
                RoomGetRequestDto roomGetRequestDto = new RoomGetRequestDto();
                roomGetRequestDto.setId(re.getId());
                roomGetRequestDto.setUserId(re.getAnchorId());
                return get(roomGetRequestDto);
            }
        }
        return null;
    }

    @Override
    public MeetingMemberInfo.Members joinMic(JoinMicRequestDto joinMicRequestDto) {

        RoomInfoEntity roomInfoEntity = this.getById(joinMicRequestDto.getId());
        if (roomInfoEntity == null) {
            log.warn("RoomInfoEntity Not Found. roomId:{}", joinMicRequestDto.getId());
            throw new RuntimeException("NotFound");
        }

        if (roomInfoEntity.getStatus() == ChatroomStatus.StatusOff.getVal()) {
            log.warn("RoomInfoEntity roomId:{} down.", joinMicRequestDto.getId());
            throw new RuntimeException("NotFound");
        }

        MeetingMemberInfo.Members meetingMemberInfoMembers = JSONObject.parseObject(roomInfoEntity.getMeetingInfo(), MeetingMemberInfo.Members.class);
        for (MeetingMemberInfo member : meetingMemberInfoMembers.getMembers()) {
            if (member.getJoined() && member.getUserId().equals(joinMicRequestDto.getUserId())) {
                log.warn("AlreadyJoined. member:{}", JSONObject.toJSONString(member));
                throw new RuntimeException("AlreadyJoined");
            }
        }

        if (joinMicRequestDto.getIndex() != null) {
            for (MeetingMemberInfo member : meetingMemberInfoMembers.getMembers()) {
                if (member.getIndex().intValue() == joinMicRequestDto.getIndex().intValue()) {
                    if (!member.getJoined()) {
                        member.setJoined(true);
                        member.setJoinTime(System.currentTimeMillis());
                        member.setUserId(joinMicRequestDto.getUserId());
                        member.setExtendsInfo(joinMicRequestDto.getExtendsInfo() != null ? joinMicRequestDto.getExtendsInfo() : "");

                        RoomInfoEntity re = new RoomInfoEntity();
                        re.setId(joinMicRequestDto.getId());
                        re.setMeetingInfo(JSONObject.toJSONString(meetingMemberInfoMembers));
                        this.updateById(re);
                        return getMeetingInfo(MeetingGetRequestDto.builder().id(joinMicRequestDto.getId()).build());
                    } else {
                        if (member.getUserId().equals(joinMicRequestDto.getUserId())) {
                            log.info("already joined. userId:{}", joinMicRequestDto.getUserId());
                            return getMeetingInfo(MeetingGetRequestDto.builder().id(joinMicRequestDto.getId()).build());
                        }
                        log.warn("already joined. so select other mic index");
                    }
                }
            }
        }

        MeetingMemberInfo meetingMemberInfo = selectAvailableMeetingMemberInfo(meetingMemberInfoMembers);
        if (meetingMemberInfo == null) {
            log.warn("No available meetingMemberInfo.");
            throw new RuntimeException("NotAvailable");
        }
        meetingMemberInfo.setJoined(true);
        meetingMemberInfo.setJoinTime(System.currentTimeMillis());
        meetingMemberInfo.setUserId(joinMicRequestDto.getUserId());
        meetingMemberInfo.setExtendsInfo(joinMicRequestDto.getExtendsInfo() != null ? joinMicRequestDto.getExtendsInfo() : "");

        RoomInfoEntity re = new RoomInfoEntity();
        re.setId(joinMicRequestDto.getId());
        re.setMeetingInfo(JSONObject.toJSONString(meetingMemberInfoMembers));
        this.updateById(re);

        return getMeetingInfo(MeetingGetRequestDto.builder().id(joinMicRequestDto.getId()).build());
    }

    @Override
    public MeetingMemberInfo.Members leaveMic(LeaveMicRequestDto leaveMicRequestDto) {


        RoomInfoEntity roomInfoEntity = this.getById(leaveMicRequestDto.getId());
        if (roomInfoEntity == null) {
            log.warn("RoomInfoEntity Not Found. roomId:{}", leaveMicRequestDto.getId());
            return null;
        }

        if (roomInfoEntity.getStatus() == ChatroomStatus.StatusOff.getVal()) {
            log.warn("RoomInfoEntity roomId:{} down.", leaveMicRequestDto.getId());
            return null;
        }

        MeetingMemberInfo.Members meetingMemberInfoMembers = JSONObject.parseObject(roomInfoEntity.getMeetingInfo(), MeetingMemberInfo.Members.class);
        for (MeetingMemberInfo member : meetingMemberInfoMembers.getMembers()) {
            if (member.getIndex().intValue() == leaveMicRequestDto.getIndex().intValue()) {
                if (!member.getJoined()) {
                    log.warn("already leave. ");
                    break;
                }
                member.setJoined(false);
                member.setUserId("");
                member.setJoinTime(null);
                member.setExtendsInfo("");
            }
        }

        RoomInfoEntity re = new RoomInfoEntity();
        re.setId(leaveMicRequestDto.getId());
        re.setMeetingInfo(JSONObject.toJSONString(meetingMemberInfoMembers));
        this.updateById(re);

        return getMeetingInfo(MeetingGetRequestDto.builder().id(leaveMicRequestDto.getId()).build());
    }


    @Override
    public MeetingMemberInfo.Members getMeetingInfo(MeetingGetRequestDto meetingGetRequestDto) {

        RoomInfoEntity roomInfoEntity = this.getById(meetingGetRequestDto.getId());
        if (roomInfoEntity == null) {
            log.warn("RoomInfoEntity Not Found. roomId:{}", meetingGetRequestDto.getId());
            return null;
        }

        if (roomInfoEntity.getStatus() == ChatroomStatus.StatusOff.getVal()) {
            log.warn("RoomInfoEntity roomId:{} down.", meetingGetRequestDto.getId());
            return null;
        }

        return JSONObject.parseObject(roomInfoEntity.getMeetingInfo(), MeetingMemberInfo.Members.class);
    }

    @Override
    public RtcAuthTokenResponse getRtcAuthToken(RtcAuthTokenRequestDto rtcAuthTokenRequestDto) {

        // 24小时有效
        long timestamp = DateUtils.addDays(new Date(), 1).getTime() / 1000;
        String token = videoCloudService.getSpecialRtcAuth(rtcAuthTokenRequestDto.getRoomId(), rtcAuthTokenRequestDto.getUserId(), timestamp);
        return RtcAuthTokenResponse.builder().authToken(token).timestamp(timestamp).build();
    }

    private MeetingMemberInfo selectAvailableMeetingMemberInfo(MeetingMemberInfo.Members meetingMemberInfoMembers) {

        for (MeetingMemberInfo member : meetingMemberInfoMembers.getMembers()) {
            if (!member.getJoined()) {
                return member;
            }
        }
        return null;
    }

    private boolean verifyPermission(String roomId, String reqUid) {

        RoomInfoEntity roomInfoEntity = this.getById(roomId);
        if (roomInfoEntity == null) {
            log.warn("RoomInfoEntity Not Found. roomId:{}", roomId);
            return true;
        }

        if (!roomInfoEntity.getAnchorId().equals(reqUid)) {
            log.warn("Insufficient permission. roomId:{}, anthor:{}, reqUid:{}", roomId,
                    roomInfoEntity.getAnchorId(), reqUid);
            return false;
        }
        return true;
    }

    private boolean updateStatus(String id, ChatroomStatus liveStatus) {

        RoomInfoEntity roomInfoEntity = new RoomInfoEntity();
        roomInfoEntity.setId(id);
        roomInfoEntity.setStatus(liveStatus.getVal());

        switch (liveStatus) {
            case StatusOn:
                roomInfoEntity.setStartedAt(new Date());
                break;
            case StatusOff:
                roomInfoEntity.setStoppedAt(new Date());
        }
        roomInfoEntity.setUpdatedAt(new Date());
        return this.updateById(roomInfoEntity);
    }

    private RoomInfoDto getRoomInfo(RoomGetRequestDto roomGetRequestDto, CountDownLatch countDownLatch) {

        try {
            return get(roomGetRequestDto);
        } catch (Exception e) {
            log.error(String.format("getRoomInfo. roomGetRequestDto:{}", JSONObject.toJSONString(roomGetRequestDto), e));
        } finally {
            countDownLatch.countDown();
        }
        return null;
    }

    private int getAvailableRoomNumber() {

        for (int i = 0; i < MAX_RETRY_TIMES; i++) {
            int showCode = generateRoomNumber();
            if (!isUsed(showCode)) {
                return showCode;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(new Random().nextInt(10));
            } catch (InterruptedException e) {
                log.warn("InterruptedException: {}", e.getMessage());
            }
        }
        throw new RuntimeException("NoAvailableShowCode");
    }

    private boolean isUsed(int showCode) {

        QueryWrapper<RoomInfoEntity> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<RoomInfoEntity> eq = queryWrapper.lambda().eq(RoomInfoEntity::getShowCode, showCode);
        return this.count(eq) > 0;
    }

    /**
    * 全数字、最多8个字位，随机生成
    * @author chunlei.zcl
    */
    public static int generateRoomNumber() {

        Random random = new Random();
        StringBuilder roomNumber = new StringBuilder();

        // 保证至少一位，最多八位
        ////for (int i = 0; i < random.nextInt(8) + 1; i++) {
        //    // 随机生成0-9之间的整数
        //    roomNumber.append(random.nextInt(10));
        //}

        return random.nextInt(99999999);
    }

}