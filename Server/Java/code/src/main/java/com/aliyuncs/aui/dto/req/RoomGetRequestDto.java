package com.aliyuncs.aui.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 获取房间信息
 * @author chunlei.zcl
 */
@Data
public class RoomGetRequestDto {
    @NotBlank(message="直播间Id不能为空")
    private String id;

    @ApiModelProperty(value = "UserId")
    @JsonProperty("user_id")
    private String userId;
}
