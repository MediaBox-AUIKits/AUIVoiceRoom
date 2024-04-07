package com.aliyun.auikits.voicechat.util;

import com.github.javafaker.Faker;

public class UserFaker {
    private String name;
    private String avatarUrl;

    public String getName() {
        return name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public static UserFaker generateFakeUser() {
        //随机生成名字
        Faker nameFaker = new Faker();

        final String profileName = nameFaker.name().firstName() + nameFaker.number().randomDigit();

        UserFaker userFaker = new UserFaker();
        userFaker.avatarUrl = AvatarUtil.getAvatarUrl(profileName);
        userFaker.name = profileName;

        return userFaker;
    }
}
