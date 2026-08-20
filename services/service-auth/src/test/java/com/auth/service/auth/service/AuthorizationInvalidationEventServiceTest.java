package com.auth.service.auth.service;

import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import com.auth.service.auth.mapper.AuthorizationInvalidationEventMapper;
import com.auth.service.auth.model.po.invalidation.AuthorizationInvalidationEventPO;
import com.auth.service.auth.model.po.invalidation.AuthorizationInvalidationEventPageRowPO;
import com.auth.service.auth.model.po.invalidation.AuthorizationInvalidationEventStatsPO;
import com.auth.service.auth.model.query.AuthorizationInvalidationEventQuery;
import com.auth.service.auth.model.value.invalidation.InvalidationIdempotencyGate;
import com.auth.service.auth.model.vo.authorization.AuthorizationInvalidationEventDetailVO;
import com.auth.service.auth.model.vo.authorization.AuthorizationInvalidationEventPageVO;
import com.auth.service.auth.model.vo.authorization.AuthorizationInvalidationEventStatsVO;
import com.auth.service.auth.service.impl.AuthorizationInvalidationEventServiceImpl;
import com.auth.service.auth.support.invalidation.InvalidationProcessingMarker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link AuthorizationInvalidationEventService} 单元测试
 */
@DisplayName("AuthorizationInvalidationEventService 幂等事件")
@ExtendWith(MockitoExtension.class)
class AuthorizationInvalidationEventServiceTest {

	@Mock
	private AuthorizationInvalidationEventMapper invalidationEventMapper;

	@InjectMocks
	private AuthorizationInvalidationEventServiceImpl eventService;

	private static AuthorizationInvalidationEventPageRowPO buildPageRow(Long id, String eventId,
			Integer impactedUserCount) {
		AuthorizationInvalidationEventPageRowPO row = new AuthorizationInvalidationEventPageRowPO();
		row.setId(id);
		row.setEventId(eventId);
		row.setChangeKind("ROLE");
		row.setImpactedUserCount(impactedUserCount);
		row.setProcessedAt(Instant.now());
		return row;
	}

	@Test
	@DisplayName("分页查询：应调用 Mapper 并返回分页 VO")
	void getPage_shouldQueryMapperAndReturnPageVo() {
		AuthorizationInvalidationEventQuery query = new AuthorizationInvalidationEventQuery();
		query.setPageIndex(1);
		query.setPageSize(10);
		query.setEventId("evt-prefix");
		query.setProcessing(true);

		AuthorizationInvalidationEventPageRowPO completedRow = buildPageRow(1L, "evt-1", 3);
		AuthorizationInvalidationEventPageRowPO processingRow = buildPageRow(2L, "evt-2",
				InvalidationProcessingMarker.PROCESSING_COUNT);

		Page<AuthorizationInvalidationEventPageRowPO> pageResult = new Page<>(1, 10, 2);
		pageResult.setRecords(List.of(completedRow, processingRow));
		when(invalidationEventMapper.selectListByPage(any(Page.class), any(AuthorizationInvalidationEventQuery.class)))
			.thenReturn(pageResult);

		PageResponse<AuthorizationInvalidationEventPageVO> response = eventService.getPage(query);

		assertEquals(2L, response.getTotal());
		assertEquals("evt-1", response.getList().get(0).getEventId());
		assertEquals(3, response.getList().get(0).getImpactedUserCount());
		assertFalse(response.getList().get(0).getProcessing());
		assertTrue(response.getList().get(1).getProcessing());

		ArgumentCaptor<AuthorizationInvalidationEventQuery> filterCaptor = ArgumentCaptor
			.forClass(AuthorizationInvalidationEventQuery.class);
		verify(invalidationEventMapper).selectListByPage(any(Page.class), filterCaptor.capture());
		assertEquals("evt-prefix", filterCaptor.getValue().getEventId());
		assertTrue(filterCaptor.getValue().getProcessing());
	}

	@Test
	@DisplayName("详情查询：记录不存在时抛出 DATA_NOT_EXIST")
	void getDetail_shouldThrowWhenMissing() {
		when(invalidationEventMapper.selectDetailById(99L)).thenReturn(null);

		AuthBusinessException exception = assertThrows(AuthBusinessException.class, () -> eventService.getDetail(99L));

		assertEquals(AuthResultCode.DATA_NOT_EXIST, exception.getResultCode());
	}

	@Test
	@DisplayName("详情查询：存在记录时返回详情 VO")
	void getDetail_shouldReturnDetailVo() {
		AuthorizationInvalidationEventPageRowPO row = new AuthorizationInvalidationEventPageRowPO();
		row.setId(10L);
		row.setEventId("evt-detail");
		row.setChangeKind("ROLE");
		row.setImpactedUserCount(5);
		row.setProcessedAt(Instant.now());
		when(invalidationEventMapper.selectDetailById(10L)).thenReturn(row);

		AuthorizationInvalidationEventDetailVO result = eventService.getDetail(10L);

		assertEquals("evt-detail", result.getEventId());
		assertEquals("ROLE", result.getChangeKind());
		assertEquals(5, result.getImpactedUserCount());
		assertFalse(result.getProcessing());
	}

	@Test
	@DisplayName("统计查询：无数据时各计数归零")
	void getStats_shouldReturnZeroWhenRowNull() {
		when(invalidationEventMapper.selectEventStats()).thenReturn(null);

		AuthorizationInvalidationEventStatsVO result = eventService.getStats();

		assertEquals(0L, result.getTotalCount());
		assertEquals(0L, result.getProcessingCount());
		assertEquals(0L, result.getCompletedCount());
	}

	@Test
	@DisplayName("统计查询：计数字段为空时归零")
	void getStats_shouldDefaultNullCountsToZero() {
		when(invalidationEventMapper.selectEventStats()).thenReturn(new AuthorizationInvalidationEventStatsPO());

		AuthorizationInvalidationEventStatsVO result = eventService.getStats();

		assertEquals(0L, result.getTotalCount());
		assertEquals(0L, result.getProcessingCount());
		assertEquals(0L, result.getCompletedCount());
	}

	@Test
	@DisplayName("统计查询：应调用 Mapper 并返回统计 VO")
	void getStats_shouldReturnEventStatsVo() {
		AuthorizationInvalidationEventStatsPO row = new AuthorizationInvalidationEventStatsPO();
		row.setTotalCount(12L);
		row.setProcessingCount(1L);
		row.setCompletedCount(11L);
		when(invalidationEventMapper.selectEventStats()).thenReturn(row);

		AuthorizationInvalidationEventStatsVO result = eventService.getStats();

		assertEquals(12L, result.getTotalCount());
		assertEquals(1L, result.getProcessingCount());
		assertEquals(11L, result.getCompletedCount());
		verify(invalidationEventMapper).selectEventStats();
	}

	@Test
	@DisplayName("已完成事件直接返回 Completed")
	void acquireGate_completedRow_shouldReturnCompleted() {
		AuthorizationInvalidationEventPO completed = new AuthorizationInvalidationEventPO();
		completed.setImpactedUserCount(10);
		completed.setVersionBumpedCount(8);
		completed.setProfileRefreshedCount(8);
		completed.setProfileEvictedCount(2);
		when(invalidationEventMapper.selectByEventId("evt-done")).thenReturn(completed);

		InvalidationIdempotencyGate gate = eventService.acquireGate("evt-done", AuthorizationChangeKind.ROLE);

		assertInstanceOf(InvalidationIdempotencyGate.Completed.class, gate);
		assertEquals(new AuthorizationInvalidateResponse(10, 8, 8, 2),
				((InvalidationIdempotencyGate.Completed) gate).response());
	}

	@Test
	@DisplayName("acquireGate 成功插入占位行")
	void acquireGate_insertSuccess_shouldReturnClaimed() {
		when(invalidationEventMapper.selectByEventId("evt-1")).thenReturn(null);
		when(invalidationEventMapper.insertProcessingClaim(any())).thenReturn(1);

		InvalidationIdempotencyGate gate = eventService.acquireGate("evt-1", AuthorizationChangeKind.ROLE);

		assertInstanceOf(InvalidationIdempotencyGate.Claimed.class, gate);
		ArgumentCaptor<AuthorizationInvalidationEventPO> captor = ArgumentCaptor
			.forClass(AuthorizationInvalidationEventPO.class);
		verify(invalidationEventMapper).insertProcessingClaim(captor.capture());
		assertEquals(InvalidationProcessingMarker.PROCESSING_COUNT, captor.getValue().getImpactedUserCount());
	}

	@Test
	@DisplayName("acquireGate 唯一键冲突且仍为处理中")
	void acquireGate_duplicateProcessing_shouldReturnInProgress() {
		when(invalidationEventMapper.selectByEventId("evt-2")).thenReturn(null);
		when(invalidationEventMapper.insertProcessingClaim(any())).thenThrow(new DuplicateKeyException("dup"));

		AuthorizationInvalidationEventPO processing = new AuthorizationInvalidationEventPO();
		processing.setImpactedUserCount(InvalidationProcessingMarker.PROCESSING_COUNT);
		when(invalidationEventMapper.selectByEventId("evt-2")).thenReturn(null, processing);

		InvalidationIdempotencyGate gate = eventService.acquireGate("evt-2", AuthorizationChangeKind.ROLE);

		assertInstanceOf(InvalidationIdempotencyGate.InProgress.class, gate);
	}

	@Test
	@DisplayName("acquireGate 唯一键冲突且对方已完成")
	void acquireGate_duplicateCompleted_shouldReturnCompleted() {
		AuthorizationInvalidationEventPO processing = new AuthorizationInvalidationEventPO();
		processing.setImpactedUserCount(InvalidationProcessingMarker.PROCESSING_COUNT);
		when(invalidationEventMapper.selectByEventId("evt-3")).thenReturn(processing);
		when(invalidationEventMapper.insertProcessingClaim(any())).thenThrow(new DuplicateKeyException("dup"));

		AuthorizationInvalidationEventPO completed = new AuthorizationInvalidationEventPO();
		completed.setImpactedUserCount(3);
		completed.setVersionBumpedCount(2);
		completed.setProfileRefreshedCount(2);
		completed.setProfileEvictedCount(1);
		when(invalidationEventMapper.selectByEventId("evt-3")).thenReturn(processing, completed);

		InvalidationIdempotencyGate gate = eventService.acquireGate("evt-3", AuthorizationChangeKind.ROLE);

		assertInstanceOf(InvalidationIdempotencyGate.Completed.class, gate);
		assertEquals(new AuthorizationInvalidateResponse(3, 2, 2, 1),
				((InvalidationIdempotencyGate.Completed) gate).response());
	}

	@Test
	@DisplayName("completeProcessedOutcome 更新占位行")
	void completeProcessedOutcome_shouldUpdateRow() {
		AuthorizationInvalidateResponse response = new AuthorizationInvalidateResponse(1, 1, 1, 0);
		when(invalidationEventMapper.updateProcessedOutcome(any())).thenReturn(1);

		eventService.completeProcessedOutcome("evt-4", AuthorizationChangeKind.ROLE, response);

		verify(invalidationEventMapper).updateProcessedOutcome(any());
	}

	@Test
	@DisplayName("无 processing 占位时不释放")
	void releaseProcessingClaim_shouldReturnFalseWhenNotProcessing() {
		AuthorizationInvalidationEventPO projection = new AuthorizationInvalidationEventPO();
		projection.setImpactedUserCount(3);
		when(invalidationEventMapper.selectByEventId("role:1")).thenReturn(projection);

		boolean released = eventService.releaseProcessingClaim("role:1");

		assertFalse(released);
		verify(invalidationEventMapper, never()).deleteProcessingClaim("role:1");
	}

	@Test
	@DisplayName("processing 占位时应删除占位行")
	void releaseProcessingClaim_shouldReleaseProcessingPlaceholder() {
		AuthorizationInvalidationEventPO projection = new AuthorizationInvalidationEventPO();
		projection.setImpactedUserCount(InvalidationProcessingMarker.PROCESSING_COUNT);
		when(invalidationEventMapper.selectByEventId("role:2")).thenReturn(projection);
		when(invalidationEventMapper.deleteProcessingClaim("role:2")).thenReturn(1);

		boolean released = eventService.releaseProcessingClaim("role:2");

		assertTrue(released);
		verify(invalidationEventMapper).deleteProcessingClaim("role:2");
	}

	@Test
	@DisplayName("批量释放超时 processing 占位")
	void cleanupStaleProcessingClaims_shouldDelegateToMapper() {
		Instant cutoff = LocalDateTime.of(2026, 1, 1, 0, 0).toInstant(java.time.ZoneOffset.UTC);
		when(invalidationEventMapper.deleteStaleProcessingClaims(cutoff, 50)).thenReturn(3);

		int deleted = eventService.cleanupStaleProcessingClaims(cutoff, 50);

		assertEquals(3, deleted);
		verify(invalidationEventMapper).deleteStaleProcessingClaims(cutoff, 50);
	}

	@Test
	@DisplayName("批量删除已完成过期幂等事件")
	void purgeCompletedBefore_shouldDelegateToMapper() {
		Instant cutoff = LocalDateTime.of(2025, 6, 1, 0, 0).toInstant(java.time.ZoneOffset.UTC);
		when(invalidationEventMapper.deleteCompletedBefore(cutoff, 200)).thenReturn(10);

		int deleted = eventService.purgeCompletedBefore(cutoff, 200);

		assertEquals(10, deleted);
		verify(invalidationEventMapper).deleteCompletedBefore(cutoff, 200);
	}

}
