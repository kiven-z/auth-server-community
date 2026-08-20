package com.auth.service.system.authorization.service.impl;

import com.auth.service.system.authorization.convert.AuthorizationInvalidationStatsOpsConverter;
import com.auth.service.system.authorization.mapper.AuthorizationInvalidationOutboxOpsMapper;
import com.auth.service.system.authorization.model.po.AuthorizationInvalidationFailureRateTrendBucketPO;
import com.auth.service.system.authorization.model.query.AuthorizationInvalidationFailureRateTrendQuery;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationFailureRateTrendPointVO;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationFailureRateTrendVO;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationSummaryVO;
import com.auth.service.system.authorization.service.AuthorizationInvalidationEventOpsService;
import com.auth.service.system.authorization.service.AuthorizationInvalidationOutboxOpsService;
import com.auth.service.system.authorization.service.AuthorizationInvalidationStatsQueryService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * 授权失效运维统计查询服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AuthorizationInvalidationStatsQueryServiceImpl implements AuthorizationInvalidationStatsQueryService {

	private final AuthorizationInvalidationOutboxOpsService authorizationInvalidationOutboxOpsService;

	private final AuthorizationInvalidationEventOpsService authorizationInvalidationEventOpsService;

	private final AuthorizationInvalidationOutboxOpsMapper authorizationInvalidationOutboxOpsMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationInvalidationSummaryVO getSummary() {
		AuthorizationInvalidationSummaryVO summaryVO = new AuthorizationInvalidationSummaryVO();
		summaryVO.setOutbox(AuthorizationInvalidationStatsOpsConverter
			.toOutboxStatsVo(authorizationInvalidationOutboxOpsService.getOutboxStats()));
		summaryVO.setEvent(AuthorizationInvalidationStatsOpsConverter
			.toEventStatsVo(authorizationInvalidationEventOpsService.getEventStats()));
		return summaryVO;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationInvalidationFailureRateTrendVO getFailureRateTrend(
			@NotNull AuthorizationInvalidationFailureRateTrendQuery query) {
		if (query.getStartTime() == null && query.getEndTime() == null) {
			LocalDate endDate = LocalDate.now(ZoneOffset.UTC);
			LocalDate startDate = endDate.minusDays(query.getDays() - 1L);
			query.setStartTime(startDate.atStartOfDay(ZoneOffset.UTC).toInstant());
			query.setEndTime(endDate.atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC));
		}

		AuthorizationInvalidationFailureRateTrendVO trendVo = new AuthorizationInvalidationFailureRateTrendVO();
		trendVo.setGranularity(query.getGranularity().name());
		trendVo.setStartTime(query.getStartTime());
		trendVo.setEndTime(query.getEndTime());

		List<AuthorizationInvalidationFailureRateTrendBucketPO> buckets = authorizationInvalidationOutboxOpsMapper
			.selectFailureRateTrendBuckets(query);
		List<AuthorizationInvalidationFailureRateTrendPointVO> list = buckets.stream()
			.map(AuthorizationInvalidationStatsOpsConverter::toFailureRateTrendPointVo)
			.toList();
		trendVo.setPoints(list);
		return trendVo;
	}

}
