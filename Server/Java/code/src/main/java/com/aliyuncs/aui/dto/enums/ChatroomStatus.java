package com.aliyuncs.aui.dto.enums;

/**
 * 聊天室状态
 *
 * @author chunlei.zcl
 */
public enum ChatroomStatus {

    /**
     * 已开始
     * @author chunlei.zcl
     */
    StatusOn(1),

    /**
     * 已结束
     * @author chunlei.zcl
     */
    StatusOff(2);

    private int val;

    public static ChatroomStatus of(int val) {

        for (ChatroomStatus value : ChatroomStatus.values()) {
            if (val == value.getVal()) {
                return value;
            }
        }
        return null;
    }

    ChatroomStatus(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

}
