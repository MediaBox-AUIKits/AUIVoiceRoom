package com.aliyun.auikits.voicechat.util;

import android.content.Context;
import android.view.Gravity;

import com.aliyun.auikits.voicechat.R;

import io.github.muddz.styleabletoast.StyleableToast;

public class ToastHelper {

    public static void showToast(Context context, String text, int duration) {
        new StyleableToast.Builder(context)
                .text(text)
                .textColor(context.getResources().getColor(R.color.voicechat_white_default))
                .textSize(14)
                .cornerRadius(8)
                .backgroundColor(context.getResources().getColor(R.color.voicechat_toast_background))
                .length(duration)
                .gravity(Gravity.CENTER)
                .build().show();
    }

    public static void showToast(Context context, int resId, int duration) {
        showToast(context, context.getString(resId), duration);
    }
}
