package com.aliyuncs.aui.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 聊天室Entity
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("room_infos")
public class RoomInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@TableId(type = IdType.INPUT)
	private String id;
	/**
	 * 创建时间
	 */
	private Date createdAt;
	/**
	 * 修改时间
	 */
	private Date updatedAt;
	/**
	 * 聊天室标题
	 */
	private String title;
	/**
	 * 扩展信息
	 */
	@TableField("extends")
	private String extendsInfo;

	/**
	 * 群组Id
	 */
	private String chatId;
	/**
	 * 公告
	 */
	private String notice;
	/**
	 * 封面
	 */
	private String coverUrl;
	/**
	 * 主播Id
	 */
	private String anchorId;
	/**
	 * 主播Nick
	 */
	private String anchorNick;

	/**
	 * 状态 1： 开始， 2：结束
	 */
	private Integer status;
	/**
	 * 连麦成员信息（json序列化）
	 */
	private String meetingInfo;

	/**
	* 显示的号码，即房间号
	*/
	private Integer showCode;
	/**
	 * 开始时间
	 */
	private Date startedAt;
	/**
	 * 结束时间
	 */
	private Date stoppedAt;

}
