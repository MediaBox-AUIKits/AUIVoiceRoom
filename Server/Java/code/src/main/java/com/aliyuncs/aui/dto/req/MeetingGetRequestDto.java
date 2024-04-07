package com.aliyuncs.aui.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 获取连麦信息
 * @author chunlei.zcl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingGetRequestDto {

    @NotBlank(message="房间Id不能为空")
    private String id;

}
