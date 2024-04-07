package com.aliyun.auikits.voicechat.widget.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.annotation.Nullable;

@SuppressLint("AppCompatCustomView")
public class StateTextView extends TextView {
    public StateTextView(Context context) {
        super(context);
    }

    public StateTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public StateTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        ViewStateHelper.applyViewTouchAlpha(this, event);

        return super.onTouchEvent(event);
    }
}
