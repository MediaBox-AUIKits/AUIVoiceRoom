package com.aliyuncs.aui.dto.req;

import com.aliyuncs.aui.dto.MeetingMemberInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;

/**
 * 上麦
 * @author chunlei.zcl
 */
@Data
@Slf4j
public class JoinMicRequestDto {

    @NotBlank(message="房间Id不能为空")
    private String id;

    @NotBlank(message="userId不能为空")
    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("index")
    private Integer index;

    @JsonProperty("extends")
    private String extendsInfo;

    public boolean valid() {
        if (index != null && index > MeetingMemberInfo.MAX_MIC) {
            log.warn("invalid index. index must less than {}", MeetingMemberInfo.MAX_MIC);
            return false;
        }
        return true;
    }

}
