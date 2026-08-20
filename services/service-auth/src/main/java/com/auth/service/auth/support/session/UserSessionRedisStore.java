package com.auth.service.auth.support.session;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.NumberUtil;
import com.auth.module.security.contract.api.UserSessionIndex;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.auth.module.security.contract.redis.SecurityRedisKey.*;

/**
 * 用户会话 Redis 写路径编排：生命周期、索引维护与在线用户同步
 *
 * @author Bunny
 */
@Service
public class UserSessionRedisStore {

	private final RedisTemplate<String, String> sessionRedisTemplate;

	private final UserSessionRedisScripts userSessionRedisScripts;

	private final OnlineUserZSetStore onlineUserZSetStore;

	public UserSessionRedisStore(@Qualifier("sessionRedisTemplate") RedisTemplate<String, String> sessionRedisTemplate,
			UserSessionRedisScripts userSessionRedisScripts, OnlineUserZSetStore onlineUserZSetStore) {
		this.sessionRedisTemplate = sessionRedisTemplate;
		this.userSessionRedisScripts = userSessionRedisScripts;
		this.onlineUserZSetStore = onlineUserZSetStore;
	}

	/**
	 * 注册活跃会话：写入 Hash、用户 Set 与在线用户 ZSet（Lua 原子）
	 * @param userSessionIndex 用户会话索引
	 */
	public void registerSession(@NotNull UserSessionIndex userSessionIndex) {
		String sessionId = userSessionIndex.getSessionId();
		Long userId = userSessionIndex.getUserId();
		if (CharSequenceUtil.isBlank(sessionId) || userId == null) {
			throw new IllegalArgumentException("sessionId and userId are required for session registration");
		}

		List<String> keys = List.of(USER_SESSION.key(sessionId), USER_SESSIONS.key(userId), ONLINE_USERS.fixedKey());
		Object[] scriptArgs = UserSessionIndexRedisCodec.buildRegisterScriptArgs(userSessionIndex);
		sessionRedisTemplate.execute(userSessionRedisScripts.registerScript(), keys, scriptArgs);
	}

	/**
	 * 终止单条会话：删除 Hash 与用户 Set（Lua 原子，幂等）
	 * @param userId 用户 ID
	 * @param jti 会话唯一标识
	 */
	public void terminateSession(long userId, @NotNull String jti) {
		if (CharSequenceUtil.isBlank(jti)) {
			return;
		}
		List<String> keys = List.of(USER_SESSION.key(jti), USER_SESSIONS.key(userId));
		sessionRedisTemplate.execute(userSessionRedisScripts.terminateScript(), keys, jti);
		syncOnlineUserAfterTerminate(userId);
	}

	/**
	 * 终止用户全部活跃会话
	 * @param userId 用户 ID
	 */
	public void terminateAllSessions(long userId) {
		String userSessionsKey = USER_SESSIONS.key(userId);
		List<String> jtis = streamActiveJtis(userSessionsKey).toList();
		sessionRedisTemplate.delete(userSessionsKey);
		jtis.forEach(jti -> {
			if (CharSequenceUtil.isBlank(jti)) {
				return;
			}
			List<String> keys = List.of(USER_SESSION.key(jti), USER_SESSIONS.key(userId));
			sessionRedisTemplate.execute(userSessionRedisScripts.terminateScript(), keys, jti);
		});
		syncOnlineUserAfterTerminate(userId);
	}

	/**
	 * 读取会话索引（不存在返回空）
	 * @param jti 会话唯一标识
	 * @return 会话索引
	 */
	public Optional<UserSessionIndex> loadUserSessionIndex(@NotNull String jti) {
		if (CharSequenceUtil.isBlank(jti)) {
			return Optional.empty();
		}
		Map<String, String> hash = UserSessionIndexRedisCodec
			.asStringHash(sessionRedisTemplate.opsForHash().entries(USER_SESSION.key(jti)));
		if (CollUtil.isEmpty(hash)) {
			return Optional.empty();
		}
		return Optional.of(UserSessionIndexRedisCodec.toSessionIndex(jti, hash, null));
	}

	/**
	 * 原子旋转 refresh：命中当前 hash 则写入新令牌；grace 内命中 previous 则返回上一轮签发结果
	 * @param command 旋转入参
	 * @return 旋转结果
	 */
	public RefreshRotateResult rotateRefresh(@NotNull RefreshRotateCommand command) {
		long nowMs = System.currentTimeMillis();
		Long code = sessionRedisTemplate.execute(userSessionRedisScripts.rotateRefreshScript(),
				List.of(USER_SESSION.key(command.jti())), command.requestRefreshHash(), command.newRefreshHash(),
				String.valueOf(command.newRefreshExpiresAtMs()), String.valueOf(command.ttl().getSeconds()),
				String.valueOf(nowMs), String.valueOf(command.graceMs()), command.accessToken(), command.refreshToken(),
				String.valueOf(command.accessExpiresAtMs()));

		// 将 Lua 返回码转为旋转结果；REUSED 时读取 last* 字段
		if (code == null || code == 0L) {
			return RefreshRotateResult.builder().outcome(RefreshRotateOutcome.EXPIRED).build();
		}
		if (code == 1L) {
			return RefreshRotateResult.builder().outcome(RefreshRotateOutcome.ROTATED).build();
		}
		if (code == 2L) {
			return loadReusedTokens(command.jti());
		}
		return RefreshRotateResult.builder().outcome(RefreshRotateOutcome.MISMATCH).build();
	}

	/**
	 * 读取 grace 复用所需的上一轮签发令牌
	 * @param jti 会话唯一标识
	 * @return REUSED 结果；字段缺失时降为 MISMATCH
	 */
	private RefreshRotateResult loadReusedTokens(String jti) {
		List<Object> values = sessionRedisTemplate.opsForHash()
			.multiGet(USER_SESSION.key(jti), List.of("lastAccessToken", "lastRefreshToken", "lastAccessExpiresAt"));
		if (values == null || values.size() < 3) {
			return RefreshRotateResult.builder().outcome(RefreshRotateOutcome.MISMATCH).build();
		}
		String accessToken = values.get(0) == null ? null : String.valueOf(values.get(0));
		String refreshToken = values.get(1) == null ? null : String.valueOf(values.get(1));
		String accessExpiresAt = values.get(2) == null ? null : String.valueOf(values.get(2));
		if (CharSequenceUtil.hasBlank(accessToken, refreshToken, accessExpiresAt)
				|| !NumberUtil.isLong(accessExpiresAt)) {
			return RefreshRotateResult.builder().outcome(RefreshRotateOutcome.MISMATCH).build();
		}
		return RefreshRotateResult.builder()
			.outcome(RefreshRotateOutcome.REUSED)
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.accessExpiresAt(Instant.ofEpochMilli(Long.parseLong(accessExpiresAt)))
			.build();
	}

	/**
	 * 统计用户活跃会话数
	 * @param userId 用户 ID
	 * @return 活跃会话数
	 */
	public long countActiveSessions(long userId) {
		return Objects.requireNonNullElse(sessionRedisTemplate.opsForSet().size(USER_SESSIONS.key(userId)), 0L);
	}

	/**
	 * 获取用户所有活跃会话（Hash 不存在时清理 Set 残留）
	 * @param userId 用户 ID
	 * @return 会话列表
	 */
	public List<UserSessionIndex> listUserSessions(long userId) {
		String userSessionsKey = USER_SESSIONS.key(userId);
		List<UserSessionIndex> sessions = streamActiveJtis(userSessionsKey)
			.map(jti -> loadListedSession(userSessionsKey, userId, jti))
			.flatMap(Optional::stream)
			.toList();
		syncOnlineUserAfterTerminate(userId);
		return sessions;
	}

	/**
	 * 清理活跃会话集合中的僵尸会话
	 * @param userId 用户 ID
	 * @return 清理数量
	 */
	public int cleanupStaleActiveSessions(long userId) {
		String userSessionsKey = USER_SESSIONS.key(userId);
		return (int) streamActiveJtis(userSessionsKey).filter(jti -> CollUtil.isEmpty(UserSessionIndexRedisCodec
			.asStringHash(sessionRedisTemplate.opsForHash().entries(USER_SESSION.key(jti))))).filter(jti -> {
				// 清理用户 Set 中的僵尸 jti
				Long setRemoved = sessionRedisTemplate.opsForSet().remove(userSessionsKey, jti);
				return NumberUtil.nullToZero(setRemoved) > 0;
			}).count();
	}

	/**
	 * 超限时淘汰最旧会话
	 * @param userId 用户 ID
	 * @return 被淘汰会话 ID，若无可淘汰会话则为空
	 */
	public Optional<String> evictOldestActiveSession(long userId) {
		String userSessionsKey = USER_SESSIONS.key(userId);
		return streamActiveJtis(userSessionsKey)
			.map(jti -> loadListedSession(userSessionsKey, userId, jti).map(index -> jti))
			.flatMap(Optional::stream)
			.min(Comparator.comparingLong(jti -> {
				Long ttl = sessionRedisTemplate.getExpire(USER_SESSION.key(jti), TimeUnit.SECONDS);
				if (ttl < 0) {
					return Long.MAX_VALUE;
				}
				return ttl;
			}))
			.map(evictedJti -> {
				List<String> keys = List.of(USER_SESSION.key(evictedJti), USER_SESSIONS.key(userId));
				sessionRedisTemplate.execute(userSessionRedisScripts.terminateScript(), keys, evictedJti);
				syncOnlineUserAfterTerminate(userId);
				return evictedJti;
			});
	}

	/**
	 * 会话终止后：若无活跃会话则从在线用户 ZSet 移除
	 * @param userId 用户 ID
	 */
	public void syncOnlineUserAfterTerminate(long userId) {
		if (countActiveSessions(userId) <= 0) {
			onlineUserZSetStore.remove(userId);
		}
	}

	/**
	 * 用户活跃会话 Set 中的 jti 流
	 * @param userSessionsKey 用户会话 Set key
	 * @return 非空 jti 流
	 */
	private Stream<String> streamActiveJtis(String userSessionsKey) {
		return Optional.ofNullable(sessionRedisTemplate.opsForSet().members(userSessionsKey))
			.orElseGet(Collections::emptySet)
			.stream()
			.filter(CharSequenceUtil::isNotBlank);
	}

	/**
	 * 列表场景加载单条会话；空 Hash 则清理 Set 残留
	 * @param userSessionsKey 用户会话 Set key
	 * @param userId 用户 ID
	 * @param jti 会话唯一标识
	 * @return 会话索引
	 */
	private Optional<UserSessionIndex> loadListedSession(String userSessionsKey, long userId, String jti) {
		Map<String, String> hash = UserSessionIndexRedisCodec
			.asStringHash(sessionRedisTemplate.opsForHash().entries(USER_SESSION.key(jti)));
		if (CollUtil.isEmpty(hash)) {
			// 清理用户 Set 中的僵尸 jti
			sessionRedisTemplate.opsForSet().remove(userSessionsKey, jti);
			return Optional.empty();
		}
		return Optional.of(UserSessionIndexRedisCodec.toSessionIndex(jti, hash, userId));
	}

}
