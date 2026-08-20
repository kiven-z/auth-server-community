package com.auth.service.system.authorization.model.po;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 授权失效 Outbox 分页行
 *
 * @author Bunny
 */
@Schema(name = "AuthorizationInvalidationOutboxPageRowPO", title = "授权失效 Outbox 分页行")
@Getter
@Setter
public class AuthorizationInvalidationOutboxPageRowPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "主键")
	private Long id;

	@Schema(title = "业务事件 ID（幂等键）")
	private String eventId;

	@Schema(title = "变更维度")
	private String changeKind;

	@Schema(title = "投递状态")
	private String status;

	@Schema(title = "已重试次数")
	private Integer retryCount;

	@Schema(title = "最大重试次数")
	private Integer maxRetry;

	@Schema(title = "下次重试时间")
	private Instant nextRetryAt;

	@Schema(title = "最近失败原因")
	private String lastError;

	@Schema(title = "抢占实例标识")
	private String lockedBy;

	@Schema(title = "抢占时间")
	private Instant lockedAt;

	@Schema(title = "处理完成时间")
	private Instant processedAt;

	@Schema(title = "触发模块")
	private String sourceModule;

	@Schema(title = "业务主键")
	private String sourceBizId;

	@Schema(title = "创建时间")
	private Instant createdAt;

	@Schema(title = "更新时间")
	private Instant updatedAt;

	@Schema(title = "创建用户")
	private Long createdBy;

	@Schema(title = "更新用户")
	private Long updatedBy;

}
