package com.auth.service.auth.support.invalidation;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.support.authorization.AuthProfileRepository;
import com.auth.service.auth.support.redis.AuthProfileRedisCache;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * {@link AuthProfileMaterializationService} 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AuthProfileMaterializationServiceTest {

	@Mock
	private AuthProfileRepository authProfileRepository;

	@Mock
	private AuthProfileRedisCache authProfileRedisCache;

	@InjectMocks
	private AuthProfileMaterializationService authProfileMaterializationService;

	@Test
	@DisplayName("分批刷新：每批批量构建并写入 Redis")
	void refreshInBatches_shouldBuildAndCacheByBatch() {
		AuthProfile profile = mock(AuthProfile.class);
		when(authProfileRepository.buildByUserIds(List.of(1L, 2L))).thenReturn(List.of(profile));

		int refreshed = authProfileMaterializationService.refreshInBatches(List.of(1L, 2L), 500);

		assertEquals(1, refreshed);
		verify(authProfileRepository).buildByUserIds(List.of(1L, 2L));
		verify(authProfileRedisCache).cacheProfiles(List.of(profile));
	}

	@Test
	@DisplayName("分批驱逐：每批批量删除 Redis 键")
	void evictInBatches_shouldEvictByBatch() {
		when(authProfileRedisCache.evictProfiles(List.of(3L, 4L))).thenReturn(2);

		int evicted = authProfileMaterializationService.evictInBatches(List.of(3L, 4L), 500);

		assertEquals(2, evicted);
		verify(authProfileRedisCache).evictProfiles(List.of(3L, 4L));
	}

	@Test
	@DisplayName("空入参时跳过刷新")
	void refreshInBatches_shouldSkipWhenEmpty() {
		assertEquals(0, authProfileMaterializationService.refreshInBatches(List.of(), 500));
		verifyNoInteractions(authProfileRepository, authProfileRedisCache);
	}

}
