package com.auth.service.auth.convert;

import com.auth.service.auth.model.po.invalidation.AuthorizationInvalidationEventPageRowPO;
import com.auth.service.auth.support.invalidation.InvalidationProcessingMarker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link AuthorizationInvalidationEventConverter} 单元测试
 */
@DisplayName("AuthorizationInvalidationEventConverter 幂等事件 PO→VO")
class AuthorizationInvalidationEventConverterTest {

	@Test
	@DisplayName("分页行 PO 应映射为分页 VO")
	void toPageVO_shouldMapFieldsAndProcessingFlag() {
		AuthorizationInvalidationEventPageRowPO row = new AuthorizationInvalidationEventPageRowPO();
		row.setId(1L);
		row.setEventId("evt-1");
		row.setChangeKind("ROLE");
		row.setImpactedUserCount(3);
		row.setProcessedAt(Instant.now());

		var pageVo = AuthorizationInvalidationEventConverter.INSTANCE.toPageVO(row);

		assertEquals(1L, pageVo.getId());
		assertEquals("evt-1", pageVo.getEventId());
		assertEquals("ROLE", pageVo.getChangeKind());
		assertEquals(3, pageVo.getImpactedUserCount());
		assertFalse(pageVo.getProcessing());
	}

	@Test
	@DisplayName("处理中占位计数应映射 processing=true")
	void toPageVO_shouldSetProcessingFlagForPlaceholderCount() {
		AuthorizationInvalidationEventPageRowPO row = new AuthorizationInvalidationEventPageRowPO();
		row.setImpactedUserCount(InvalidationProcessingMarker.PROCESSING_COUNT);

		var pageVo = AuthorizationInvalidationEventConverter.INSTANCE.toPageVO(row);

		assertTrue(pageVo.getProcessing());
	}

	@Test
	@DisplayName("详情行 PO 应映射备注字段")
	void toDetailVo_shouldMapRemark() {
		AuthorizationInvalidationEventPageRowPO row = new AuthorizationInvalidationEventPageRowPO();
		row.setEventId("evt-detail");
		row.setRemark("retry");

		var detailVo = AuthorizationInvalidationEventConverter.INSTANCE.toDetailVo(row);

		assertEquals("evt-detail", detailVo.getEventId());
		assertEquals("retry", detailVo.getRemark());
	}

}
