package com.auth.service.system.authorization.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.time.Instant;

/**
 * 授权失效 Outbox
 *
 * @author Bunny
 */
@TableName("sys_authorization_invalidation_outbox")
@Getter
@Setter
public class SysAuthorizationInvalidationOutboxEntity extends BaseEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "业务幂等键")
	private String eventId;

	@Schema(title = "授权变更维度枚举名")
	private String changeKind;

	@Schema(title = "授权失效业务键的 JSON")
	private String payload;

	@Schema(title = "投递状态：PENDING / PROCESSING / SUCCESS / FAILED / DEAD")
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

	@Schema(title = "触发模块，如 SYS_ROLE。")
	private String sourceModule;

	@Schema(title = "追踪号，如 update:a3f2c1b0；完整业务键在 payload")
	private String sourceBizId;

}
