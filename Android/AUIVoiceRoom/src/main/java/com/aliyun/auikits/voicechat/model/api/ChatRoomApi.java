package com.aliyun.auikits.voicechat.model.api;

import com.aliyun.auikits.biz.voiceroom.VoiceRoomServerConstant;
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
    public static final String HOST = VoiceRoomServerConstant.HOST;

    @POST(VoiceRoomServerConstant.LOGIN_URL)
    Observable<LoginResponse> login(
            @Body
            LoginRequest request
    );

    @POST(VoiceRoomServerConstant.GET_IM_TOKEN_URL)
    Observable<ImTokenResponse> getImToken(
            @Header("Authorization")
            String authorization,
            @Body
            ImTokenRequest request
    );

    @POST(VoiceRoomServerConstant.GET_RTC_TOKEN_URL)
    Observable<RtcTokenResponse> getRtcToken(
            @Header("Authorization")
            String authorization,
            @Body
            RtcTokenRequest request
    );

    @POST(VoiceRoomServerConstant.GET_CHAT_ROOM_LIST_URL)
    Observable<ChatRoomListResponse> fetchRoomList(
            @Header("Authorization")
            String authorization,
            @Body ChatRoomListRequest request);

    @POST(VoiceRoomServerConstant.GET_CHAT_ROOM_INFO_URL)
    Observable<ChatRoomResponse> getRoomInfo(
            @Header("Authorization") String authorization,
            @Body ChatRoomRequest request
    );

    @POST(VoiceRoomServerConstant.DISMISS_CHAT_ROOM_URL)
    Observable<CloseRoomResponse> dismissRoom(@Header("Authorization") String authorization, @Body CloseRoomRequest request);

    @POST(VoiceRoomServerConstant.CREATE_CHAT_ROOM_URL)
    Observable<CreateRoomResponse> createRoom(@Header("Authorization") String authorization, @Body CreateRoomRequest request);

}
