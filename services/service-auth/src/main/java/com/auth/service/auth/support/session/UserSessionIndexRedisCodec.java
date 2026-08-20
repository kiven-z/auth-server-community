package com.auth.service.auth.support.session;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import com.auth.module.security.contract.api.UserSessionIndex;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;

import static com.auth.module.security.contract.redis.SecurityRedisKey.USER_SESSION;

/**
 * 会话索引 Redis Hash
 *
 * @author Bunny
 */
@UtilityClass
public class UserSessionIndexRedisCodec {

	/**
	 * 将 Redis Hash entries 转为纯字符串
	 * @param raw 原始 Hash
	 * @return field/value 均为字符串的 Map
	 */
	public Map<String, String> asStringHash(Map<Object, Object> raw) {
		if (MapUtil.isEmpty(raw)) {
			return Collections.emptyMap();
		}
		Map<String, String> normalized = new LinkedHashMap<>(raw.size());
		raw.forEach((key, value) -> {
			if (key != null && value != null) {
				normalized.put(String.valueOf(key), String.valueOf(value));
			}
		});
		return normalized;
	}

	/**
	 * 从 Hash 组装会话索引
	 * @param jti 会话唯一标识
	 * @param hash 会话 Hash（field/value 均为纯字符串）
	 * @param overrideUserId 覆盖 userId（列表场景传入；单条加载时为 null）
	 * @return 会话索引
	 */
	public UserSessionIndex toSessionIndex(String jti, Map<String, String> hash, Long overrideUserId) {
		UserSessionIndex sessionIndex = BeanUtil.toBean(hash, UserSessionIndex.class);
		if (overrideUserId != null) {
			sessionIndex.setUserId(overrideUserId);
		}
		sessionIndex.setSessionId(jti);
		return sessionIndex;
	}

	/**
	 * 构建 register Lua 脚本参数
	 * @param userSessionIndex 会话索引
	 * @return ARGV 数组
	 */
	public Object[] buildRegisterScriptArgs(@NotNull UserSessionIndex userSessionIndex) {
		String sessionId = userSessionIndex.getSessionId();

		// 解析在线用户 ZSet 排序分值
		Long loginAt = userSessionIndex.getLoginAt();
		double loginAtScore = loginAt == null ? System.currentTimeMillis() : loginAt.doubleValue();
		Duration ttl = resolveSessionTtl(userSessionIndex);

		List<String> args = new ArrayList<>();
		args.add(sessionId);
		args.add(String.valueOf((long) loginAtScore));
		args.add(String.valueOf(ttl != null ? ttl.getSeconds() : 0L));
		args.add(String.valueOf(userSessionIndex.getUserId()));

		Map<String, Object> sessionMap = BeanUtil.beanToMap(userSessionIndex);
		sessionMap.forEach((field, value) -> {
			if (value != null) {
				args.add(field);
				args.add(String.valueOf(value));
			}
		});
		return args.toArray(new Object[0]);
	}

	/**
	 * 根据 refreshToken 过期时间计算会话 TTL
	 * @param userSessionIndex 会话索引
	 * @return TTL，无法计算时返回默认 TTL
	 */
	private Duration resolveSessionTtl(@NotNull UserSessionIndex userSessionIndex) {
		// 获取刷新令牌过期时间
		Long refreshTokenExpiresAt = userSessionIndex.getRefreshTokenExpiresAt();
		Duration ttl = USER_SESSION.getDefaultTtl();

		// 计算会话 TTL
		if (refreshTokenExpiresAt != null) {
			long ttlSeconds = (refreshTokenExpiresAt - System.currentTimeMillis()) / 1000;
			if (ttlSeconds > 0) {
				ttl = Duration.ofSeconds(ttlSeconds);
			}
		}
		return ttl;
	}

}
