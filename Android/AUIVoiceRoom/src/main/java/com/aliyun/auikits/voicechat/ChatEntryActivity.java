package com.aliyun.auikits.voicechat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alivc.auimessage.model.token.IMNewToken;
import com.aliyun.auikits.voicechat.R;
import com.aliyun.auikits.voicechat.databinding.VoicechatActivityEntryBinding;
import com.aliyun.auikits.voicechat.databinding.VoicechatDialogLoadingBinding;
import com.aliyun.auikits.voicechat.base.card.CardEntity;
import com.aliyun.auikits.voicechat.base.card.CardListAdapter;
import com.aliyun.auikits.voicechat.adapter.ChatItemDecoration;
import com.aliyun.auikits.voicechat.base.feed.BizParameter;
import com.aliyun.auikits.voicechat.base.feed.ContentViewModel;
import com.aliyun.auikits.voicechat.base.feed.IBizCallback;
import com.aliyun.auikits.voicechat.base.network.RetrofitManager;
import com.aliyun.auikits.voicechat.model.api.ChatRoomApi;
import com.aliyun.auikits.voicechat.model.entity.ChatMember;
import com.aliyun.auikits.voicechat.model.entity.ChatRoomItem;
import com.aliyun.auikits.voicechat.model.content.ChatRoomContentModel;
import com.aliyun.auikits.voicechat.model.entity.network.ChatRoomRequest;
import com.aliyun.auikits.voicechat.model.entity.network.ChatRoomResponse;
import com.aliyun.auikits.voicechat.model.entity.network.RtcTokenRequest;
import com.aliyun.auikits.voicechat.model.entity.network.RtcTokenResponse;
import com.aliyun.auikits.voicechat.service.ChatRoomManager;
import com.aliyun.auikits.voicechat.service.ChatRoomService;
import com.aliyun.auikits.voicechat.util.AvatarUtil;
import com.aliyun.auikits.voicechat.util.ToastHelper;
import com.aliyun.auikits.voicechat.widget.card.CardTypeDef;
import com.aliyun.auikits.voicechat.widget.card.ChatRoomCard;
import com.aliyun.auikits.voicechat.widget.card.DefaultCardViewFactory;
import com.aliyun.auikits.voicechat.util.DisplayUtil;
import com.aliyun.auikits.voicechat.widget.list.CustomViewHolder;
import com.aliyun.auikits.voiceroom.AUIVoiceRoom;
import com.aliyun.auikits.voiceroom.bean.NetworkState;
import com.aliyun.auikits.voiceroom.bean.RoomInfo;
import com.aliyun.auikits.voiceroom.bean.UserInfo;
import com.aliyun.auikits.voiceroom.callback.ActionCallback;
import com.aliyun.auikits.voiceroom.external.RtcInfo;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.jaeger.library.StatusBarUtil;
import com.orhanobut.dialogplus.DialogPlus;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;

@Route(path = "/voicechat/ChatEntryActivity")
public class ChatEntryActivity extends AppCompatActivity implements ContentViewModel.OnDataUpdateCallback {
    private static final String TAG = "ChatEntryTag";

    //IM信息
    public static final String KEY_IM_TAG = "CHAT_IM_TAG";
    public static final String KEY_AUTHORIZATION = "AUTHORIZATION";
    public static final String KEY_USER_ID = "USER_ID";
    public static final int REQUEST_CODE = 1002;

    private VoicechatActivityEntryBinding binding;
    private CardListAdapter chatRoomItemAdapter;
    private ContentViewModel contentViewModel;
    private ChatRoomContentModel chatRoomContentModel;

    private ChatMember currentUser;
    private IMNewToken im_token;
    private String authorization;
    private BizParameter bizParameter = new BizParameter();

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setTransparent(this);
        RxJavaPlugins.setErrorHandler(Functions.<Throwable>emptyConsumer());

        binding = DataBindingUtil.setContentView(this, R.layout.voicechat_activity_entry);
        binding.setLifecycleOwner(this);

        Intent intent = getIntent();
        // 检查 Intent 的 action 和 category
        boolean isLaunchedFromHome = Intent.ACTION_MAIN.equals(intent.getAction()) &&
                intent.hasCategory(Intent.CATEGORY_LAUNCHER);

        if(isLaunchedFromHome) {
            findViewById(R.id.back_btn).setVisibility(View.GONE);
        } else {
            findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
        }

        im_token = (IMNewToken) getIntent().getSerializableExtra(KEY_IM_TAG);
        authorization = getIntent().getStringExtra(KEY_AUTHORIZATION);
        String userId = getIntent().getStringExtra(KEY_USER_ID);
        if(im_token == null || authorization == null || userId == null) {
            Toast.makeText(ChatEntryActivity.this, R.string.voicechat_invalid_param, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUser = new ChatMember(userId);

        DefaultCardViewFactory factory = new DefaultCardViewFactory();
        factory.registerCardView(CardTypeDef.CHAT_ROOM_CARD, ChatRoomCard.class);
        chatRoomItemAdapter = new CardListAdapter(factory);
        binding.rvChatRoomList.setLayoutManager(new GridLayoutManager(this,2));
        binding.rvChatRoomList.setAdapter(chatRoomItemAdapter);
        binding.rvChatRoomList.addItemDecoration(new ChatItemDecoration((int) DisplayUtil.convertDpToPixel(6, this), (int) DisplayUtil.convertDpToPixel(12, this)));

        chatRoomContentModel = new ChatRoomContentModel();
        contentViewModel = new ContentViewModel.Builder()
                        .setContentModel(chatRoomContentModel)
                        .setBizParameter(bizParameter)
                        .setLoadMoreEnable(false)
                        .setEmptyView(R.layout.voicechat_list_room_empty_view)
//                        .setLoadingView(R.layout.voicechat_loading_view)
                        .setErrorView(R.layout.voicechat_layout_error_view, R.id.btn_retry)
                        .setOnDataUpdateCallback(this)
                        .build();


        chatRoomItemAdapter.addChildClickViewIds(R.id.btn_chat_room_enter);
        chatRoomItemAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {
            @Override
            public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                CardEntity cardEntity = (CardEntity) adapter.getItem(position);
                jumpToChatActivity((ChatRoomItem) cardEntity.bizData);
            }
        });
        chatRoomItemAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                CardEntity cardEntity = (CardEntity) adapter.getItem(position);
                jumpToChatActivity((ChatRoomItem) cardEntity.bizData);
            }
        });

        binding.srlChatRoomList.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                contentViewModel.initData();
            }
        });

        binding.srlChatRoomList.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                contentViewModel.loadMore();
            }
        });

        onDataInit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        contentViewModel.unBind();
        ChatRoomManager.getInstance().destroy();
    }

    private void onDataInit() {

        //目前名字设置成和ID一样，业务方可以根据自己的用户登录体系去改
        currentUser.setName(currentUser.getId());
        currentUser.setAvatar(AvatarUtil.getAvatarUrl(currentUser.getId()));
        currentUser.setIdentifyFlag(ChatMember.IDENTIFY_FLAG_SELF);
        currentUser.setMicrophoneStatus(ChatMember.MICROPHONE_STATUS_DISABLE);
        currentUser.setNetworkStatus(NetworkState.NORMAL);

        bizParameter.append(ChatRoomContentModel.KEY_AUTHORIZATION, authorization);
        ChatRoomManager.getInstance().addGlobalParam(ChatActivity.KEY_AUTHORIZATION, authorization);
        contentViewModel.bindView(chatRoomItemAdapter);
    }


    public void onClickCreateRoom(View view) {

        ChatMember currentUser = getCurrentUser();

        DialogPlus dialog = createLoadingDialog();
        dialog.show();

        //创建房间
        Observable.create(new ObservableOnSubscribe<List<CardEntity>>() {
            @Override
            public void subscribe(@io.reactivex.rxjava3.annotations.NonNull ObservableEmitter<List<CardEntity>> emitter) throws Throwable {
                chatRoomContentModel.createRoom(ChatEntryActivity.this.authorization, currentUser, new IBizCallback<CardEntity>() {
                    @Override
                    public void onSuccess(List<CardEntity> data) {
                        if(data.size() > 0) {
                            Log.v(TAG, "create room success");
                            emitter.onNext(data);
                            emitter.onComplete();
                        } else {
                            Log.v(TAG, "create room fail");
                            emitter.onError(new RuntimeException(ChatEntryActivity.this.getString(R.string.voicechat_create_room_failed)));
                        }
                    }

                    @Override
                    public void onError(int code, String msg) {
                        emitter.onError(new RuntimeException(ChatEntryActivity.this.getString(R.string.voicechat_create_room_failed)));
                    }
                });
            }
        })
        //加入房间
        .flatMap(new Function<List<CardEntity>, ObservableSource<Pair<ChatRoomItem, Integer>>>() {
            @Override
            public ObservableSource<Pair<ChatRoomItem, Integer>> apply(List<CardEntity> cardEntities) throws Throwable {
                CardEntity cardEntity = cardEntities.get(0);
                ChatRoomItem chatRoomItem = (ChatRoomItem) cardEntity.bizData;

                return Observable.zip(Observable.just(chatRoomItem), createJoinRoomObservable(chatRoomItem, currentUser), (chatRoomParam, joinRoomResponse) -> {
                    return new Pair<>(chatRoomParam, joinRoomResponse);
                });
            }
        }).timeout(10, TimeUnit.SECONDS)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
          .subscribe(
                new Consumer<Pair<ChatRoomItem, Integer>>() {
                    @Override
                    public void accept(Pair<ChatRoomItem, Integer> data) throws Throwable {
                        dialog.dismiss();
                        if(data.second == ChatRoomManager.CODE_SUCCESS) {
                            Intent intent = new Intent(ChatEntryActivity.this, ChatActivity.class);
                            intent.putExtra(ChatActivity.KEY_CHAT_ROOM_ENTITY, data.first);
                            intent.putExtra(ChatActivity.KEY_CHAT_SELF_ENTITY, currentUser);
                            intent.putExtra(ChatActivity.KEY_AUTHORIZATION, ChatEntryActivity.this.authorization);
                            startActivityForResult(intent, REQUEST_CODE);
                        } else {
                            ToastHelper.showToast(ChatEntryActivity.this, R.string.voicechat_join_room_failed, Toast.LENGTH_SHORT);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        dialog.dismiss();
                        throwable.printStackTrace();
                        if(throwable instanceof RuntimeException) {
                            ToastHelper.showToast(ChatEntryActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT);
                        } else {
                            ToastHelper.showToast(ChatEntryActivity.this, R.string.voicechat_join_room_failed, Toast.LENGTH_SHORT);
                        }

                    }
                });
    }

    private Observable<Integer> createJoinRoomObservable(ChatRoomItem chatRoomItem, ChatMember currentUser) {
        RoomInfo roomInfo = new RoomInfo(chatRoomItem.getId());
        roomInfo.creator = chatRoomItem.getCompere().getId();
        String authorization = ChatEntryActivity.this.authorization;
        RtcTokenRequest rtcTokenRequest = new RtcTokenRequest();
        rtcTokenRequest.room_id = roomInfo.roomId;
        rtcTokenRequest.user_id = currentUser.getId();
        //获取RTC Token
        Observable<RtcInfo> rtcInfoObservable = RetrofitManager.getRetrofit(ChatRoomApi.HOST).create(ChatRoomApi.class).getRtcToken(authorization, rtcTokenRequest)
                .map(new Function<RtcTokenResponse, RtcInfo>() {
                    @Override
                    public RtcInfo apply(RtcTokenResponse response) throws Throwable {
                        Log.v(TAG, "get rtc token  success");
                        return new RtcInfo(response.auth_token, response.timestamp, ChatRoomApi.RTC_GLSB);
                    }
                });

        //房间初始化
        Observable<AUIVoiceRoom> roomInitObservable = Observable.create(new ObservableOnSubscribe<AUIVoiceRoom>() {
            @Override
            public void subscribe(@io.reactivex.rxjava3.annotations.NonNull ObservableEmitter<AUIVoiceRoom> emitter) throws Throwable {
                AUIVoiceRoom auiVoiceRoom = ChatRoomManager.getInstance().createVoiceRoom(roomInfo.roomId);
                UserInfo userInfo = new UserInfo(currentUser.getId(), currentUser.getId());
                userInfo.userName = currentUser.getName();
                userInfo.avatarUrl = currentUser.getAvatar();

                auiVoiceRoom.init(ChatEntryActivity.this.getApplicationContext(),ChatRoomApi.APP_ID,  ChatEntryActivity.this.authorization, userInfo, ChatEntryActivity.this.im_token, new ActionCallback() {
                    @Override
                    public void onResult(int code, String msg, Map<String, Object> params) {
                        if(code == ChatRoomManager.CODE_SUCCESS) {
                            Log.v(TAG, "init room success");
                            emitter.onNext(auiVoiceRoom);
                            emitter.onComplete();
                        } else {
                            ChatRoomManager.getInstance().destroyVoiceRoom(roomInfo.roomId);
                            Log.v(TAG, "init room fail:code:" + code + ",msg:" + msg );
                            emitter.onError(new RuntimeException(msg));
                        }
                    }
                });
            }
        });

        ChatRoomRequest chatRoomRequest = new ChatRoomRequest();
        chatRoomRequest.id = chatRoomItem.getId();
        chatRoomRequest.user_id = currentUser.getId();
        Observable<ChatRoomResponse> getRoomObservable = RetrofitManager.getRetrofit(ChatRoomApi.HOST).create(ChatRoomApi.class).getRoomInfo(ChatEntryActivity.this.authorization, chatRoomRequest);

        //JoinRoom
        Observable<Integer> joinRoomObservable = Observable.zip(rtcInfoObservable, roomInitObservable, getRoomObservable, (rtcInfo, voiceRoom, roomResponse) -> {
                    return new Object[] {rtcInfo, voiceRoom, roomResponse};
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Function<Object[], ObservableSource<Integer>>() {
                    @Override
                    public ObservableSource<Integer> apply(Object[] rtcInfoAUIVoiceRoomPair) throws Throwable {
                        ChatRoomResponse chatRoomResponse = ((ChatRoomResponse)rtcInfoAUIVoiceRoomPair[2]);
                        if(chatRoomResponse.isRoomValid()) {
                            Log.v(TAG, "room is valid");
                            chatRoomItem.setMemberNum(chatRoomResponse.getOnlineCount());
                            return ChatRoomService.joinRoom(((AUIVoiceRoom)rtcInfoAUIVoiceRoomPair[1]), roomInfo, (RtcInfo) rtcInfoAUIVoiceRoomPair[0]);
                        }  else {
                            Log.v(TAG, "room is closed");
                            return Observable.error(new RuntimeException(ChatEntryActivity.this.getString(R.string.voicechat_room_closed)));
                        }
                    }
                }).doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer code) throws Throwable {
                        if(code != ChatRoomManager.CODE_SUCCESS) {
                            ChatRoomManager.getInstance().destroyVoiceRoom(roomInfo.roomId);
                        } else {
                            //加入房间成功后，自动在房间人数上+1
                            chatRoomItem.setMemberNum(chatRoomItem.getMemberNum() + 1);
                        }
                    }
                }).doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        ChatRoomManager.getInstance().destroyVoiceRoom(roomInfo.roomId);
                    }
                });
        return joinRoomObservable;

    }

    private void jumpToChatActivity(ChatRoomItem chatRoomItem, ChatMember currentUser) {
        DialogPlus dialog = createLoadingDialog();
        dialog.show();
        createJoinRoomObservable(chatRoomItem, currentUser)
                .timeout(10, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer code) throws Throwable {
                        dialog.dismiss();

                        if(code == ChatRoomManager.CODE_SUCCESS) {
                            Log.v(TAG, "join room is success");
                            Intent intent = new Intent(ChatEntryActivity.this, ChatActivity.class);
                            intent.putExtra(ChatActivity.KEY_CHAT_ROOM_ENTITY, chatRoomItem);
                            intent.putExtra(ChatActivity.KEY_CHAT_SELF_ENTITY, currentUser);
                            intent.putExtra(ChatActivity.KEY_AUTHORIZATION, ChatEntryActivity.this.authorization);
                            startActivityForResult(intent, REQUEST_CODE);
                        } else {
                            Log.v(TAG, "join room is fail:" + code);
                            ToastHelper.showToast(ChatEntryActivity.this, R.string.voicechat_join_room_failed, Toast.LENGTH_SHORT);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        dialog.dismiss();
                        throwable.printStackTrace();
                        if(throwable instanceof RuntimeException) {
                            ToastHelper.showToast(ChatEntryActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT);
                        } else {
                            ToastHelper.showToast(ChatEntryActivity.this, R.string.voicechat_join_room_failed, Toast.LENGTH_SHORT);
                        }

                    }
                });
    }

    private void jumpToChatActivity(ChatRoomItem chatRoomItem) {
        jumpToChatActivity(chatRoomItem, getCurrentUser());
    }

    private ChatMember getCurrentUser() {
        return currentUser;
    }

    private DialogPlus createLoadingDialog() {
        VoicechatDialogLoadingBinding binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.voicechat_dialog_loading, null, false);
        binding.setLifecycleOwner(this);
        CustomViewHolder viewHolder = new CustomViewHolder(binding.getRoot());
        DialogPlus dialog = DialogPlus.newDialog(this)
                .setContentHolder(viewHolder)
                .setGravity(Gravity.CENTER)
                .setExpanded(false)
                .setContentWidth(DisplayUtil.dip2px(112))
                .setContentBackgroundResource(android.R.color.transparent)
                .setOverlayBackgroundResource(android.R.color.transparent)
                .setCancelable(false)
                .create();
        return dialog;
    }

    @Override
    public void onInitStart() {


    }

    @Override
    public void onInitEnd(boolean success, List<CardEntity> cardEntities) {

        binding.srlChatRoomList.finishRefresh();

    }

    @Override
    public void onLoadMoreStart() {

    }

    @Override
    public void onLoadMoreEnd(boolean success, List<CardEntity> cardEntities) {
        binding.srlChatRoomList.finishLoadMore();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {

                boolean roomDismiss = data.getBooleanExtra(ChatActivity.KEY_ROOM_DISMISS, false);
                String roomID = data.getStringExtra(ChatActivity.KEY_ROOM_ID);
                if(!TextUtils.isEmpty(roomID)) {
                    //如果房间解散，则移除卡片上的数据
                    if(roomDismiss) {
                        List<CardEntity> cardEntityList = chatRoomItemAdapter.getData();
                        CardEntity targetCardEntity = null;
                        int targetCardPos = -1;
                        for(CardEntity cardEntity : cardEntityList) {
                            ChatRoomItem chatRoomItem = (ChatRoomItem) cardEntity.bizData;
                            targetCardPos++;
                            if(chatRoomItem.getId().equals(roomID)) {
                                targetCardEntity = cardEntity;
                                break;
                            }
                        }
                        if(targetCardEntity != null && targetCardPos >= 0) {
                            chatRoomItemAdapter.removeAt(targetCardPos);
                        }
                    } else {
                        ChatRoomManager.getInstance().destroyVoiceRoom(roomID);
                    }

                }
            }
        }
    }
}
