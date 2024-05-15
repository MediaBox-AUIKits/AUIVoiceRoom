package com.aliyun.auikits.voicechat.widget.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;

import androidx.databinding.DataBindingUtil;

import com.aliyun.auikits.voicechat.R;
import com.aliyun.auikits.voicechat.databinding.VoicechatDialogExitBinding;
import com.aliyun.auikits.voicechat.ChatActivity;
import com.aliyun.auikits.voicechat.base.network.RetrofitManager;
import com.aliyun.auikits.voicechat.model.api.ChatRoomApi;
import com.aliyun.auikits.voicechat.model.entity.network.CloseRoomRequest;
import com.aliyun.auikits.voicechat.model.entity.network.CloseRoomResponse;
import com.aliyun.auikits.voicechat.service.ChatRoomManager;
import com.aliyun.auikits.voicechat.service.ChatRoomService;
import com.aliyun.auikits.voicechat.widget.list.CustomViewHolder;
import com.aliyun.auikits.voice.ARTCVoiceRoomEngine;
import com.orhanobut.dialogplus.DialogPlus;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DialogHelper {

    public static void showCloseDialog(Context context, ARTCVoiceRoomEngine roomController, boolean isCompere) {
        if(context instanceof Activity) {

            VoicechatDialogExitBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.voicechat_dialog_exit, null, false);
            if(isCompere) {
                binding.tvDialogTips.setText(R.string.voicechat_exit_room_tip_for_compere);
            } else {
                binding.tvDialogTips.setText(R.string.voicechat_exit_room_tip_for_other);
            }
            CustomViewHolder viewHolder = new CustomViewHolder(binding.getRoot());
            DialogPlus dialog = DialogPlus.newDialog(context)
                    .setContentHolder(viewHolder)
                    .setGravity(Gravity.CENTER)
                    .setExpanded(false)
                    .setOverlayBackgroundResource(android.R.color.transparent)
                    .setContentBackgroundResource(R.drawable.voicechat_dialog_exit_bg)
                    .setOnClickListener((dialog1, v) -> {
                        if(v.getId() == R.id.btn_confirm) {
                            Observable<Integer> exitRoomObservable = null;
                            if(isCompere) {
                                String authorization = (String) ChatRoomManager.getInstance().getGlobalParam(ChatActivity.KEY_AUTHORIZATION);
                                if(authorization != null) {
                                    CloseRoomRequest closeRoomRequest = new CloseRoomRequest();
                                    closeRoomRequest.id = roomController.getRoomInfo().roomId;
                                    closeRoomRequest.user_id = roomController.getCurrentUser().userId;
                                    exitRoomObservable = RetrofitManager.getRetrofit(ChatRoomApi.HOST).create(ChatRoomApi.class)
                                        .dismissRoom(authorization, closeRoomRequest)
                                            .flatMap(new Function<CloseRoomResponse, ObservableSource<Integer>>() {
                                                @Override
                                                public ObservableSource<Integer> apply(CloseRoomResponse closeRoomResponse) throws Throwable {
                                                    if(closeRoomResponse.success) {
                                                        return ChatRoomService.exitRoom(roomController, isCompere);
                                                    }
                                                    return Observable.just(-1);
                                                }
                                            });
                                } else {
                                    exitRoomObservable = Observable.just(-1);
                                }
                            } else {
                                exitRoomObservable = ChatRoomService.exitRoom(roomController, isCompere);
                            }
                            exitRoomObservable
                              .subscribeOn(Schedulers.io())
                             .observeOn(AndroidSchedulers.mainThread())
                             .subscribe(new Consumer<Integer>() {
                                 @Override
                                 public void accept(Integer code) throws Throwable {
                                     Log.v("DialogHelper", "exit Room:" + code);
                                 }
                             }, new Consumer<Throwable>() {
                                 @Override
                                 public void accept(Throwable throwable) throws Throwable {
                                     throwable.printStackTrace();
                                 }
                             });

                            Intent returnIntent = new Intent();
                            if(isCompere) {
                                returnIntent.putExtra(ChatActivity.KEY_ROOM_DISMISS, true);
                            }
                            returnIntent.putExtra(ChatActivity.KEY_ROOM_ID, roomController.getRoomInfo().roomId);
                            ((Activity) context).setResult(Activity.RESULT_OK , returnIntent);
                            dialog1.dismiss();
                            ((Activity) context).finish();

                        } else if(v.getId() == R.id.btn_cancel) {
                            dialog1.dismiss();
                        }
                    })
                    .create();
            dialog.show();


        }
    }
}
