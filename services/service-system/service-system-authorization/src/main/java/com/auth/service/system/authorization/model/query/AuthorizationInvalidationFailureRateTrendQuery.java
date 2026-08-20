package com.auth.service.system.authorization.model.query;

import com.auth.service.system.authorization.model.enums.AuthorizationInvalidationFailureRateTrendGranularity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 授权失效失败率趋势查询条件
 *
 * @author Bunny
 */
@Getter
@Setter
public class AuthorizationInvalidationFailureRateTrendQuery implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "聚合粒度（DAY|WEEK）", defaultValue = "DAY")
	@NotNull(message = "聚合粒度不能为空")
	private AuthorizationInvalidationFailureRateTrendGranularity granularity = AuthorizationInvalidationFailureRateTrendGranularity.DAY;

	@Schema(title = "统计开始时间（与 endTime 成对使用）")
	private Instant startTime;

	@Schema(title = "统计结束时间（与 startTime 成对使用）")
	private Instant endTime;

	@Schema(title = "最近天数（未指定起止时间时生效）", defaultValue = "30")
	@Max(366)
	@Min(1)
	private Integer days = 30;

	/**
	 * 起止时间须同时指定或同时省略
	 */
	@AssertTrue(message = "起止时间必须成对指定")
	public boolean isTimeRangeValid() {
		return (startTime == null && endTime == null) || (startTime != null && endTime != null);
	}

}
