package com.aliyun.auikits.voicechat.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;

import de.hdodenhof.circleimageview.CircleImageView;

public class ImageTools {
    public static void loadImage(
            ImageView view,
            String imageUrl,
            Drawable placeHolder,
            Drawable error,
            Transformation<Bitmap> transform
    ) {
        boolean noAnimate = (view instanceof CircleImageView);
        loadImage(view, imageUrl, placeHolder, error, transform, DecodeFormat.DEFAULT, noAnimate, null);
    }

    public static void loadImage(ImageView view,
                                 String imageUrl,
                                 Drawable placeHolder,
                                 Drawable errorHolder,
                                 Transformation<Bitmap> transform,
                                 DecodeFormat format,
                                 boolean noAnimate,
                                 Target<Drawable> target
    ) {
        if(TextUtils.isEmpty(imageUrl)) {
            if(placeHolder != null) {
                view.setImageDrawable(placeHolder);
            } else if(errorHolder != null) {
                view.setImageDrawable(errorHolder);
            }

            return;
        }

        RequestBuilder<Drawable> loadBuilder;
        if(view.getContext() instanceof Activity) {
            if(((Activity) view.getContext()).isDestroyed()) {
                return;
            }
        }


        if(imageUrl.contains(".gif")) {
            loadBuilder = Glide.with(view.getContext())
                    .load(imageUrl)
                    .skipMemoryCache(true);
        } else {
            loadBuilder = Glide.with(view.getContext())
                    .load(imageUrl);
        }

        if(placeHolder != null) {
            loadBuilder = loadBuilder.placeholder(placeHolder);
        }

        if(errorHolder != null) {
            loadBuilder = loadBuilder.error(errorHolder);
        }

        if(transform != null) {
            loadBuilder = loadBuilder.transform(transform);
        }

        if(format != null) {
            loadBuilder = loadBuilder.apply(RequestOptions.formatOf(format));
        }

        if(!noAnimate) {
            DrawableTransitionOptions transitionOptions = new DrawableTransitionOptions();
            transitionOptions.crossFade(new DrawableCrossFadeFactory.Builder(300).setCrossFadeEnabled(true));
            loadBuilder = loadBuilder.transition(transitionOptions);
        }
        if(target != null) {
            loadBuilder.into(target);
        } else {
            loadBuilder.into(view);
        }
    }
}
