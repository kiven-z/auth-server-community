package com.auth.service.auth.convert;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;
import com.auth.service.auth.model.po.invalidation.AuthorizationInvalidationEventPO;
import com.auth.service.auth.support.invalidation.InvalidationProcessingMarker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link AuthorizationInvalidationEventMappingSupport} 写侧投影组装单元测试
 */
@DisplayName("AuthorizationInvalidationEventMappingSupport 写侧映射")
class AuthorizationInvalidationEventMappingSupportTest {

	@Test
	@DisplayName("buildProcessingClaim 生成处理中占位投影")
	void buildProcessingClaim_shouldSetProcessingMarkers() {
		AuthorizationInvalidationEventPO projection = AuthorizationInvalidationEventMappingSupport
			.buildProcessingClaim("evt-1", AuthorizationChangeKind.ROLE);

		assertEquals("evt-1", projection.getEventId());
		assertEquals(AuthorizationChangeKind.ROLE.name(), projection.getChangeKind());
		assertEquals(InvalidationProcessingMarker.PROCESSING_COUNT, projection.getImpactedUserCount());
		assertEquals(InvalidationProcessingMarker.PROCESSING_COUNT, projection.getVersionBumpedCount());
		assertNotNull(projection.getId());
		assertNotNull(projection.getProcessedAt());
	}

	@Test
	@DisplayName("toResponse 将投影转为失效结果 DTO")
	void toResponse_shouldMapCountsWithDefaults() {
		AuthorizationInvalidationEventPO projection = new AuthorizationInvalidationEventPO();
		projection.setImpactedUserCount(5);
		projection.setVersionBumpedCount(4);
		projection.setProfileRefreshedCount(3);
		projection.setProfileEvictedCount(2);

		AuthorizationInvalidateResponse response = AuthorizationInvalidationEventMappingSupport.toResponse(projection);

		assertEquals(new AuthorizationInvalidateResponse(5, 4, 3, 2), response);
	}

	@Test
	@DisplayName("toResponse 空计数默认为零")
	void toResponse_shouldDefaultNullCountsToZero() {
		AuthorizationInvalidationEventPO projection = new AuthorizationInvalidationEventPO();

		AuthorizationInvalidateResponse response = AuthorizationInvalidationEventMappingSupport.toResponse(projection);

		assertEquals(new AuthorizationInvalidateResponse(0, 0, 0, 0), response);
	}

	@Test
	@DisplayName("toProcessedOutcomeProjection 由结果 DTO 构建完成态投影")
	void toProcessedOutcomeProjection_shouldMapResponseFields() {
		AuthorizationInvalidateResponse response = new AuthorizationInvalidateResponse(1, 1, 1, 0);

		AuthorizationInvalidationEventPO projection = AuthorizationInvalidationEventMappingSupport
			.toProcessedOutcomeProjection("evt-2", AuthorizationChangeKind.USER, response);

		assertEquals("evt-2", projection.getEventId());
		assertEquals(AuthorizationChangeKind.USER.name(), projection.getChangeKind());
		assertEquals(1, projection.getImpactedUserCount());
		assertEquals(1, projection.getVersionBumpedCount());
		assertEquals(1, projection.getProfileRefreshedCount());
		assertEquals(0, projection.getProfileEvictedCount());
		assertNotNull(projection.getProcessedAt());
	}

	@Test
	@DisplayName("toProcessingFlag 识别处理中占位")
	void toProcessingFlag_shouldDetectProcessingCount() {
		assertTrue(AuthorizationInvalidationEventMappingSupport
			.toProcessingFlag(InvalidationProcessingMarker.PROCESSING_COUNT));
	}

}
