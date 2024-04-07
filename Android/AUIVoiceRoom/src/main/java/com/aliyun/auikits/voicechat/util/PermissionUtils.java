package com.aliyun.auikits.voicechat.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * 检查权限/权限数组
 * request权限
 */
public class PermissionUtils {

    public static final String[] PERMISSION_MANIFEST = {
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static final String[] PERMISSION_MANIFEST33 = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
    };

    public static String[] getPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return PERMISSION_MANIFEST;
        }
        return PERMISSION_MANIFEST33;
    }


}
