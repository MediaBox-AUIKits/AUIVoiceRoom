package com.aliyuncs.aui.dto.res;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 房间信息DTO
 *
 * @author chunlei.zcl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomInfoDto {

    private String id;
    /**
     * 创建时间
     */
    @JsonProperty("created_at")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss",timezone = "GMT+8")
    private Date createdAt;
    /**
     * 修改时间
     */
    @JsonProperty("updated_at")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss",timezone = "GMT+8")
    private Date updatedAt;
    /**
     * 房间标题
     */
    private String title;
    /**
     * 房间创建者
     */
    private String anchor;

    /**
     * 房间状态：1-正常；2-解散
     */
    private Integer status;

    /**
     * 扩展信息
     */
    @JsonProperty("extends")
    private String extendsInfo;

    /**
     * 群组Id
     */
    @JsonProperty("chat_id")
    private String chatId;

    /**
     * 直播公告
     */
    private String notice;

    /**
     * 房间封面
     */
    @JsonProperty("cover_url")
    private String coverUrl;
    /**
     * 主播Id
     */
    @JsonProperty("anchor_id")
    private String anchorId;
    /**
     * 主播Nick
     */
    @JsonProperty("anchor_nick")
    private String anchorNick;

    /**
     * 连麦成员信息（json序列化）
     */
    private String meetingInfo;

    /**
     * 显示的号码，即房间号
     */
    @JsonProperty("show_code")
    private Integer showCode;

    /**
     * 开始时间
     */
    @JsonProperty("started_at")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss",timezone = "GMT+8")
    private Date startedAt;
    /**
     * 结束时间
     */
    @JsonProperty("stopped_at")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss",timezone = "GMT+8")
    private Date stoppedAt;
    @JsonProperty("metrics")
    private Metrics metrics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metrics {

        @JsonProperty("online_count")
        private Long onlineCount;

    }

}
