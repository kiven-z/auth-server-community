package com.auth.service.auth.support.invalidation;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.support.authorization.AuthProfileRepository;
import com.auth.service.auth.support.redis.AuthProfileRedisCache;
import com.auth.service.auth.util.BatchPartition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 授权画像物化：分批重建缓存或驱逐 Redis 画像
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class AuthProfileMaterializationService {

	private final AuthProfileRepository authProfileRepository;

	private final AuthProfileRedisCache authProfileRedisCache;

	/**
	 * 分批重建并写入 Redis 画像
	 * @param userIds 用户 ID
	 * @param batchSize 每批大小
	 * @return 成功刷新数量
	 */
	public int refreshInBatches(Collection<Long> userIds, int batchSize) {
		if (CollUtil.isEmpty(userIds) || batchSize <= 0) {
			return 0;
		}

		int refreshed = 0;
		for (List<Long> batch : BatchPartition.partitionIds(userIds, batchSize)) {
			refreshed += refreshBatch(batch);
		}
		return refreshed;
	}

	/**
	 * 分批驱逐 Redis 画像
	 * @param userIds 用户 ID
	 * @param batchSize 每批大小
	 * @return 成功驱逐数量
	 */
	public int evictInBatches(Collection<Long> userIds, int batchSize) {
		if (CollUtil.isEmpty(userIds) || batchSize <= 0) {
			return 0;
		}

		int evicted = 0;
		for (List<Long> batch : BatchPartition.partitionIds(userIds, batchSize)) {
			evicted += evictBatch(batch);
		}
		return evicted;
	}

	private int refreshBatch(List<Long> batch) {
		if (CollUtil.isEmpty(batch)) {
			return 0;
		}
		try {
			List<AuthProfile> profiles = authProfileRepository.buildByUserIds(batch);
			if (CollUtil.isEmpty(profiles)) {
				return 0;
			}
			authProfileRedisCache.cacheProfiles(profiles);
			return profiles.size();
		}
		catch (RuntimeException ex) {
			log.warn("Failed to refresh auth profiles for userIds={}: {}", batch, ex.getMessage());
			return 0;
		}
	}

	private int evictBatch(List<Long> batch) {
		if (CollUtil.isEmpty(batch)) {
			return 0;
		}
		try {
			return authProfileRedisCache.evictProfiles(batch);
		}
		catch (RuntimeException ex) {
			log.warn("Failed to evict auth profiles for userIds={}: {}", batch, ex.getMessage());
			return 0;
		}
	}

}
