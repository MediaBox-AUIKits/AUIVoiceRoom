package com.aliyuncs.aui.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 连麦成员信息
 *
 * @author chunlei.zcl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingMemberInfo {

    public static final int MAX_MIC = 8;

    /**
     * 麦位
     */
    private Integer index;

    /**
     * 是否在线
     */
    private Boolean joined;
    /**
    * 用户Id
    */
    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("extends")
    private String extendsInfo;

    @JsonProperty("join_time")
    private Long joinTime;

    public static Members init() {

        List<MeetingMemberInfo> members = new ArrayList<>();
        for (int i = 1; i <= MAX_MIC; i++) {
            MeetingMemberInfo meetingMemberInfo = MeetingMemberInfo.builder()
                    .index(i)
                    .joined(false)
                    .userId("")
                    .build();
            members.add(meetingMemberInfo);
        }
        return Members.builder().members(members).build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Members {
        private List<MeetingMemberInfo> members;
    }
}
