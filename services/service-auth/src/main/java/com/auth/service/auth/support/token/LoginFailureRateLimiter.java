package com.auth.service.auth.support.token;

import com.auth.module.security.autoconfigure.config.user.UserConfigProperties;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.auth.module.security.contract.redis.SecurityRedisKey.LOGIN_FAILURE_COUNT;

/**
 * 登录失败限流器
 *
 * <p>
 * 专注处理登录失败计数与账户锁定，不涉及会话容量控制。 职责清晰的三方法：
 * <ul>
 * <li>{@link #assertNotLocked(Long)}：登录前置只读检查，密码失败达阈值时抛锁定异常。</li>
 * <li>{@link #recordFailure(Long)}：认证失败时调用，自增计数并按需设置 TTL，返回剩余次数。</li>
 * <li>{@link #recordSuccess(Long)}：认证成功时调用，清除失败计数。</li>
 * </ul>
 * </p>
 *
 * @author Bunny
 */
@Component
public class LoginFailureRateLimiter {

	/**
	 * userId 非空断言文案
	 */
	private static final String USER_ID_NOT_NULL = "userId must not be null";

	/**
	 * 失败计数滑动窗口：仅在尚未达到锁定阈值前用于限定计数生命周期
	 */
	private static final Duration ATTEMPT_TTL = Duration.ofMinutes(5);

	/**
	 * 锁定时长：达到失败阈值后写入此 TTL，保证锁定窗口内计数不消失
	 */
	private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

	private final RedisTemplate<String, Object> redisTemplate;

	private final UserConfigProperties userConfigProperties;

	public LoginFailureRateLimiter(RedisTemplate<String, Object> redisTemplate,
			UserConfigProperties userConfigProperties) {
		this.redisTemplate = redisTemplate;
		this.userConfigProperties = userConfigProperties;
	}

	/**
	 * 登录前置检查（只读）：密码失败达阈值时抛锁定异常。
	 * @param userId 用户 ID
	 */
	public void assertNotLocked(Long userId) {
		Assert.notNull(userId, USER_ID_NOT_NULL);

		// 配置中最大登录尝试次数
		Integer maxPasswordAttempts = userConfigProperties.getMaxPasswordAttempts();

		// Redis 中已经登录的最大尝试次数
		String failureCountKey = LOGIN_FAILURE_COUNT.key(userId);
		Object value = redisTemplate.opsForValue().get(failureCountKey);
		long failures = value instanceof Number num ? num.longValue() : 0L;

		// 超过最大配置尝试次数
		if (failures >= maxPasswordAttempts) {
			Long expireMinute = redisTemplate.getExpire(failureCountKey, TimeUnit.MINUTES);
			long minutes = Math.max(1L, expireMinute);
			throw new AuthBusinessException(AuthResultCode.USER_LOCKED_WITH_MINUTES, minutes);
		}
	}

	/**
	 * 认证失败时调用：自增失败计数；首次落地设 {@link #ATTEMPT_TTL}；达阈值时升级为 {@link #LOCK_DURATION}。
	 * @param userId 用户 ID
	 * @return 剩余可尝试次数（已锁定返回 0）
	 */
	public int recordFailure(Long userId) {
		Assert.notNull(userId, USER_ID_NOT_NULL);

		String key = LOGIN_FAILURE_COUNT.key(userId);
		Long count = redisTemplate.opsForValue().increment(key);
		if (count == null) {
			return 0;
		}

		// 判断是否超过最大次数
		int max = userConfigProperties.getMaxPasswordAttempts();
		if (count == 1L) {
			redisTemplate.expire(key, ATTEMPT_TTL);
		}
		if (count >= max) {
			redisTemplate.expire(key, LOCK_DURATION);
		}
		return Math.max(0, max - count.intValue());
	}

	/**
	 * 认证成功时调用：清除失败计数
	 * @param userId 用户 ID
	 */
	public void recordSuccess(Long userId) {
		Assert.notNull(userId, USER_ID_NOT_NULL);

		String failureCount = LOGIN_FAILURE_COUNT.key(userId);
		redisTemplate.delete(failureCount);
	}

}
