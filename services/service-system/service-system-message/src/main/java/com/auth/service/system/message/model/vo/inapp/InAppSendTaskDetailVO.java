package com.auth.service.system.message.model.vo.inapp;

import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.common.core.model.response.BaseResponse;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.List;

/**
 * 站内信发送任务详情返回对象
 *
 * @author Bunny
 */
@Schema(name = "InAppSendTaskDetailVO", title = "站内信发送任务详情返回对象")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class InAppSendTaskDetailVO extends BaseResponse {

	@Schema(name = "sourceType", title = "来源")
	private String sourceType;

	@Schema(name = "sceneCode", title = "场景编码")
	private String sceneCode;

	@Schema(name = "title", title = "标题")
	private String title;

	@Schema(name = "contentType", title = "正文类型")
	private String contentType;

	@Schema(name = "content", title = "正文")
	private String content;

	@JsonStringFormat
	@Schema(name = "categoryId", title = "业务小类 ID")
	private Long categoryId;

	@Schema(name = "categoryName", title = "业务小类名称")
	private String categoryName;

	@Schema(name = "linkUrl", title = "跳转链接")
	private String linkUrl;

	@JsonStringFormat
	@Schema(name = "senderUserId", title = "发起人用户 ID")
	private Long senderUserId;

	@Schema(name = "recipientScopeType", title = "接收范围类型")
	private String recipientScopeType;

	@Schema(name = "recipientScopeJson", title = "接收范围快照 JSON")
	private String recipientScopeJson;

	@JsonSerialize(contentUsing = ToStringSerializer.class)
	@Schema(name = "recipientScopeIds", title = "范围 ID 列表")
	private List<Long> recipientScopeIds;

	@Schema(name = "includeChildren", title = "部门是否包含子部门")
	private Boolean includeChildren;

	@Schema(name = "totalCount", title = "展开后目标总数")
	private Integer totalCount;

	@Schema(name = "successCount", title = "投递成功数")
	private Integer successCount;

	@Schema(name = "failCount", title = "投递失败数")
	private Integer failCount;

	@Schema(name = "status", title = "任务状态")
	private String status;

	@Schema(name = "recalledAt", title = "撤回时间")
	private Instant recalledAt;

	@JsonStringFormat
	@Schema(name = "recallUserId", title = "撤回操作人用户 ID")
	private Long recallUserId;

	@Schema(name = "remark", title = "备注")
	private String remark;

}
