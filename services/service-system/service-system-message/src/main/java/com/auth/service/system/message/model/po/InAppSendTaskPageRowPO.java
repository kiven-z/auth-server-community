package com.auth.service.system.message.model.po;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 站内信发送任务分页行
 *
 * @author Bunny
 */
@Schema(name = "InAppSendTaskPageRowPO", title = "站内信发送任务分页行")
@Getter
@Setter
@ToString
public class InAppSendTaskPageRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "主键")
	private Long id;

	@Schema(title = "来源")
	private String sourceType;

	@Schema(title = "场景编码")
	private String sceneCode;

	@Schema(title = "标题")
	private String title;

	@Schema(title = "正文类型")
	private String contentType;

	@Schema(title = "业务小类 ID")
	private Long categoryId;

	@Schema(title = "业务小类名称")
	private String categoryName;

	@Schema(title = "发起人用户 ID")
	private Long senderUserId;

	@Schema(title = "接收范围类型")
	private String recipientScopeType;

	@Schema(title = "展开后目标总数")
	private Integer totalCount;

	@Schema(title = "投递成功数")
	private Integer successCount;

	@Schema(title = "投递失败数")
	private Integer failCount;

	@Schema(title = "任务状态")
	private String status;

	@Schema(title = "撤回时间")
	private Instant recalledAt;

	@Schema(title = "创建时间")
	private Instant createdAt;

	@Schema(title = "更新时间")
	private Instant updatedAt;

	@Schema(title = "创建用户")
	private Long createdBy;

	@Schema(title = "更新用户")
	private Long updatedBy;

}
