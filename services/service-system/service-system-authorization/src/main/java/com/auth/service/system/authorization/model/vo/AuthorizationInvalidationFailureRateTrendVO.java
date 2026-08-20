package com.auth.service.system.authorization.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * 授权失效失败率趋势
 *
 * @author Bunny
 */
@Schema(name = "AuthorizationInvalidationFailureRateTrendVO", title = "授权失效失败率趋势")
@Getter
@Setter
@ToString
public class AuthorizationInvalidationFailureRateTrendVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "聚合粒度（DAY|WEEK）")
	private String granularity;

	@Schema(title = "统计开始时间")
	private Instant startTime;

	@Schema(title = "统计结束时间")
	private Instant endTime;

	@Schema(title = "趋势数据点")
	private List<AuthorizationInvalidationFailureRateTrendPointVO> points;

}
