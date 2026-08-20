package com.auth.service.system.authorization.service;

import com.auth.service.system.authorization.model.query.AuthorizationInvalidationFailureRateTrendQuery;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationFailureRateTrendVO;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationSummaryVO;

/**
 * 授权失效运维统计查询服务
 *
 * @author Bunny
 */
public interface AuthorizationInvalidationStatsQueryService {

	/**
	 * 查询 Outbox 与幂等事件统计摘要
	 * @return 统计摘要
	 */
	AuthorizationInvalidationSummaryVO getSummary();

	/**
	 * 查询 Outbox 失败率趋势
	 * @param query 趋势查询条件
	 * @return 失败率趋势
	 */
	AuthorizationInvalidationFailureRateTrendVO getFailureRateTrend(
			AuthorizationInvalidationFailureRateTrendQuery query);

}
