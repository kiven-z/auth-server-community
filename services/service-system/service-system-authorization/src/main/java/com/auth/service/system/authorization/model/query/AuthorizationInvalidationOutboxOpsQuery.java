package com.auth.service.system.authorization.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 授权失效 Outbox 运维查询条件
 *
 * @author Bunny
 */
@Getter
@Setter
public class AuthorizationInvalidationOutboxOpsQuery implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "页码")
	private long pageIndex;

	@Schema(title = "每页条数")
	private long pageSize;

	@Schema(title = "业务事件 ID")
	private String eventId;

	@Schema(title = "变更维度（AuthorizationChangeKind）")
	private String changeKind;

	@Schema(title = "投递状态（AuthorizationInvalidationOutboxStatus）")
	private String status;

	@Schema(title = "触发模块")
	private String sourceModule;

	@Schema(title = "业务主键")
	private String sourceBizId;

	@Schema(title = "创建时间（开始）")
	private Instant createdAtStart;

	@Schema(title = "创建时间（结束）")
	private Instant createdAtEnd;

	@Schema(title = "处理完成时间（开始）")
	private Instant processedAtStart;

	@Schema(title = "处理完成时间（结束）")
	private Instant processedAtEnd;

}
