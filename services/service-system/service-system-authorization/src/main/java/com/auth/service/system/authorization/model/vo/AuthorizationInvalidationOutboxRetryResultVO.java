package com.auth.service.system.authorization.model.vo;

import com.auth.common.core.annotation.JsonStringFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * 授权失效 Outbox 人工重试结果
 *
 * @author Bunny
 */
@Schema(name = "AuthorizationInvalidationOutboxRetryResultVO", title = "授权失效 Outbox 人工重试结果")
@Getter
@Setter
@ToString
public class AuthorizationInvalidationOutboxRetryResultVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonStringFormat
	@Schema(title = "Outbox 主键")
	private Long outboxId;

	@Schema(title = "业务事件 ID")
	private String eventId;

	@Schema(title = "重试前状态")
	private String previousStatus;

	@Schema(title = "当前状态")
	private String currentStatus;

	@Schema(title = "是否投递成功")
	private Boolean dispatched;

	@Schema(title = "是否释放了 auth 侧处理中占位")
	private Boolean claimReleased;

	@Schema(title = "最近失败原因")
	private String lastError;

}
