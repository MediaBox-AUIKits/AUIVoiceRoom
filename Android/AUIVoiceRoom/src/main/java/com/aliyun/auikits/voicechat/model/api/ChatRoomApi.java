package com.aliyun.auikits.voicechat.model.api;

import com.aliyun.auikits.voicechat.model.entity.network.CloseRoomResponse;
import com.aliyun.auikits.voicechat.model.entity.network.LoginRequest;
import com.aliyun.auikits.voicechat.model.entity.network.LoginResponse;
import com.aliyun.auikits.voicechat.model.entity.network.ChatRoomListRequest;
import com.aliyun.auikits.voicechat.model.entity.network.ChatRoomListResponse;
import com.aliyun.auikits.voicechat.model.entity.network.ChatRoomRequest;
import com.aliyun.auikits.voicechat.model.entity.network.ChatRoomResponse;
import com.aliyun.auikits.voicechat.model.entity.network.CloseRoomRequest;
import com.aliyun.auikits.voicechat.model.entity.network.CreateRoomRequest;
import com.aliyun.auikits.voicechat.model.entity.network.CreateRoomResponse;
import com.aliyun.auikits.voicechat.model.entity.network.ImTokenRequest;
import com.aliyun.auikits.voicechat.model.entity.network.ImTokenResponse;
import com.aliyun.auikits.voicechat.model.entity.network.RtcTokenRequest;
import com.aliyun.auikits.voicechat.model.entity.network.RtcTokenResponse;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ChatRoomApi {
    public static final String APP_ID = "xxx";
    public static final String HOST = "xxx";
    public static final String RTC_GLSB = "https://gw.rtn.aliyuncs.com";

    @POST("/login")
    Observable<LoginResponse> login(
            @Body
            LoginRequest request
    );

    @POST("/api/chatroom/token")
    Observable<ImTokenResponse> getImToken(
            @Header("Authorization")
            String authorization,
            @Body
            ImTokenRequest request
    );

    @POST("/api/chatroom/getRtcAuthToken")
    Observable<RtcTokenResponse> getRtcToken(
            @Header("Authorization")
            String authorization,
            @Body
            RtcTokenRequest request
    );

    @POST("/api/chatroom/list")
    Observable<ChatRoomListResponse> fetchRoomList(
            @Header("Authorization")
            String authorization,
            @Body ChatRoomListRequest request);

    @POST("/api/chatroom/get")
    Observable<ChatRoomResponse> getRoomInfo(
            @Header("Authorization") String authorization,
            @Body ChatRoomRequest request
    );

    @POST("/api/chatroom/dismiss")
    Observable<CloseRoomResponse> dismissRoom(@Header("Authorization") String authorization, @Body CloseRoomRequest request);

    @POST("/api/chatroom/create")
    Observable<CreateRoomResponse> createRoom(@Header("Authorization") String authorization, @Body CreateRoomRequest request);

}
