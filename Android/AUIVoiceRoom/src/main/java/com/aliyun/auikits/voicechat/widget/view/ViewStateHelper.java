package com.aliyun.auikits.voicechat.widget.view;

import android.view.MotionEvent;
import android.view.View;

public class ViewStateHelper {
    public static void applyViewTouchAlpha(View view, MotionEvent event) {
        if (!view.isEnabled() || !view.isClickable()) {
            return;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                view.setAlpha(0.5f);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                view.setAlpha(1f);
                break;

            default:
                break;
        }
    }
}
