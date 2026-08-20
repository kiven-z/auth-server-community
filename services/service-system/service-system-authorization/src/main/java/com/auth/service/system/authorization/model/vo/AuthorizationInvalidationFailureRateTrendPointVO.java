package com.auth.service.system.authorization.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 授权失效失败率趋势数据点
 *
 * @author Bunny
 */
@Schema(name = "AuthorizationInvalidationFailureRateTrendPointVO", title = "授权失效失败率趋势点")
@Getter
@Setter
@ToString
public class AuthorizationInvalidationFailureRateTrendPointVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "时间桶（日期字符串）")
	private String bucket;

	@Schema(title = "总记录数")
	private Long totalCount;

	@Schema(title = "待重试记录数")
	private Long failedCount;

	@Schema(title = "死信记录数")
	private Long deadCount;

	@Schema(title = "失败率（DEAD + FAILED / 总数，百分比）")
	private BigDecimal failureRatePercent;

}
