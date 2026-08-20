package com.auth.service.system.authorization.feign.fallback;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;
import com.auth.service.system.authorization.feign.AuthorizationInternalFeignClient;
import com.auth.service.system.authorization.feign.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 授权域内部 Feign 降级
 *
 * @author Bunny
 */
@Slf4j
@Component
public class AuthorizationInternalFeignClientFallback implements AuthorizationInternalFeignClient {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result<EffectiveCodesInnerDTO> getEffectiveCodes(Long userId) {
		log.error("AuthorizationInternalFeignClientFallback: getEffectiveCodes failed, userId={}", userId);
		return Result.success(new EffectiveCodesInnerDTO());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result<AuthorizationInvalidateResponse> invalidate(AuthorizationInvalidateRequest request) {
		String eventId = request != null ? request.eventId() : null;
		log.error("AuthorizationInternalFeignClientFallback: invalidate failed, eventId={}", eventId);
		return Result.error("Authorization invalidation service is unavailable");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result<PageResponse<AuthorizationInvalidationEventPageInnerDTO>> getInvalidationEventPage(
			AuthorizationInvalidationEventInnerQuery query) {
		log.error("AuthorizationInternalFeignClientFallback: getInvalidationEventPage failed");
		return Result.error("Authorization invalidation event query is unavailable");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result<AuthorizationInvalidationEventDetailInnerDTO> getInvalidationEventDetail(Long id) {
		log.error("AuthorizationInternalFeignClientFallback: getInvalidationEventDetail failed, id={}", id);
		return Result.error("Authorization invalidation event query is unavailable");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result<AuthorizationInvalidationEventStatsInnerDTO> getInvalidationEventSummary() {
		log.error("AuthorizationInternalFeignClientFallback: getInvalidationEventSummary failed");
		return Result.error("Authorization invalidation event summary is unavailable");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result<Boolean> releaseInvalidationEventClaim(String eventId) {
		log.error("AuthorizationInternalFeignClientFallback: releaseInvalidationEventClaim failed, eventId={}",
				eventId);
		return Result.error("Authorization invalidation event release claim is unavailable");
	}

}
