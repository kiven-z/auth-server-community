package com.auth.service.system.message.model.query;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 站内信发送任务收件人分页查询
 *
 * @author Bunny
 */
@Schema(name = "InAppSendTaskRecipientQuery", title = "站内信发送任务收件人查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class InAppSendTaskRecipientQuery extends PageQueryRequest {

	@Schema(name = "userId", title = "接收人用户 ID")
	private Long userId;

	@Schema(name = "isRead", title = "是否已读")
	private Boolean isRead;

	@Schema(name = "isDeleted", title = "用户侧是否软删除")
	private Boolean isDeleted;

}
