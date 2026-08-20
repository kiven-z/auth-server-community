package com.auth.service.system.authorization.service.impl;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.common.web.resttemplate.FeignUtil;
import com.auth.service.system.authorization.convert.AuthorizationInvalidationEventOpsConverter;
import com.auth.service.system.authorization.convert.AuthorizationInvalidationStatsOpsConverter;
import com.auth.service.system.authorization.feign.AuthorizationInternalFeignClient;
import com.auth.service.system.authorization.feign.dto.AuthorizationInvalidationEventDetailInnerDTO;
import com.auth.service.system.authorization.feign.dto.AuthorizationInvalidationEventInnerQuery;
import com.auth.service.system.authorization.feign.dto.AuthorizationInvalidationEventPageInnerDTO;
import com.auth.service.system.authorization.model.po.AuthorizationInvalidationEventStatsPO;
import com.auth.service.system.authorization.model.query.AuthorizationInvalidationEventOpsQuery;
import com.auth.service.system.authorization.service.AuthorizationInvalidationEventOpsService;
import com.auth.service.system.common.exception.SystemBusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.auth.service.system.authorization.exception.AuthorizationInvalidationOpsResultCode.EVENT_RELEASE_NOT_PROCESSING;
import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;
import static com.auth.service.system.common.exception.code.SystemCommonResultCode.SERVICE_UNAVAILABLE;

/**
 * 授权失效幂等事件运维门面实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(readOnly = true)
public class AuthorizationInvalidationEventOpsServiceImpl implements AuthorizationInvalidationEventOpsService {

	private final AuthorizationInternalFeignClient authorizationInternalFeignClient;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<AuthorizationInvalidationEventPageInnerDTO> getPage(
			AuthorizationInvalidationEventOpsQuery query) {
		AuthorizationInvalidationEventInnerQuery innerQuery = AuthorizationInvalidationEventOpsConverter.INSTANCE
			.toInnerQuery(query);
		var result = authorizationInternalFeignClient.getInvalidationEventPage(innerQuery);

		if (!FeignUtil.isSuccessWithData(result)) {
			log.warn("Authorization invalidation event page unavailable");
			throw new SystemBusinessException(SERVICE_UNAVAILABLE);
		}
		return result.getData();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationInvalidationEventDetailInnerDTO getDetail(Long id) {
		var result = authorizationInternalFeignClient.getInvalidationEventDetail(id);

		if (!FeignUtil.isSuccessWithData(result)) {
			log.warn("Authorization invalidation event detail unavailable, id={}", id);
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}
		return result.getData();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationInvalidationEventStatsPO getEventStats() {
		var result = authorizationInternalFeignClient.getInvalidationEventSummary();
		if (!FeignUtil.isSuccessWithData(result)) {
			log.warn("Authorization invalidation event summary unavailable");
			throw new SystemBusinessException(SERVICE_UNAVAILABLE);
		}

		return AuthorizationInvalidationStatsOpsConverter.toEventStatsPo(result.getData());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean releaseProcessingClaim(Long id) {
		AuthorizationInvalidationEventDetailInnerDTO detail = getDetail(id);
		if (detail.getProcessing() != null && !detail.getProcessing()) {
			throw new SystemBusinessException(EVENT_RELEASE_NOT_PROCESSING);
		}

		Result<Boolean> result = authorizationInternalFeignClient.releaseInvalidationEventClaim(detail.getEventId());
		if (!FeignUtil.isSuccessWithData(result)) {
			log.warn("Authorization invalidation event release claim unavailable, id={}", id);
			throw new SystemBusinessException(SERVICE_UNAVAILABLE);
		}
		return result.getData() != null && result.getData();
	}

}
