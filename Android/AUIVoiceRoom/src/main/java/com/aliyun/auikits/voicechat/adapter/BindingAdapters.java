package com.aliyun.auikits.voicechat.adapter;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import com.aliyun.auikits.voicechat.util.ImageTools;
import com.aliyun.auikits.voicechat.widget.helper.DebouncedOnClickListener;
import com.bumptech.glide.load.Transformation;

public class BindingAdapters {
    @BindingAdapter(
            value = {"imageUrl", "placeholder", "error", "imageResource", "transformation"},
            requireAll = false
    )
    public static void loadImage(ImageView view,
                                 String imageUrl,
                                 Drawable placeHolder,
                                 Drawable error,
                                 int imageResource,
                                 Transformation<Bitmap> transform
                                 ) {

        if(imageResource != 0) {
            view.setImageResource(imageResource);
        }
        if(TextUtils.isEmpty(imageUrl)) {
            if(error != null) {
                view.setImageDrawable(error);
            }
            return;
        }

        ImageTools.loadImage(view, imageUrl, placeHolder, error, transform);

    }



    @BindingAdapter("visibleOrGone")
    public static void setVisibleOrGone(View view, Boolean show) {
        view.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @BindingAdapter("textColor")
    public static void setTextColor(TextView view, int color) {
        view.setTextColor(color);
    }

    @BindingAdapter("animation")
    public static void switchAnimation(com.skyfishjy.library.RippleBackground view,
                                 boolean animation
    ) {

        if(animation) {
            view.startRippleAnimation();
        } else {
            view.stopRippleAnimation();
        }
    }

    @BindingAdapter("android:onClick")
    public static void debouncedListener(View view, View.OnClickListener listener) {
        view.setOnClickListener(new DebouncedOnClickListener(listener));
    }


}
