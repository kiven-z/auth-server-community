package com.auth.service.system.authorization.model.vo;

import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

/**
 * 授权失效 Outbox 分页行
 *
 * @author Bunny
 */
@Schema(name = "AuthorizationInvalidationOutboxPageVO", title = "授权失效 Outbox 分页行")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class AuthorizationInvalidationOutboxPageVO extends BaseResponse {

	@Schema(title = "业务事件 ID")
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

}
