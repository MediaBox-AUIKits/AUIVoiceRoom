package com.aliyuncs.aui.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.aui.dto.req.ImTokenRequestDto;
import com.aliyuncs.aui.dto.res.NewImTokenResponseDto;
import com.aliyuncs.aui.dto.res.RoomInfoDto;
import com.aliyuncs.aui.service.VideoCloudService;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.live.model.v20161101.*;
import com.aliyuncs.profile.DefaultProfile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * 视频云服务实现类
 *
 * @author chunlei.zcl
 */
@Service
@Slf4j
public class VideoCloudServiceImpl implements VideoCloudService {

    private static final String LIVE_DOMAIN = "live.aliyun.com";

    private static final String LIVE_OPEN_API_DOMAIN = "live.aliyuncs.com";

    @Value("${biz.openapi.access.key}")
    private String accessKeyId;
    @Value("${biz.openapi.access.secret}")
    private String accessKeySecret;

    @Value("${biz.live_mic.app_id}")
    private String liveMicAppId;
    @Value("${biz.live_mic.app_key}")
    private String liveMicAppKey;

    @Value("${biz.new_im.appId}")
    private String appId;

    @Value("${biz.new_im.appKey}")
    private String appKey;

    @Value("${biz.new_im.appSign}")
    private String appSign;

    private IAcsClient client;

    @PostConstruct
    public void init() {

        DefaultProfile profile = DefaultProfile.getProfile("cn-shanghai", accessKeyId, accessKeySecret);
        client = new DefaultAcsClient(profile);
    }

    @Override
    public NewImTokenResponseDto getNewImToken(ImTokenRequestDto imTokenRequestDto) {

        String role = imTokenRequestDto.getRole();
        if (role == null) {
            role = "";
        }
        String nonce = UUID.randomUUID().toString();
        long timestamp = DateUtils.addDays(new Date(), 2).getTime() / 1000;
        String signContent = String.format("%s%s%s%s%s%s", appId, appKey, imTokenRequestDto.getUserId(), nonce, timestamp, role);
        String appToken = org.apache.commons.codec.digest.DigestUtils.sha256Hex(signContent);

        NewImTokenResponseDto newImTokenResponseDto = NewImTokenResponseDto.builder()
                .appId(appId)
                .appSign(appSign)
                .appToken(appToken)
                .auth(NewImTokenResponseDto.Auth.builder()
                        .userId(imTokenRequestDto.getUserId())
                        .nonce(nonce)
                        .timestamp(timestamp)
                        .role(role)
                        .build())
                .build();

        log.info("getNewImToken. userId:{}, newImTokenResponseDto:{}", imTokenRequestDto.getUserId(), JSONObject.toJSONString(newImTokenResponseDto));
        return newImTokenResponseDto;
    }

    public String createNewImMessageGroup(String groupId, String creatorId) {

        long start = System.currentTimeMillis();
        CreateLiveMessageGroupRequest request = new CreateLiveMessageGroupRequest();
        request.setAppId(appId);
        request.setGroupId(groupId);
        request.setCreatorId(creatorId);

        log.info("createNewImMessageGroup, request:{}", JSONObject.toJSONString(request));

        try {
            CreateLiveMessageGroupResponse acsResponse = client.getAcsResponse(request);
            log.info("createNewImMessageGroup, response:{}, consume:{}", JSONObject.toJSONString(acsResponse), (System.currentTimeMillis() - start));
            return acsResponse.getGroupId();
        } catch (ServerException e) {
            log.error("createNewImMessageGroup ServerException. ErrCode:{}, ErrMsg:{}, RequestId:{}", e.getErrCode(), e.getErrMsg(), e.getRequestId());
        } catch (ClientException e) {
            log.error("createNewImMessageGroup ClientException. ErrCode:{}, ErrMsg:{}, RequestId:{}", e.getErrCode(), e.getErrMsg(), e.getRequestId());
        } catch (Exception e) {
            log.error("createNewImMessageGroup Exception. error:{}", e.getMessage());
        }
        return null;
    }

    @Override
    public RoomInfoDto.Metrics getChannelDetails(String channelId) {

        long start = System.currentTimeMillis();

        DescribeChannelParticipantsRequest request = new DescribeChannelParticipantsRequest();
        request.setAppId(liveMicAppId);
        request.setChannelId(channelId);

        try {
            DescribeChannelParticipantsResponse acsResponse = client.getAcsResponse(request);
            log.info("getChannelDetails, response:{}, consume:{}", JSONObject.toJSONString(acsResponse), (System.currentTimeMillis() - start));
            Integer totalNum = acsResponse.getTotalNum();

            return RoomInfoDto.Metrics.builder().onlineCount(totalNum != null ? totalNum.longValue() : 0).build();
        } catch (ServerException e) {
            log.error("getChannelDetails ServerException. ErrCode:{}, ErrMsg:{}, RequestId:{}", e.getErrCode(), e.getErrMsg(), e.getRequestId());
        } catch (ClientException e) {
            log.error("getChannelDetails ClientException. ErrCode:{}, ErrMsg:{}, RequestId:{}", e.getErrCode(), e.getErrMsg(), e.getRequestId());
        } catch (Exception e) {
            log.error("getChannelDetails Exception. error:{}", e.getMessage());
        }

        return null;
    }

    public String getSpecialRtcAuth(String channelId, String userId, long timestamp) {

        String rtcAuthStr = String.format("%s%s%s%s%d", liveMicAppId, liveMicAppKey, channelId, userId, timestamp);
        String rtcAuth = getSHA256(rtcAuthStr);
        log.info("getRtcAuth. rtcAuthStr:{}, rtcAuth:{}", rtcAuthStr, rtcAuth);
        return rtcAuth;
    }



    private static String getSHA256(String str) {
        MessageDigest messageDigest;
        String encodestr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes("UTF-8"));
            encodestr = byte2Hex(messageDigest.digest());
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodestr;
    }

    private static String byte2Hex(byte[] bytes) {
        StringBuilder stringBuffer = new StringBuilder();
        String temp = null;
        for (byte aByte : bytes) {
            temp = Integer.toHexString(aByte & 0xFF);
            if (temp.length() == 1) {
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }

}
