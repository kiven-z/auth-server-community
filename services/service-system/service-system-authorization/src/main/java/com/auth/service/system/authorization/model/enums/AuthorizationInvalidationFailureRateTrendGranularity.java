package com.auth.service.system.authorization.model.enums;

/**
 * 授权失效失败率趋势聚合粒度
 *
 * @author Bunny
 */
public enum AuthorizationInvalidationFailureRateTrendGranularity {

	/**
	 * 按自然日聚合
	 */
	DAY,

	/**
	 * 按自然周聚合（周一至周日，桶为当周周一日期）
	 */
	WEEK

}
