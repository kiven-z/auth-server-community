package com.auth.service.auth.service;

import com.auth.common.core.constants.BatchSizes;
import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;
import com.auth.module.security.contract.dto.invalidation.RoleInvalidatePayload;
import com.auth.service.auth.model.po.user.UserInvalidationStatePO;
import com.auth.service.auth.model.value.invalidation.InvalidationIdempotencyGate;
import com.auth.service.auth.service.impl.AuthorizationInvalidationServiceImpl;
import com.auth.service.auth.support.invalidation.AuthProfileMaterializationService;
import com.auth.service.auth.support.invalidation.UserInvalidationRepository;
import com.auth.service.auth.support.invalidation.impact.ImpactResolverRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link AuthorizationInvalidationService} 单元测试
 */
@DisplayName("AuthorizationInvalidationService 授权失效编排")
@ExtendWith(MockitoExtension.class)
class AuthorizationInvalidationServiceTest {

	@Mock
	private AuthorizationInvalidationEventService authorizationInvalidationEventService;

	@Mock
	private ImpactResolverRegistry impactResolverRegistry;

	@Mock
	private UserInvalidationRepository userInvalidationRepository;

	@Mock
	private AuthProfileMaterializationService authProfileMaterializationService;

	@InjectMocks
	private AuthorizationInvalidationServiceImpl authorizationInvalidationService;

	private static UserInvalidationStatePO activeState() {
		UserInvalidationStatePO state = new UserInvalidationStatePO();
		state.setUserId(1L);
		state.setStatus(1);
		return state;
	}

	@Test
	@DisplayName("幂等命中时直接返回缓存结果")
	void invalidate_idempotentHit_shouldReturnCached() {
		AuthorizationInvalidateResponse cached = new AuthorizationInvalidateResponse(10, 8, 8, 2);
		when(authorizationInvalidationEventService.acquireGate("evt-1", AuthorizationChangeKind.ROLE))
			.thenReturn(InvalidationIdempotencyGate.Completed.builder().response(cached).build());

		AuthorizationInvalidateRequest request = new AuthorizationInvalidateRequest("evt-1",
				AuthorizationChangeKind.ROLE, new RoleInvalidatePayload(List.of("ADMIN")));
		AuthorizationInvalidateResponse response = authorizationInvalidationService.invalidate(request);

		assertEquals(cached, response);
		verify(impactResolverRegistry, never()).resolve(any());
	}

	@Test
	@DisplayName("无影响用户时返回全零并完成幂等占位")
	void invalidate_emptyImpact_shouldReturnEmpty() {
		when(authorizationInvalidationEventService.acquireGate("evt-2", AuthorizationChangeKind.ROLE))
			.thenReturn(InvalidationIdempotencyGate.Claimed.builder().build());
		when(impactResolverRegistry.resolve(any())).thenReturn(Set.of());

		AuthorizationInvalidateRequest request = new AuthorizationInvalidateRequest("evt-2",
				AuthorizationChangeKind.ROLE, new RoleInvalidatePayload(List.of("ADMIN")));
		AuthorizationInvalidateResponse response = authorizationInvalidationService.invalidate(request);

		assertEquals(AuthorizationInvalidateResponse.empty(), response);
		verify(authorizationInvalidationEventService).completeProcessedOutcome("evt-2", AuthorizationChangeKind.ROLE,
				AuthorizationInvalidateResponse.empty());
	}

	@Test
	@DisplayName("先递增 perm_version 再刷新画像")
	void invalidate_shouldIncrementBeforeRefresh() {
		when(authorizationInvalidationEventService.acquireGate("evt-3", AuthorizationChangeKind.ROLE))
			.thenReturn(InvalidationIdempotencyGate.Claimed.builder().build());
		when(impactResolverRegistry.resolve(any())).thenReturn(Set.of(1L));
		when(userInvalidationRepository.loadByUserIds(Set.of(1L))).thenReturn(List.of(activeState()));
		when(userInvalidationRepository.incrementPermVersionInBatches(Set.of(1L), BatchSizes.SIZE_500)).thenReturn(1);
		when(authProfileMaterializationService.refreshInBatches(Set.of(1L), BatchSizes.SIZE_500)).thenReturn(1);
		when(authProfileMaterializationService.evictInBatches(Set.of(), BatchSizes.SIZE_500)).thenReturn(0);

		AuthorizationInvalidateRequest request = new AuthorizationInvalidateRequest("evt-3",
				AuthorizationChangeKind.ROLE, new RoleInvalidatePayload(List.of("ADMIN")));
		AuthorizationInvalidateResponse response = authorizationInvalidationService.invalidate(request);

		assertEquals(new AuthorizationInvalidateResponse(1, 1, 1, 0), response);

		InOrder order = inOrder(userInvalidationRepository, authProfileMaterializationService);
		order.verify(userInvalidationRepository).incrementPermVersionInBatches(Set.of(1L), BatchSizes.SIZE_500);
		order.verify(authProfileMaterializationService).refreshInBatches(Set.of(1L), BatchSizes.SIZE_500);
		verify(authorizationInvalidationEventService).completeProcessedOutcome("evt-3", AuthorizationChangeKind.ROLE,
				new AuthorizationInvalidateResponse(1, 1, 1, 0));
	}

	@Test
	@DisplayName("库中无行时仅驱逐画像")
	void invalidate_missingUserRow_shouldEvictOnly() {
		when(authorizationInvalidationEventService.acquireGate("evt-4", AuthorizationChangeKind.ROLE))
			.thenReturn(InvalidationIdempotencyGate.Claimed.builder().build());
		when(impactResolverRegistry.resolve(any())).thenReturn(Set.of(99L));
		when(userInvalidationRepository.loadByUserIds(Set.of(99L))).thenReturn(List.of());
		when(userInvalidationRepository.incrementPermVersionInBatches(Set.of(), BatchSizes.SIZE_500)).thenReturn(0);
		when(authProfileMaterializationService.refreshInBatches(Set.of(), BatchSizes.SIZE_500)).thenReturn(0);
		when(authProfileMaterializationService.evictInBatches(Set.of(99L), BatchSizes.SIZE_500)).thenReturn(1);

		AuthorizationInvalidateRequest request = new AuthorizationInvalidateRequest("evt-4",
				AuthorizationChangeKind.ROLE, new RoleInvalidatePayload(List.of("ADMIN")));
		AuthorizationInvalidateResponse response = authorizationInvalidationService.invalidate(request);

		assertEquals(new AuthorizationInvalidateResponse(1, 0, 0, 1), response);
		verify(userInvalidationRepository).incrementPermVersionInBatches(Set.of(), BatchSizes.SIZE_500);
		verify(authProfileMaterializationService).refreshInBatches(Set.of(), BatchSizes.SIZE_500);
		verify(authProfileMaterializationService).evictInBatches(Set.of(99L), BatchSizes.SIZE_500);
	}

	@Test
	@DisplayName("有行与无行混合时分别升版本与驱逐")
	void invalidate_mixedImpact_shouldSplitByPresence() {
		when(authorizationInvalidationEventService.acquireGate("evt-7", AuthorizationChangeKind.ROLE))
			.thenReturn(InvalidationIdempotencyGate.Claimed.builder().build());
		when(impactResolverRegistry.resolve(any())).thenReturn(Set.of(1L, 99L));
		when(userInvalidationRepository.loadByUserIds(Set.of(1L, 99L))).thenReturn(List.of(activeState()));
		when(userInvalidationRepository.incrementPermVersionInBatches(Set.of(1L), BatchSizes.SIZE_500)).thenReturn(1);
		when(authProfileMaterializationService.refreshInBatches(Set.of(1L), BatchSizes.SIZE_500)).thenReturn(1);
		when(authProfileMaterializationService.evictInBatches(Set.of(99L), BatchSizes.SIZE_500)).thenReturn(1);

		AuthorizationInvalidateRequest request = new AuthorizationInvalidateRequest("evt-7",
				AuthorizationChangeKind.ROLE, new RoleInvalidatePayload(List.of("ADMIN")));
		AuthorizationInvalidateResponse response = authorizationInvalidationService.invalidate(request);

		assertEquals(new AuthorizationInvalidateResponse(2, 1, 1, 1), response);
		verify(userInvalidationRepository).incrementPermVersionInBatches(Set.of(1L), BatchSizes.SIZE_500);
		verify(authProfileMaterializationService).refreshInBatches(Set.of(1L), BatchSizes.SIZE_500);
		verify(authProfileMaterializationService).evictInBatches(Set.of(99L), BatchSizes.SIZE_500);
	}

	@Test
	@DisplayName("流水线失败时释放幂等占位")
	void invalidate_pipelineFailure_shouldReleaseClaim() {
		when(authorizationInvalidationEventService.acquireGate("evt-5", AuthorizationChangeKind.ROLE))
			.thenReturn(InvalidationIdempotencyGate.Claimed.builder().build());
		when(impactResolverRegistry.resolve(any())).thenThrow(new IllegalStateException("resolver error"));

		AuthorizationInvalidateRequest request = new AuthorizationInvalidateRequest("evt-5",
				AuthorizationChangeKind.ROLE, new RoleInvalidatePayload(List.of("ADMIN")));

		try {
			authorizationInvalidationService.invalidate(request);
		}
		catch (IllegalStateException ignored) {
			// expected
		}

		verify(authorizationInvalidationEventService).releaseClaim("evt-5");
		verify(authorizationInvalidationEventService, never()).completeProcessedOutcome(any(), any(), any());
	}

	@Test
	@DisplayName("他人处理中时返回空结果")
	void invalidate_inProgress_shouldReturnEmpty() {
		when(authorizationInvalidationEventService.acquireGate("evt-6", AuthorizationChangeKind.ROLE))
			.thenReturn(InvalidationIdempotencyGate.InProgress.builder().build());

		AuthorizationInvalidateRequest request = new AuthorizationInvalidateRequest("evt-6",
				AuthorizationChangeKind.ROLE, new RoleInvalidatePayload(List.of("ADMIN")));
		AuthorizationInvalidateResponse response = authorizationInvalidationService.invalidate(request);

		assertEquals(AuthorizationInvalidateResponse.empty(), response);
		verify(impactResolverRegistry, never()).resolve(any());
	}

}
