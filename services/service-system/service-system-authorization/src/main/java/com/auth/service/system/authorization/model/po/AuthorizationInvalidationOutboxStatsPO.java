package com.auth.service.system.authorization.model.po;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 授权失效 Outbox 状态统计 PO
 *
 * @author Bunny
 */
@Getter
@Setter
public class AuthorizationInvalidationOutboxStatsPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "总记录数")
	private Long totalCount;

	@Schema(title = "待投递记录数")
	private Long pendingCount;

	@Schema(title = "处理中记录数")
	private Long processingCount;

	@Schema(title = "已成功记录数")
	private Long successCount;

	@Schema(title = "待重试记录数")
	private Long failedCount;

	@Schema(title = "死信记录数")
	private Long deadCount;

}
