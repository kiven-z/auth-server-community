package com.auth.service.auth.support.redis.store;

import cn.hutool.crypto.digest.MD5;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import com.auth.service.auth.model.enums.CredentialDimension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;

import static com.auth.module.security.contract.redis.SecurityRedisKey.EMAIL_CODE;
import static com.auth.module.security.contract.redis.SecurityRedisKey.SMS_CODE;

/**
 * 登录验证码 Redis 存储。
 *
 * @author Bunny
 */
@Service
public class LoginVerificationCodeStore {

	private final RedisTemplate<String, Object> redisTemplate;

	public LoginVerificationCodeStore(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	/**
	 * 按 Redis key 校验验证码并在成功后删除。
	 * @param key 验证码 Redis key
	 * @param code 用户输入的验证码
	 */
	public void verifyAndConsume(String key, String code) {
		String codeDigest = MD5.create().digestHex16(Objects.requireNonNull(code));
		String stored = key == null ? null : (String) redisTemplate.opsForValue().get(key);
		if (!Objects.equals(codeDigest, stored)) {
			throw new AuthBusinessException(AuthResultCode.AUTH_CODE_ERROR);
		}

		if (key != null) {
			redisTemplate.delete(key);
		}
	}

	/**
	 * 存储验证码摘要。
	 * @param dimension 凭证维度
	 * @param target 凭证值
	 * @param digestHex16 MD5 摘要（hex16）
	 */
	public void storeDigest(CredentialDimension dimension, String target, String digestHex16) {
		String key = CredentialDimension.resolveKey(dimension, target);
		Duration ttl = switch (dimension) {
			case EMAIL -> EMAIL_CODE.getDefaultTtl();
			case PHONE -> SMS_CODE.getDefaultTtl();
			default -> null;
		};

		if (key != null && ttl != null) {
			redisTemplate.opsForValue().set(key, digestHex16, ttl);
		}
	}

}
