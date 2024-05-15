package com.aliyun.auikits.im;

import android.text.TextUtils;

import com.alivc.auicommon.common.base.util.ThreadUtil;
import com.alivc.auimessage.AUIMessageConfig;
import com.alivc.auimessage.MessageService;
import com.alivc.auimessage.MessageServiceFactory;
import com.alivc.auimessage.listener.InteractionCallback;
import com.alivc.auimessage.listener.MessageListener;
import com.alivc.auimessage.model.base.AUIMessageModel;
import com.alivc.auimessage.model.base.AUIMessageUserInfo;
import com.alivc.auimessage.model.base.InteractionError;
import com.alivc.auimessage.model.lwp.CreateGroupRequest;
import com.alivc.auimessage.model.lwp.CreateGroupResponse;
import com.alivc.auimessage.model.lwp.GetGroupInfoRequest;
import com.alivc.auimessage.model.lwp.GetGroupInfoResponse;
import com.alivc.auimessage.model.lwp.JoinGroupRequest;
import com.alivc.auimessage.model.lwp.JoinGroupResponse;
import com.alivc.auimessage.model.lwp.LeaveGroupRequest;
import com.alivc.auimessage.model.lwp.LeaveGroupResponse;
import com.alivc.auimessage.model.lwp.SendMessageToGroupRequest;
import com.alivc.auimessage.model.lwp.SendMessageToGroupResponse;
import com.alivc.auimessage.model.lwp.SendMessageToGroupUserRequest;
import com.alivc.auimessage.model.lwp.SendMessageToGroupUserResponse;
import com.alivc.auimessage.model.message.ExitGroupMessage;
import com.alivc.auimessage.model.message.JoinGroupMessage;
import com.alivc.auimessage.model.message.LeaveGroupMessage;
import com.alivc.auimessage.model.message.MuteGroupMessage;
import com.alivc.auimessage.model.message.UnMuteGroupMessage;
import com.alivc.auimessage.model.token.IMNewToken;
import com.alivc.auimessage.model.token.IMNewTokenAuth;
import com.aliyun.auikits.common.AliyunLog;
import com.aliyun.auikits.common.util.CommonUtil;
import com.aliyun.auikits.voiceroom.callback.ActionCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IMService {
    private static final String TAG = "IMService";
    private MessageService mIMService;
    private static final List<MessageListener> IM_LISTENER_LIST = new ArrayList();
    private static final MessageReceiver MESSAGE_RECEIVER = new MessageReceiver();
    private IMLoginInfo imLoginInfo;
    private static final Map<String, MessageListener> IM_LISTENER_MAP = new LinkedHashMap();
    public static final String KEY_GROUP_ID = "group_id";
    private int mPendingMessageCount = 0; //待发送消息数量
    private Object mMessagePendingLock = new Object();
    private boolean mAllowPendingMessage = true;

    public static class MessageReceiver implements MessageListener {
        public void onJoinGroup(final AUIMessageModel<JoinGroupMessage> aUIMessageModel) {
            CommonUtil.runOnUI(new Runnable() {
                @Override
                public final void run() {
                    if (TextUtils.isEmpty(aUIMessageModel.groupId)) {
                        for (MessageListener listener : IM_LISTENER_LIST) {
                            listener.onJoinGroup(aUIMessageModel);
                        }
                    } else if (IM_LISTENER_MAP.containsKey(aUIMessageModel.groupId)) {
                        Object obj = IM_LISTENER_MAP.get(aUIMessageModel.groupId);

                        ((MessageListener) obj).onJoinGroup(aUIMessageModel);
                    }
                }
            });
        }

        public void onLeaveGroup(final AUIMessageModel<LeaveGroupMessage> aUIMessageModel) {
            CommonUtil.runOnUI(new Runnable() {
                @Override
                public final void run() {
                    if (TextUtils.isEmpty(aUIMessageModel.groupId)) {
                        for (MessageListener listener : IM_LISTENER_LIST) {
                            listener.onLeaveGroup(aUIMessageModel);
                        }
                    } else if (IM_LISTENER_MAP.containsKey(aUIMessageModel.groupId)) {
                        Object obj = IM_LISTENER_MAP.get(aUIMessageModel.groupId);

                        ((MessageListener) obj).onLeaveGroup(aUIMessageModel);
                    }
                }
            });
        }

        public void onMuteGroup(final AUIMessageModel<MuteGroupMessage> aUIMessageModel) {
            CommonUtil.runOnUI(new Runnable() {
                @Override
                public final void run() {
                    if (TextUtils.isEmpty(aUIMessageModel.groupId)) {
                        for (MessageListener listener : IM_LISTENER_LIST) {
                            listener.onMuteGroup(aUIMessageModel);
                        }
                    } else if (IM_LISTENER_MAP.containsKey(aUIMessageModel.groupId)) {
                        Object obj = IM_LISTENER_MAP.get(aUIMessageModel.groupId);

                        ((MessageListener) obj).onMuteGroup(aUIMessageModel);
                    }
                }
            });
        }

        public void onUnMuteGroup(final AUIMessageModel<UnMuteGroupMessage> aUIMessageModel) {
            CommonUtil.runOnUI(new Runnable() {
                @Override
                public final void run() {
                    if (TextUtils.isEmpty(aUIMessageModel.groupId)) {
                        for (MessageListener listener : IM_LISTENER_LIST) {
                            listener.onUnMuteGroup(aUIMessageModel);
                        }
                    } else if (IM_LISTENER_MAP.containsKey(aUIMessageModel.groupId)) {
                        Object obj = IM_LISTENER_MAP.get(aUIMessageModel.groupId);

                        ((MessageListener) obj).onUnMuteGroup(aUIMessageModel);
                    }
                }
            });
        }

        public void onMessageReceived(final AUIMessageModel<String> aUIMessageModel) {
            CommonUtil.runOnUI(new Runnable() {
                @Override
                public final void run() {
                    if (TextUtils.isEmpty(aUIMessageModel.groupId)) {
                        for (MessageListener listener : IM_LISTENER_LIST) {
                            listener.onMessageReceived(aUIMessageModel);
                        }
                    } else if (IM_LISTENER_MAP.containsKey(aUIMessageModel.groupId)) {
                        Object obj = IM_LISTENER_MAP.get(aUIMessageModel.groupId);

                        ((MessageListener) obj).onMessageReceived(aUIMessageModel);
                    }
                }
            });
        }

        @Override
        public void onExitedGroup(AUIMessageModel<ExitGroupMessage> message) {
            for (MessageListener listener : IM_LISTENER_LIST) {
                listener.onExitedGroup(message);
            }
        }
    }

    private static IMService mInstance;
    private static Object mLock = new Object();

    private IMService(){
        MessageServiceFactory.useInternal();
        this.mIMService = MessageServiceFactory.getMessageService();
    }

    public static IMService getInstance(){
        if(mInstance == null){
            synchronized (mLock){
                if(mInstance == null){
                    mInstance = new IMService();
                }
            }
        }
        return mInstance;
    }

    //设置login info
    public void setLoginInfo(IMLoginInfo info){
        this.imLoginInfo = info;
    }

    //登录
    public void register(MessageListener listener, ActionCallback callback){

        if(!IM_LISTENER_LIST.contains(listener))
            IM_LISTENER_LIST.add(listener);

        if (!mIMService.isLogin()) {
            mIMService.register(MESSAGE_RECEIVER);
            AUIMessageConfig config = new AUIMessageConfig();
            IMNewToken newToken = new IMNewToken();
            newToken.app_id = this.imLoginInfo.app_id;
            newToken.app_token = this.imLoginInfo.app_token;
            newToken.app_sign = this.imLoginInfo.app_sign;
            newToken.auth = new IMNewTokenAuth();
            newToken.auth.user_id = this.imLoginInfo.auth_user_id;
            newToken.auth.nonce = this.imLoginInfo.auth_once;
            newToken.auth.timestamp = this.imLoginInfo.auth_timestamp;
            newToken.auth.role = this.imLoginInfo.auth_role;
            config.newToken = newToken;
            config.deviceId = this.imLoginInfo.device_id;
            if (mIMService != null) {
                mIMService.setConfig(config);
            }
            AUIMessageUserInfo userInfo = new AUIMessageUserInfo();
            userInfo.userId = this.imLoginInfo.auth_user_id;
            userInfo.userAvatar = this.imLoginInfo.avatar;
            userInfo.userNick = this.imLoginInfo.nick_name;
            if (mIMService == null) {
                return;
            }
            synchronized (mMessagePendingLock){
                mAllowPendingMessage = true;
                mMessagePendingLock.notify();
            }
            mIMService.login(userInfo, new InteractionCallback<Void>() {
                public void onSuccess(Void data) {
                    AliyunLog.d(TAG, "im login success");
                    CommonUtil.actionCallback(callback, 0, null);
                }

                public void onError(InteractionError interactionError) {
                    AliyunLog.d(TAG, String.format("im login failed code[%s] msg[%s]", interactionError.code, interactionError.msg));
                    CommonUtil.actionCallback(callback, -1, interactionError.msg);
                }
            });
        }
    }

    private boolean increasePendingCount(){
        synchronized (mMessagePendingLock){
            if(!mAllowPendingMessage){
                return false;
            }
            mPendingMessageCount++;
        }
        return true;
    }

    private void decreasePendingCount(){
        synchronized (mMessagePendingLock){
            mPendingMessageCount--;
            mMessagePendingLock.notify();
        }
    }

    //登出
    public void unregister(MessageListener listener){
        IM_LISTENER_LIST.remove(listener);

        if (mIMService != null && mIMService.isLogin() && IM_LISTENER_LIST.size() == 0) {
            ThreadUtil.runOnSubThread(new Runnable() {
                @Override
                public void run() {
                    while(mPendingMessageCount > 0){
                        AliyunLog.d(TAG,  "unregister wait count " + mPendingMessageCount);
                        try {
                            synchronized (mMessagePendingLock){
                                mAllowPendingMessage = false;
                                mMessagePendingLock.wait();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            AliyunLog.e(TAG, "" + e.getMessage());
                        }
                    }
                    mIMService.logout(new InteractionCallback<Void>() {
                        public void onSuccess(Void data) {
                            AliyunLog.d(TAG, "IM logout success");
                        }

                        public void onError(InteractionError interactionError) {
                            AliyunLog.d(TAG, "IM logout error " + ((Object) (interactionError == null ? null : interactionError.code)) + ' ' + ((Object) (interactionError != null ? interactionError.msg : null)));
                        }
                    });
                    mIMService.unregister(MESSAGE_RECEIVER);
                }
            });
        }
    }

    public boolean isLogin(){
        if(mIMService == null)
            return false;
        return mIMService.isLogin();
    }

    //发送群组消息
    public int sendGroupMessage(String groupId, int msgType, String json) {
        if (this.mIMService == null || !isLogin()) {
            return -1;
        }
        String methodName = CommonUtil.getCallMethodName();
        AliyunLog.d(TAG, String.format("send type[%d] custom group message[%s]", msgType, json));
        SendMessageToGroupRequest sendReq = new SendMessageToGroupRequest();
        sendReq.groupId = groupId;
        sendReq.msgType = msgType;
        sendReq.data = json;
        if(!increasePendingCount()){
            AliyunLog.d(TAG, String.format("%s type[%d] failed", CommonUtil.getCallMethodName(), msgType));
            return -2;
        }
        mIMService.sendMessageToGroup(sendReq, new InteractionCallback<SendMessageToGroupResponse>() {
            public void onError(InteractionError interactionError) {
                decreasePendingCount();
                AliyunLog.e(TAG, methodName + " onError " + interactionError.msg);
            }

            public void onSuccess(SendMessageToGroupResponse data) {
                decreasePendingCount();
                AliyunLog.d(TAG, String.format("%s %s msgType[%d]", methodName, CommonUtil.getCallMethodName(), msgType));
            }
        });
        return 0;
    }

    //发送个人定向消息
    public int sendMessage(String groupId, String userId, int msgType, String json) {
        if (this.mIMService == null || !this.isLogin()) {
            return -1;
        }
        String methodName = CommonUtil.getCallMethodName();
        AliyunLog.d(TAG, String.format("group[%s] inner message type[%d], userId[%s]", groupId, msgType, userId));
        SendMessageToGroupUserRequest sendReq = new SendMessageToGroupUserRequest();
        sendReq.groupId = groupId;
        sendReq.receiverId = userId;
        sendReq.msgType = msgType;
        sendReq.data = json;
        if(!increasePendingCount()){
            AliyunLog.d(TAG, String.format("%s type[%d] failed", CommonUtil.getCallMethodName(), msgType));
            return -2;
        }
        mIMService.sendMessageToGroupUser(sendReq, new InteractionCallback<SendMessageToGroupUserResponse>() {
            public void onSuccess(SendMessageToGroupUserResponse data2) {
                decreasePendingCount();
                AliyunLog.d(TAG, String.format("%s %s msgType[%d]", methodName, CommonUtil.getCallMethodName(), msgType));
            }

            public void onError(InteractionError interactionError) {
                decreasePendingCount();
                AliyunLog.e(TAG, methodName + " onError " + interactionError.msg);
            }
        });
        return 0;
    }

    //创建群组
    public void createGroup(String groupId, ActionCallback callback){
        CreateGroupRequest req = new CreateGroupRequest();
        req.groupId = groupId;
        req.groupExtension = "";
        mIMService.createGroup(req, new InteractionCallback<CreateGroupResponse>() {
            @Override
            public void onSuccess(final CreateGroupResponse data) {
                CommonUtil.runOnUI(new Runnable() {
                    @Override
                    public final void run() {
                        if (callback == null) {
                            return;
                        }
                        Map<String, Object> params = new HashMap<>();
                        params.put(KEY_GROUP_ID, data.groupId);
                        callback.onResult(0, null, params);
                    }
                });
            }

            @Override
            public void onError(final InteractionError interactionError) {
                CommonUtil.runOnUI(new Runnable() {
                    @Override
                    public final void run() {
                        if (callback == null) {
                            return;
                        }
                        callback.onResult(-1, String.format("create group failed!!! code[%s] msg[%s]", interactionError.code, interactionError.msg), null);
                    }
                });
            }
        });
    }

    //加入群组
    public void joinGroup(String groupId, MessageListener listener, ActionCallback callback){
        if(mIMService == null){
            CommonUtil.actionCallback(callback, -1, "im service null");
            return;
        }
        IM_LISTENER_MAP.put(groupId, listener);
        final JoinGroupRequest joinImGroupReq = new JoinGroupRequest();
        joinImGroupReq.groupId = groupId;
        mIMService.joinGroup(joinImGroupReq, new InteractionCallback<JoinGroupResponse>() {
            public void onSuccess(JoinGroupResponse data) {
                getGroupInfo(groupId, callback);
            }

            public void onError(InteractionError interactionError) {
                AliyunLog.d(TAG, String.format("join IM group [%s] result code[%s] msg[%s]", joinImGroupReq.groupId, interactionError.code, interactionError.msg));
                CommonUtil.actionCallback(callback, -1, interactionError.msg);
            }
        });
    }

    //获取群组信息
    public void getGroupInfo(String groupId, ActionCallback callback){
        if(mIMService == null){
            CommonUtil.actionCallback(callback, -1, "im service null");
            return;
        }
        if(callback == null)
            return;
        GetGroupInfoRequest request = new GetGroupInfoRequest();
        request.groupId = groupId;
        mIMService.getGroupInfo(request, new InteractionCallback<GetGroupInfoResponse>() {
            @Override
            public void onSuccess(GetGroupInfoResponse data) {
                Map<String, Object> params = new Hashtable<>();
                params.put("groupId", data.groupId);
                params.put("onlineCount", data.onlineCount);
                CommonUtil.actionCallback(callback, 0, null, params);
            }

            @Override
            public void onError(InteractionError interactionError) {
                CommonUtil.actionCallback(callback, -1, interactionError != null ? interactionError.msg : "", null);
            }
        });
    }

    //离开群组
    public void leaveGroup(String groupId, ActionCallback callback){
        if(mIMService == null){
            CommonUtil.actionCallback(callback, -1, "im service null");
            return;
        }
        LeaveGroupRequest leaveGroupReq = new LeaveGroupRequest();
        leaveGroupReq.groupId = groupId;
        if (mIMService != null) {
            mIMService.leaveGroup(leaveGroupReq, new InteractionCallback<LeaveGroupResponse>() {
                public void onSuccess(LeaveGroupResponse data) {
                    AliyunLog.d(TAG, "IM leave " + groupId + " success");
                    CommonUtil.actionCallback(callback, 0, null);
                }

                public void onError(InteractionError interactionError) {
                    AliyunLog.e(TAG, String.format("leave group[%s] failed code[%s] msg[%s]", groupId, interactionError.code, interactionError.msg));
                    CommonUtil.actionCallback(callback, -2, interactionError.msg);
                }
            });
        }
        IM_LISTENER_MAP.remove(groupId);
    }
}
