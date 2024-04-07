package com.aliyun.auikits.voicechat.widget.view;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.RecyclerView;

public class MsgRecyclerView extends RecyclerView {

    public MsgRecyclerView(Context context) {
        super(context);
    }

    public MsgRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MsgRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected float getTopFadingEdgeStrength() {
        // 返回一个介于0和1之间的值来表示顶部淡出的强度
        return 1.0f; // 完全淡出
    }

    @Override
    protected float getBottomFadingEdgeStrength() {
        // 返回一个介于0和1之间的值来表示底部淡出的强度
        return 0.0f; // 完全淡出
    }
}