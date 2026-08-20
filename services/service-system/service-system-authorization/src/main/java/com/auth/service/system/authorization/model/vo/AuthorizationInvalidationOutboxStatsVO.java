package com.auth.service.system.authorization.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 授权失效 Outbox 状态统计 VO
 *
 * @author Bunny
 */
@Schema(name = "AuthorizationInvalidationOutboxStatsVO", title = "授权失效 Outbox 统计")
@Getter
@Setter
@ToString
public class AuthorizationInvalidationOutboxStatsVO implements Serializable {

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

	@Schema(title = "失败率（DEAD + FAILED / 总数，百分比）")
	private BigDecimal failureRatePercent;

}
