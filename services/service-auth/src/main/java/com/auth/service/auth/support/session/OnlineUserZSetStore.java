package com.auth.service.auth.support.session;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.auth.module.security.contract.redis.SecurityRedisKey.ONLINE_USERS;

/**
 * 在线用户 ZSet 唯一写读入口
 *
 * @author Bunny
 */
@Service
public class OnlineUserZSetStore {

	private final RedisTemplate<String, String> sessionRedisTemplate;

	public OnlineUserZSetStore(@Qualifier("sessionRedisTemplate") RedisTemplate<String, String> sessionRedisTemplate) {
		this.sessionRedisTemplate = sessionRedisTemplate;
	}

	/**
	 * 写入或更新在线用户分值（Java 侧补偿；注册主路径由 Lua 写入）
	 * @param userId 用户 ID
	 * @param loginAtScore 登录时间分值（毫秒时间戳）
	 */
	public void upsert(long userId, double loginAtScore) {
		sessionRedisTemplate.opsForZSet().add(ONLINE_USERS.fixedKey(), String.valueOf(userId), loginAtScore);
	}

	/**
	 * 按用户 ID 移出在线用户 ZSet
	 * @param userId 用户 ID
	 */
	public void remove(long userId) {
		sessionRedisTemplate.opsForZSet().remove(ONLINE_USERS.fixedKey(), String.valueOf(userId));
	}

	/**
	 * 按 ZSet member 原文移出（用于非法 member 清理）
	 * @param member ZSet member
	 */
	public void removeByMember(String member) {
		sessionRedisTemplate.opsForZSet().remove(ONLINE_USERS.fixedKey(), member);
	}

	/**
	 * 在线用户总数
	 * @return ZSet 基数
	 */
	public long totalCount() {
		return Objects.requireNonNullElse(sessionRedisTemplate.opsForZSet().zCard(ONLINE_USERS.fixedKey()), 0L);
	}

	/**
	 * 按登录时间倒序分页读取 member
	 * @param start 起始 rank（含）
	 * @param end 结束 rank（含）
	 * @return member 集合，无数据时为空集
	 */
	public Set<String> reverseRange(long start, long end) {
		return Optional.ofNullable(sessionRedisTemplate.opsForZSet().reverseRange(ONLINE_USERS.fixedKey(), start, end))
			.orElseGet(Collections::emptySet);
	}

	/**
	 * 读取用户在线分值
	 * @param userId 用户 ID
	 * @return 分值，不存在时为 null
	 */
	public Double score(long userId) {
		return sessionRedisTemplate.opsForZSet().score(ONLINE_USERS.fixedKey(), String.valueOf(userId));
	}

}
