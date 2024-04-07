package com.aliyun.auikits.voicechat.vm;


import android.content.Context;
import android.text.style.ForegroundColorSpan;

import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;

import com.aliyun.auikits.voicechat.R;
import com.aliyun.auikits.voicechat.model.entity.ChatMember;
import com.aliyun.auikits.voicechat.model.entity.ChatMessage;
import com.aliyun.auikits.voicechat.util.DisplayUtil;
import com.aliyun.auikits.voicechat.widget.view.TextTagSpan;
import com.aliyun.auikits.voicechat.widget.view.Truss;


public class ChatMessageViewModel extends ViewModel {

    public ObservableField<CharSequence> content = new ObservableField<CharSequence>();

    public void bind(Context context, ChatMessage msg) {

        if(msg.getType() == ChatMessage.TYPE_CHAT_MSG){
            ChatMember chatMember = msg.getMember();

            TextTagSpan identifyFlagSpan = null;
            String identifyFlagStr = null;
            if(chatMember.getIdentifyFlag() == ChatMember.IDENTIFY_FLAG_COMPERE) {
                identifyFlagSpan = new TextTagSpan(context, (int)DisplayUtil.convertDpToPixel(24, context), (int)DisplayUtil.convertDpToPixel(12, context))
                        .setRightMargin((int) DisplayUtil.convertDpToPixel(2, context))
                        .setTextColor(context.getResources().getColor(R.color.voicechat_white_default))
                        .setTextSize(DisplayUtil.sp2px(8))
                        .setRadius((int)DisplayUtil.convertDpToPixel(6, context))
                        .setBackground(R.drawable.voicechat_chat_compere_flag_bg);

                identifyFlagStr = context.getString(R.string.voicechat_chat_compere_flag);

            } else if(chatMember.getIdentifyFlag() == ChatMember.IDENTIFY_FLAG_SELF){
                identifyFlagSpan = new TextTagSpan(context, (int)DisplayUtil.convertDpToPixel(24, context), (int)DisplayUtil.convertDpToPixel(12, context))
                        .setRightMargin((int) DisplayUtil.convertDpToPixel(2, context))
                        .setTextColor(context.getResources().getColor(R.color.voicechat_white_default))
                        .setTextSize(DisplayUtil.sp2px(8))
                        .setRadius((int)DisplayUtil.convertDpToPixel(6, context))
                        .setBackground(R.drawable.voicechat_chat_self_flag_bg);
                identifyFlagStr = context.getString(R.string.voicechat_chat_self_flag);
            }
            Truss truss = new Truss();
            if(identifyFlagSpan != null) {
                truss.pushSpan(identifyFlagSpan)
                        .append(identifyFlagStr)
                        .popSpan();
            }
            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(context.getResources().getColor(R.color.voicechat_msg_author_color));
            truss.pushSpan(foregroundColorSpan)
                    .append(chatMember.getName() + " : ")
                    .popSpan()
                    .append(msg.getContent());
            this.content.set(truss.build());
        } else {
            this.content.set(msg.getContent());
        }

    }
}
