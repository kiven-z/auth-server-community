package com.auth.service.auth.support.redis;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.auth.module.security.contract.redis.SecurityRedisKey.USER_PERM;

/**
 * 授权画像 Redis 缓存
 *
 * @author Bunny
 */
@Repository
public class AuthProfileRedisCache {

	private final RedisTemplate<String, Object> redisTemplate;

	private final ObjectMapper objectMapper;

	public AuthProfileRedisCache(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
		this.redisTemplate = redisTemplate;
		this.objectMapper = objectMapper;
	}

	/**
	 * 批量写入授权画像缓存
	 * @param authProfiles 授权画像列表
	 */
	public void cacheProfiles(@NotNull Collection<AuthProfile> authProfiles) {
		if (CollUtil.isEmpty(authProfiles)) {
			return;
		}
		for (AuthProfile authProfile : authProfiles) {
			if (authProfile == null) {
				continue;
			}

			Long userId = authProfile.getUserId();
			Assert.notNull(userId, "用户ID不能为空");

			redisTemplate.opsForValue().set(USER_PERM.key(userId), authProfile, USER_PERM.getDefaultTtl());
		}
	}

	/**
	 * 读取授权画像缓存
	 * @param userId 用户 ID
	 * @return 授权画像
	 */
	public Optional<AuthProfile> loadCachedProfile(long userId) {
		Object value = redisTemplate.opsForValue().get(USER_PERM.key(userId));

		if (value == null) {
			return Optional.empty();
		}

		if (value instanceof AuthProfile profile) {
			return Optional.of(profile);
		}

		AuthProfile profile = objectMapper.convertValue(value, AuthProfile.class);
		return Optional.ofNullable(profile);
	}

	/**
	 * 驱逐授权画像缓存
	 * @param userId 用户 ID
	 */
	public void evictProfile(long userId) {
		String key = USER_PERM.key(userId);
		redisTemplate.delete(key);
	}

	/**
	 * 批量驱逐授权画像缓存
	 * @param userIds 用户 ID
	 * @return 成功驱逐数量
	 */
	public int evictProfiles(Collection<Long> userIds) {
		if (CollUtil.isEmpty(userIds)) {
			return 0;
		}
		List<String> keys = userIds.stream().filter(Objects::nonNull).map(USER_PERM::key).distinct().toList();
		if (CollUtil.isEmpty(keys)) {
			return 0;
		}
		Long deleted = redisTemplate.delete(keys);
		return deleted == null ? 0 : deleted.intValue();
	}

}
