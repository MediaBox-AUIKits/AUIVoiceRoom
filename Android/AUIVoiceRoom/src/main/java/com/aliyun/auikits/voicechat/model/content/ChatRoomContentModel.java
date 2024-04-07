package com.aliyun.auikits.voicechat.model.content;

import com.aliyun.auikits.voicechat.base.card.CardEntity;
import com.aliyun.auikits.voicechat.base.feed.AbsContentModel;
import com.aliyun.auikits.voicechat.base.feed.BizParameter;
import com.aliyun.auikits.voicechat.base.feed.IBizCallback;
import com.aliyun.auikits.voicechat.base.feed.IContentObserver;
import com.aliyun.auikits.voicechat.base.network.RetrofitManager;
import com.aliyun.auikits.voicechat.model.api.ChatRoomApi;
import com.aliyun.auikits.voicechat.model.entity.ChatMember;
import com.aliyun.auikits.voicechat.model.entity.ChatRoomItem;
import com.aliyun.auikits.voicechat.model.entity.network.ChatRoomListRequest;
import com.aliyun.auikits.voicechat.model.entity.network.ChatRoomListResponse;
import com.aliyun.auikits.voicechat.model.entity.network.ChatRoomRspItem;
import com.aliyun.auikits.voicechat.model.entity.network.CreateRoomRequest;
import com.aliyun.auikits.voicechat.model.entity.network.CreateRoomResponse;
import com.aliyun.auikits.voicechat.util.GsonHolder;
import com.aliyun.auikits.voicechat.widget.card.CardTypeDef;
import com.github.javafaker.Faker;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ChatRoomContentModel extends AbsContentModel<CardEntity> {
    public static final String KEY_AUTHORIZATION = "AUTHORIZATION";
    private int currentPage = 1;

    @Override
    public void initData(BizParameter parameter, IBizCallback<CardEntity> callback) {
        currentPage = 1;
        String request_token = parameter.getQuerySet().get(KEY_AUTHORIZATION);
        ChatRoomListRequest chatRoomListRequest = new ChatRoomListRequest();
        chatRoomListRequest.user_id = "admin";
        RetrofitManager.getRetrofit(ChatRoomApi.HOST).create(ChatRoomApi.class)
                        .fetchRoomList(request_token, chatRoomListRequest)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<ChatRoomListResponse>() {
                                    @Override
                                    public void accept(ChatRoomListResponse chatRoomListResponse) throws Throwable {
                                        if(chatRoomListResponse.isSuccess()) {
                                            List<CardEntity> cardEntityList = new ArrayList<>();
                                            if(chatRoomListResponse.rooms != null) {
                                                for(ChatRoomRspItem chatRoomRspItem : chatRoomListResponse.rooms) {
                                                    CardEntity cardEntity = new CardEntity();
                                                    cardEntity.cardType = CardTypeDef.CHAT_ROOM_CARD;
                                                    cardEntity.bizData = chatRoomRspItem.toChatRoomItem();
                                                    cardEntityList.add(cardEntity);
                                                }
                                            }
                                            if (callback != null) {
                                                callback.onSuccess(cardEntityList);
                                            }
                                        } else {
                                            if (callback != null) {
                                                callback.onError(-1, String.valueOf(chatRoomListResponse.getCode()));
                                            }
                                        }

                                    }
                                }, new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Throwable {
                                        if (callback != null) {
                                            callback.onError(-1, throwable.getMessage());
                                        }
                                    }
                                });

    }

    @Override
    public void fetchData(boolean isPullToRefresh, BizParameter parameter, IBizCallback<CardEntity> callback) {
        String request_token = parameter.getQuerySet().get(KEY_AUTHORIZATION);
        ChatRoomListRequest chatRoomListRequest = new ChatRoomListRequest();
        chatRoomListRequest.user_id = "admin";
        chatRoomListRequest.page_num = currentPage + 1;
        RetrofitManager.getRetrofit(ChatRoomApi.HOST).create(ChatRoomApi.class)
                .fetchRoomList(request_token, chatRoomListRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ChatRoomListResponse>() {
                    @Override
                    public void accept(ChatRoomListResponse chatRoomListResponse) throws Throwable {
                        if(chatRoomListResponse.isSuccess()) {
                            List<CardEntity> cardEntityList = new ArrayList<>();
                            if(chatRoomListResponse.rooms != null && chatRoomListResponse.rooms.size() > 0) {
                                currentPage += 1;
                                for(ChatRoomRspItem chatRoomRspItem : chatRoomListResponse.rooms) {
                                    CardEntity cardEntity = new CardEntity();
                                    cardEntity.cardType = CardTypeDef.CHAT_ROOM_CARD;
                                    cardEntity.bizData = chatRoomRspItem.toChatRoomItem();
                                    cardEntityList.add(cardEntity);
                                }
                            }
                            if (callback != null) {
                                callback.onSuccess(cardEntityList);
                            }
                        } else {
                            if (callback != null) {
                                callback.onError(-1, String.valueOf(chatRoomListResponse.getCode()));
                            }
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        if (callback != null) {
                            callback.onError(-1, throwable.getMessage());
                        }
                    }
                });
    }

    public void createRoom(String app_sign, ChatMember creator, IBizCallback<CardEntity> callback) {

        CreateRoomRequest createRoomRequest = new CreateRoomRequest();
        createRoomRequest.title = creator.getName() + "的聊天室";
        createRoomRequest.anchor = creator.getId();
        createRoomRequest.anchor_nick = creator.getName();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(ChatRoomRspItem.KEY_ANCHOR_AVATAR, creator.getAvatar());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        createRoomRequest.exd = jsonObject.toString();
        RetrofitManager.getRetrofit(ChatRoomApi.HOST).create(ChatRoomApi.class)
                .createRoom(app_sign,createRoomRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Function<CreateRoomResponse, List<CardEntity>>() {
                    @Override
                    public List<CardEntity> apply(CreateRoomResponse createRoomResponse) throws Throwable {
                        List<CardEntity> cardEntityList = new ArrayList<>();
                        if(createRoomResponse.isSuccess()) {
                            CardEntity cardEntity = new CardEntity();
                            cardEntity.cardType = CardTypeDef.CHAT_ROOM_CARD;
                            ChatRoomItem chatRoomItem = new ChatRoomItem();

                            chatRoomItem.setTitle(creator.getName() + "的聊天室");
                            chatRoomItem.setId(createRoomResponse.id);
                            chatRoomItem.setRoomId(String.valueOf(createRoomResponse.show_code));
                            ChatMember compere = new ChatMember(creator.getId());
                            compere.setName(creator.getName());
                            compere.setAvatar(creator.getAvatar());
                            compere.setIdentifyFlag(ChatMember.IDENTIFY_FLAG_COMPERE);

                            chatRoomItem.setCompere(compere);

                            //把创建成功的数据插入插入到列表上
                            cardEntity.bizData = chatRoomItem;
                            cardEntityList.add(cardEntity);
                        }

                        return cardEntityList;
                    }
                })
                .subscribe(new Consumer<List<CardEntity>>() {
                    @Override
                    public void accept(List<CardEntity> cardEntityList) throws Throwable {


                        //请求成功之后,插入到列表
                        if (observers != null) {
                            for (IContentObserver<CardEntity> observer : observers) {
                                observer.onContentInsert(cardEntityList);
                            }
                        }

                        if (callback != null) {
                            callback.onSuccess(cardEntityList);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        if (callback != null) {
                            callback.onError(-1, throwable.getMessage());
                        }
                    }
                });
    }
}
