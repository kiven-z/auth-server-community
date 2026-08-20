package com.auth.service.system.message.model.query;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 站内信发送任务分页查询
 *
 * @author Bunny
 */
@Schema(name = "InAppSendTaskQuery", title = "站内信发送任务查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class InAppSendTaskQuery extends PageQueryRequest {

	@Schema(name = "status", title = "任务状态")
	private String status;

	@Schema(name = "sourceType", title = "来源")
	private String sourceType;

	@Schema(name = "recipientScopeType", title = "接收范围类型")
	private String recipientScopeType;

	@Schema(name = "categoryId", title = "业务小类 ID")
	private Long categoryId;

	@Schema(name = "title", title = "标题关键字")
	private String title;

}
