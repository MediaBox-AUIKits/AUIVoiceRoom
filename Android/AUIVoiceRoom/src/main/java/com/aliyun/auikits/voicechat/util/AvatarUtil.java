package com.aliyun.auikits.voicechat.util;

import android.text.TextUtils;

public class AvatarUtil {


    public static String getAvatarUrl(String user_id) {
        String [] avatarArray = new String[] {
                "https://img.alicdn.com/imgextra/i3/O1CN01RScCaG1Ogg7EHqMU8_!!6000000001735-2-tps-174-174.png",
                "https://img.alicdn.com/imgextra/i1/O1CN01fwrnjZ1HbugVT1prp_!!6000000000777-2-tps-174-174.png",
                "https://img.alicdn.com/imgextra/i2/O1CN01Izsial1HimcrLB7hW_!!6000000000792-2-tps-174-174.png",
                "https://img.alicdn.com/imgextra/i2/O1CN011QCZqK1arvEDOqARU_!!6000000003384-2-tps-174-174.png",
                "https://img.alicdn.com/imgextra/i4/O1CN01nBP9CO22Cz4DJw50t_!!6000000007085-2-tps-174-174.png",
                "https://img.alicdn.com/imgextra/i4/O1CN01rgx31a1ZVBxNVVC7Q_!!6000000003199-2-tps-174-174.png",
                "https://img.alicdn.com/imgextra/i1/O1CN01p5nNVQ1eRavOtp5iU_!!6000000003868-2-tps-174-174.png",
                "https://img.alicdn.com/imgextra/i1/O1CN01puPUSh1wE0FtPOMtf_!!6000000006275-2-tps-174-174.png",
                "https://img.alicdn.com/imgextra/i2/O1CN01vqjRRH1V9t4PV8ORg_!!6000000002611-2-tps-174-174.png"
        };
        if(TextUtils.isEmpty(user_id)) {
            return avatarArray[0];
        }
        int firstLetter = user_id.charAt(0);
        int index = firstLetter % 9;

        return avatarArray[index];
    }
}
