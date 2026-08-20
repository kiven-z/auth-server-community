package com.auth.module.security.autoconfigure.service;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.exception.SecurityResultCodeEnum;
import com.auth.module.security.contract.exception.SecurityTokenException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import static com.auth.module.security.contract.redis.SecurityRedisKey.USER_PERM;

/**
 * 读取授权画像缓存（AuthProfile）
 *
 * <p>
 * 注意：RedisTemplate 的 JSON 序列化在没有类型信息时可能反序列化为 Map，这里使用 ObjectMapper 进行二次转换；若运行时仍为
 * {@link AuthProfile} 实例则直接返回
 * </p>
 *
 * @author Bunny
 */
@Service
public class AuthProfileCacheService {

	private final RedisTemplate<String, Object> redisTemplate;

	private final ObjectMapper objectMapper;

	public AuthProfileCacheService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
		this.redisTemplate = redisTemplate;
		this.objectMapper = objectMapper;
	}

	/**
	 * 加载授权画像
	 * @param userId 用户 ID
	 * @return 授权画像；缓存未命中时返回 null
	 */
	public AuthProfile load(long userId) {
		String key = USER_PERM.key(userId);
		Object raw;
		try {
			raw = redisTemplate.opsForValue().get(key);
		}
		catch (RuntimeException ex) {
			throw new SecurityTokenException(SecurityResultCodeEnum.REDIS_UNAVAILABLE,
					"Redis unavailable when loading AuthProfile: " + ex.getMessage());
		}

		if (raw == null) {
			return null;
		}

		if (raw instanceof AuthProfile profile) {
			return profile;
		}

		try {
			return objectMapper.convertValue(raw, AuthProfile.class);
		}
		catch (IllegalArgumentException ex) {
			throw new SecurityTokenException(SecurityResultCodeEnum.PROFILE_DESERIALIZE_FAILED,
					"Failed to deserialize AuthProfile: " + ex.getMessage());
		}
	}

}
